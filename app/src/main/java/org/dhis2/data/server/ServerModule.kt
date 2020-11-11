package org.dhis2.data.server

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.DhisAnalyticCharts
import java.util.ArrayList
import okhttp3.Interceptor
import org.dhis2.App
import org.dhis2.BuildConfig
import org.dhis2.data.dagger.PerServer
import org.dhis2.data.prefs.PreferenceProviderImpl
import org.dhis2.utils.RulesUtilsProvider
import org.dhis2.utils.RulesUtilsProviderImpl
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.AnalyticsInterceptor
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager

@Module
@PerServer
class ServerModule {
    @Provides
    @PerServer
    fun sdk(): D2 {
        return D2Manager.getD2()
    }

    @Provides
    @PerServer
    fun configurationRepository(d2: D2?): UserManager {
        return UserManagerImpl(d2!!)
    }

    @Provides
    @PerServer
    fun dataBaseExporter(d2: D2?): DataBaseExporter {
        return DataBaseExporterImpl(d2)
    }

    @Provides
    @PerServer
    fun rulesUtilsProvider(d2: D2?): RulesUtilsProvider {
        return RulesUtilsProviderImpl(d2!!)
    }

    @Provides
    @PerServer
    fun provideCharts(serverComponent: ServerComponent): Charts {
        return DhisAnalyticCharts.Provider.get(serverComponent)
    }

    companion object {
        @JvmStatic
        fun getD2Configuration(context: Context): D2Configuration {
            val interceptors: MutableList<Interceptor> =
                ArrayList()
            interceptors.add(StethoInterceptor())
            interceptors.add(
                AnalyticsInterceptor(
                    AnalyticsHelper(
                        FirebaseAnalytics.getInstance(context),
                        PreferenceProviderImpl(context),
                        (context as App).appComponent().matomoController()
                    )
                )
            )
            return D2Configuration.builder()
                .appName(BuildConfig.APPLICATION_ID)
                .appVersion(BuildConfig.VERSION_NAME)
                .connectTimeoutInSeconds(10 * 60)
                .readTimeoutInSeconds(10 * 60)
                .networkInterceptors(interceptors)
                .writeTimeoutInSeconds(10 * 60)
                .context(context)
                .build()
        }
    }
}
