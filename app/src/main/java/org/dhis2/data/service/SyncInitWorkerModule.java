package org.dhis2.data.service;

import org.dhis2.data.dagger.PerService;
import org.hisp.dhis.android.core.D2;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 24/10/2018.
 */

@Module
@PerService
public class SyncInitWorkerModule {

    @Provides
    @PerService
    SyncPresenter getSyncPresenter(@Nonnull D2 d2) {
        return new SyncPresenterImpl(d2);
    }

}
