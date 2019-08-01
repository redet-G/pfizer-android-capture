package org.dhis2.usescases.sync;

import org.dhis2.usescases.general.AbstractActivityContracts;

public class SyncContracts {

    public interface View extends AbstractActivityContracts.View{

        void saveTheme(Integer themeId);

        void saveFlag(String s);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init(View view);

        void syncMeta(int seconds, String scheduleTag);

        void syncReservedValues();

        void syncData(int seconds, String scheduleTag);

        void getTheme();
    }
}
