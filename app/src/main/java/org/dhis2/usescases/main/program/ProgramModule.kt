package org.dhis2.usescases.main.program

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.dhislogic.DhisTrackedEntityInstanceUtils
import org.dhis2.data.filter.FilterController
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2

@Module
@PerFragment
class ProgramModule(private val view: ProgramView) {

    @Provides
    @PerFragment
    internal fun programPresenter(
        homeRepository: HomeRepository,
        schedulerProvider: SchedulerProvider,
        preferenceProvider: PreferenceProvider,
        filterManager: FilterManager
    ): ProgramPresenter {
        return ProgramPresenter(
            view,
            homeRepository,
            schedulerProvider,
            preferenceProvider,
            filterManager
        )
    }

    @Provides
    @PerFragment
    internal fun homeRepository(
        d2: D2,
        filterController: FilterController,
        dhisProgramUtils: DhisProgramUtils,
        dhisTrackedEntityInstanceUtils: DhisTrackedEntityInstanceUtils,
        schedulerProvider: SchedulerProvider,
        resourceManager: ResourceManager
    ): HomeRepository {
        return HomeRepositoryImpl(
            d2,
            filterController,
            dhisProgramUtils,
            dhisTrackedEntityInstanceUtils,
            resourceManager,
            schedulerProvider
        )
    }

    @Provides
    @PerFragment
    internal fun providesAdapter(presenter: ProgramPresenter): ProgramModelAdapter {
        return ProgramModelAdapter(presenter)
    }

    @Provides
    @PerFragment
    fun provideAnimations(): ProgramAnimation {
        return ProgramAnimation()
    }
}
