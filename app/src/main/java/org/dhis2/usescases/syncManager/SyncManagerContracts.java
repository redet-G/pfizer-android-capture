package org.dhis2.usescases.syncManager;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.maintenance.D2Error;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by lmartin on 21/03/2018.
 */

public class SyncManagerContracts {

    interface View extends AbstractActivityContracts.View {

        Consumer<Pair<Integer, Integer>> setSyncData();

        void wipeDatabase();

        void showTutorial();

        void showSyncErrors(List<D2Error> data);
    }

    public interface Presenter {

        void init(SyncManagerContracts.View view);

        void syncData(int seconds, String scheduleTag);

        void syncMeta(int seconds, String scheduleTag);

        void syncData();

        void syncMeta();

        void disponse();

        void resetSyncParameters();

        void onWipeData();

        void wipeDb();

        void deleteLocalData();

        void onReservedValues();

        void checkSyncErrors();

        void checkData();

        void cancelPendingWork(String meta);
    }
}
