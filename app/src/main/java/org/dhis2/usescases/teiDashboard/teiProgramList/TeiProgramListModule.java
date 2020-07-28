package org.dhis2.usescases.teiDashboard.teiProgramList;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.dhislogic.DhisEnrollmentUtils;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */
@PerActivity
@Module
public class TeiProgramListModule {


    private final TeiProgramListContract.View view;
    private final String teiUid;

    TeiProgramListModule(TeiProgramListContract.View view, String teiUid) {
        this.view = view;
        this.teiUid = teiUid;
    }

    @Provides
    @PerActivity
    TeiProgramListContract.View provideView(TeiProgramListActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    TeiProgramListContract.Presenter providesPresenter(TeiProgramListContract.Interactor interactor,
                                                       PreferenceProvider preferenceProvider,
                                                       AnalyticsHelper analyticsHelper,
                                                       DhisEnrollmentUtils dhisEnrollmentUtils) {
        return new TeiProgramListPresenter(view, interactor, teiUid, preferenceProvider, analyticsHelper, dhisEnrollmentUtils);
    }

    @Provides
    @PerActivity
    TeiProgramListContract.Interactor provideInteractor(@NonNull TeiProgramListRepository teiProgramListRepository) {
        return new TeiProgramListInteractor(teiProgramListRepository);
    }

    @Provides
    @PerActivity
    TeiProgramListAdapter provideProgramEventDetailAdapter(TeiProgramListContract.Presenter presenter) {
        return new TeiProgramListAdapter(presenter);
    }

    @Provides
    @PerActivity
    TeiProgramListRepository eventDetailRepository(D2 d2) {
        return new TeiProgramListRepositoryImpl(d2);
    }
}
