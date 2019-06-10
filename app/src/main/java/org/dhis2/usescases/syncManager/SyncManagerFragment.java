package org.dhis2.usescases.syncManager;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.work.State;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.BuildConfig;
import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.FragmentSyncManagerBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.SyncUtils;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.toptas.fancyshowcase.listener.DismissListener;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import timber.log.Timber;

import static org.dhis2.utils.Constants.DATA_NOW;
import static org.dhis2.utils.Constants.META_NOW;
import static org.dhis2.utils.Constants.TIME_15M;
import static org.dhis2.utils.Constants.TIME_DAILY;
import static org.dhis2.utils.Constants.TIME_HOURLY;
import static org.dhis2.utils.Constants.TIME_MANUAL;
import static org.dhis2.utils.Constants.TIME_WEEKLY;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncManagerFragment extends FragmentGlobalAbstract implements SyncManagerContracts.View {

    private int metaInitializationCheck = 0;
    private int dataInitializationCheck = 0;

    @Inject
    SyncManagerContracts.Presenter presenter;

    private FragmentSyncManagerBinding binding;
    private SharedPreferences prefs;
    private CompositeDisposable listenerDisposable;
    private Context context;


    public SyncManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        ((Components) context.getApplicationContext()).userComponent()
                .plus(new SyncManagerModule()).inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sync_manager, container, false);

        binding.setPresenter(presenter);
        prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);

        initRadioGroups();

        binding.dataPeriods.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (dataInitializationCheck++ >= 1)
                    saveTimeData(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /*NO USE*/
            }
        });
        binding.metadataPeriods.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (metaInitializationCheck++ >= 1)
                    saveTimeMeta(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /*NO USE*/
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        WorkManager.getInstance().getStatusesByTagLiveData(META_NOW).observe(this, workStatuses -> {
            if (!workStatuses.isEmpty() && workStatuses.get(0).getState() == State.RUNNING) {
                binding.metadataLastSync.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
                binding.metadataLastSync.setText(R.string.syncing_configuration);
                binding.buttonSyncMeta.setEnabled(false);
            } else {
                binding.buttonSyncMeta.setEnabled(true);
                setLastSyncDate();
                presenter.checkData();
            }
        });
        WorkManager.getInstance().getStatusesByTagLiveData(DATA_NOW).observe(this, workStatuses -> {
            if (!workStatuses.isEmpty() && workStatuses.get(0).getState() == State.RUNNING) {
                binding.dataLastSync.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
                binding.dataLastSync.setText(R.string.syncing_data);
                binding.buttonSyncData.setEnabled(false);
            } else {
                binding.buttonSyncData.setEnabled(true);
                setLastSyncDate();
                presenter.checkData();
            }
        });
        presenter.init(this);

        if (SyncUtils.isSyncRunning()) {
            binding.buttonSyncData.setEnabled(false);
            binding.buttonSyncMeta.setEnabled(false);
        }

        showTutorial();

        listenerDisposable = new CompositeDisposable();

        listenerDisposable.add(RxTextView.textChanges(binding.eventMaxData).debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> prefs.edit().putInt(Constants.EVENT_MAX, Integer.valueOf(data.toString())).apply(),
                        Timber::d
                ));

        listenerDisposable.add(RxTextView.textChanges(binding.teiMaxData).debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> prefs.edit().putInt(Constants.TEI_MAX, Integer.valueOf(data.toString())).apply(),
                        Timber::d
                ));

        binding.limitByOrgUnit.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(Constants.LIMIT_BY_ORG_UNIT, isChecked).apply());
        binding.limitByProgram.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(Constants.LIMIT_BY_PROGRAM, isChecked).apply());

        setLastSyncDate();
    }

    @Override
    public void onPause() {
        super.onPause();
        listenerDisposable.clear();
        presenter.disponse();
    }

    @Override
    public void onStop() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(123456);
        super.onStop();
    }

    @Override
    public Consumer<Pair<Integer, Integer>> setSyncData() {
        return syncParameters -> {
            binding.eventMaxData.setText(String.valueOf(prefs.getInt(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT)));
            binding.teiMaxData.setText(String.valueOf(prefs.getInt(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT)));
            binding.eventCurrentData.setText(String.valueOf(syncParameters.val0()));
            binding.teiCurrentData.setText(String.valueOf(syncParameters.val1()));
            binding.limitByOrgUnit.setChecked(prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false));
            binding.limitByProgram.setChecked(prefs.getBoolean(Constants.LIMIT_BY_PROGRAM, false));
        };
    }

    private void setLastSyncDate() {
        boolean dataStatus = prefs.getBoolean(Constants.LAST_DATA_SYNC_STATUS, true);
        boolean metaStatus = prefs.getBoolean(Constants.LAST_META_SYNC_STATUS, true);

        if (dataStatus) {
            binding.dataLastSync.setText(String.format(getString(R.string.last_data_sync_date), prefs.getString(Constants.LAST_DATA_SYNC, "-")));
            binding.metadataLastSync.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
        } else {
            binding.dataLastSync.setText(getString(R.string.sync_error_text));
        }

        if (presenter.dataHasErrors()) {
            String src = getString(R.string.data_sync_error);
            SpannableString str = new SpannableString(src);
            int wIndex = src.indexOf('@');
            int eIndex = src.indexOf('$');
            str.setSpan(new ImageSpan(getContext(), R.drawable.ic_sync_warning), wIndex, wIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new ImageSpan(getContext(), R.drawable.ic_sync_problem_red), eIndex, eIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.dataLastSync.setText(str);
            binding.dataLastSync.setTextColor(ContextCompat.getColor(getContext(), R.color.red_060));

        } else if (presenter.dataHasWarnings()) {
            String src = getString(R.string.data_sync_warning);
            SpannableString str = new SpannableString(src);
            int wIndex = src.indexOf('@');
            str.setSpan(new ImageSpan(getContext(), R.drawable.ic_sync_warning), wIndex, wIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.dataLastSync.setText(str);
            binding.dataLastSync.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryOrange));
        }

        if (metaStatus) {
            binding.metadataLastSync.setText(String.format(getString(R.string.last_data_sync_date), prefs.getString(Constants.LAST_META_SYNC, "-")));
            binding.metadataLastSync.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
        }else {
            binding.metadataLastSync.setText(getString(R.string.metadata_sync_error));
            binding.metadataLastSync.setTextColor(ContextCompat.getColor(context, R.color.red_060));
        }

    }

    private void initRadioGroups() {
        int timeData = prefs.getInt("timeData", TIME_DAILY);
        int timeMeta = prefs.getInt("timeMeta", TIME_DAILY);

        switch (timeData) {
            case TIME_15M:
                binding.dataPeriods.setSelection(0);
                break;
            case TIME_HOURLY:
                binding.dataPeriods.setSelection(1);
                break;
            case TIME_MANUAL:
                binding.dataPeriods.setSelection(3);
                break;
            case TIME_DAILY:
            default:
                binding.dataPeriods.setSelection(2);
                break;
        }

        switch (timeMeta) {
            case TIME_MANUAL:
                binding.metadataPeriods.setSelection(2);
                break;
            case TIME_WEEKLY:
                binding.metadataPeriods.setSelection(1);
                break;
            case TIME_DAILY:
            default:
                binding.metadataPeriods.setSelection(0);
                break;
        }
    }

    private void saveTimeData(int i) {
        int time;

        switch (i) {
            case 3:
                time = TIME_MANUAL;
                break;
            case 0:
                time = TIME_15M;
                break;
            case 1:
                time = TIME_HOURLY;
                break;
            case 2:
                time = TIME_DAILY;
                break;
            default:
                time = TIME_MANUAL;
                break;
        }
        prefs.edit().putInt(Constants.TIME_DATA, time).apply();
        if (time != TIME_MANUAL)
            presenter.syncData(time, Constants.DATA);
        else
            presenter.cancelPendingWork(Constants.DATA);
    }

    private void saveTimeMeta(int i) {
        int time;

        switch (i) {
            case 1:
                // 1 week
                time = TIME_WEEKLY;
                break;
            case 2:
                time = TIME_MANUAL;
                break;
            case 0:
            default:
                // 1 day (default)
                time = TIME_DAILY;
                break;
        }

        prefs.edit().putInt(Constants.TIME_META, time).apply();
        if (time != TIME_MANUAL)
            presenter.syncMeta(time, Constants.META);
        else
            presenter.cancelPendingWork(Constants.META);
    }

    @Override
    public void wipeDatabase() {
        new AlertDialog.Builder(context, R.style.CustomDialog)
                .setTitle(getString(R.string.wipe_data))
                .setMessage(getString(R.string.wipe_data_meesage))
                .setView(R.layout.warning_layout)
                .setPositiveButton(getString(R.string.wipe_data_ok), (dialog, which) -> showDeleteProgress())
                .setNegativeButton(getString(R.string.wipe_data_no), (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void deleteLocalData() {
        new AlertDialog.Builder(context, R.style.CustomDialog)
                .setTitle(getString(R.string.delete_local_data))
                .setMessage(getString(R.string.delete_local_data_message))
                .setView(R.layout.warning_layout)
                .setPositiveButton(getString(R.string.action_accept), (dialog, which) -> presenter.deleteLocalData())
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showDeleteProgress() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("wipe_notification", "Restart", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, "wipe_notification")
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(getString(R.string.wipe_data))
                        .setContentText(getString(R.string.please_wait))
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(123456, notificationBuilder.build());
        presenter.wipeDb();

    }

    @Override
    public void showTutorial() {
        if (isAdded() && getContext() != null) {
            NestedScrollView scrollView = getAbstractActivity().findViewById(R.id.scrollView);
            new Handler().postDelayed(() -> {
                if (getAbstractActivity() != null) {
                    FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .focusOn(getAbstractActivity().findViewById(R.id.dataPeriods))
                            .title(getString(R.string.tuto_settings_1))
                            .enableAutoTextPosition()
                            .closeOnTouch(true)
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .build();

                    FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .focusOn(getAbstractActivity().findViewById(R.id.metadataPeriods))
                            .title(getString(R.string.tuto_settings_2))
                            .enableAutoTextPosition()
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .closeOnTouch(true)
                            .build();

                    FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .focusOn(getAbstractActivity().findViewById(R.id.capacityLayout))
                            .title(getString(R.string.tuto_settings_3))
                            .enableAutoTextPosition()
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .closeOnTouch(true)
                            .dismissListener(new DismissListener() {
                                @Override
                                public void onDismiss(String id) {
                                    if (scrollView != null) {
                                        scrollView.scrollTo((int) getAbstractActivity().findViewById(R.id.reservedValue).getX(), (int) getAbstractActivity().findViewById(R.id.reservedValue).getY());
                                    }
                                }

                                @Override
                                public void onSkipped(String id) {
                                    // unused
                                }
                            })
                            .build();

                    FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .focusOn(getAbstractActivity().findViewById(R.id.reservedValue))
                            .title(getString(R.string.tuto_settings_reserved))
                            .enableAutoTextPosition()
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .closeOnTouch(true)
                            .build();

                    FancyShowCaseView tuto5 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .focusOn(getAbstractActivity().findViewById(R.id.buttonSyncError))
                            .title(getString(R.string.tuto_settings_errors))
                            .enableAutoTextPosition()
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .closeOnTouch(true)
                            .build();

                    FancyShowCaseView tuto6 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .focusOn(getAbstractActivity().findViewById(R.id.buttonDeleteLocalData))
                            .title(getString(R.string.tuto_settings_reset))
                            .enableAutoTextPosition()
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .closeOnTouch(true)
                            .build();

                    FancyShowCaseView tuto7 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .focusOn(getAbstractActivity().findViewById(R.id.wipeData))
                            .title(getString(R.string.tuto_settings_4))
                            .enableAutoTextPosition()
                            .closeOnTouch(true)
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .build();


                    ArrayList<FancyShowCaseView> steps = new ArrayList<>();
                    steps.add(tuto1);
                    steps.add(tuto2);
                    steps.add(tuto3);
                    steps.add(tuto4);
                    steps.add(tuto5);
                    steps.add(tuto6);
                    steps.add(tuto7);

                    HelpManager.getInstance().setScreenHelp(getClass().getName(), steps);
                    HelpManager.getInstance().setScroll(scrollView);

                    if (prefs != null && !prefs.getBoolean("TUTO_SETTINGS_SHOWN", false) && !BuildConfig.DEBUG) {
                        HelpManager.getInstance().showHelp();
                        prefs.edit().putBoolean("TUTO_SETTINGS_SHOWN", true).apply();
                    }
                }

            }, 500);
        }
    }

    @Override
    public void showSyncErrors(List<TrackerImportConflict> data) {
        new ErrorDialog().setData(data).show(getChildFragmentManager().beginTransaction(), ErrorDialog.TAG);
    }

    @Override
    public void showLocalDataDeleted(boolean error) {

        if (!error) {
            binding.eventCurrentData.setText(String.valueOf(0));
            binding.teiCurrentData.setText(String.valueOf(0));
        }

        Snackbar deleteDataSnack = Snackbar.make(binding.getRoot(),
                error ? R.string.delete_local_data_error : R.string.delete_local_data_done,
                Snackbar.LENGTH_SHORT);
        deleteDataSnack.show();
    }

    @Override
    public void syncData() {
        binding.dataLastSync.setText(R.string.syncing_data);
    }

    @Override
    public void syncMeta() {
        binding.metadataLastSync.setText(R.string.syncing_configuration);
    }
}
