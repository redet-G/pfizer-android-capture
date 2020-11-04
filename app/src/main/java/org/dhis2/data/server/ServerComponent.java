package org.dhis2.data.server;

import androidx.annotation.NonNull;

import dhis2.org.analytics.charts.Charts;
import org.dhis2.data.dagger.PerServer;
import org.dhis2.data.user.UserComponent;
import org.dhis2.data.user.UserModule;
import org.dhis2.utils.category.CategoryDialogComponent;
import org.dhis2.utils.category.CategoryDialogModule;
import org.dhis2.utils.customviews.CategoryComboDialogComponent;
import org.dhis2.utils.customviews.CategoryComboDialogModule;
import org.dhis2.utils.granularsync.GranularSyncComponent;
import org.dhis2.utils.granularsync.GranularSyncModule;

import dagger.Subcomponent;

@PerServer
@Subcomponent(modules = {ServerModule.class})
public interface ServerComponent extends Charts.Dependencies {

    @NonNull
    UserManager userManager();

    @NonNull
    UserComponent plus(@NonNull UserModule userModule);

    @NonNull
    GranularSyncComponent plus(@NonNull GranularSyncModule granularSyncModule);

    @NonNull
    CategoryComboDialogComponent plus(@NonNull CategoryComboDialogModule categoryComboDialogModule);

    @NonNull
    CategoryDialogComponent plus(@NonNull CategoryDialogModule categoryDialogModule);

}
