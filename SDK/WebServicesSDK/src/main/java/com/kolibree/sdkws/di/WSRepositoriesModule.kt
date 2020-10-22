package com.kolibree.sdkws.di

import com.kolibree.android.network.environment.EnvironmentManagerModule
import com.kolibree.android.network.retrofit.RetrofitModule
import com.kolibree.sdkws.account.AccountNetworkModule
import com.kolibree.sdkws.api.gruware.GruWareModule
import com.kolibree.sdkws.brushing.BrushingModule
import com.kolibree.sdkws.data.model.gopirate.GoPirateDao
import com.kolibree.sdkws.data.model.gopirate.GoPirateDatastore
import com.kolibree.sdkws.internal.OfflineUpdateDao
import com.kolibree.sdkws.internal.OfflineUpdateDatastore
import com.kolibree.sdkws.profile.ProfileNetworkModule
import com.kolibree.sdkws.push.PushNotificationApiModule
import dagger.Binds
import dagger.Module

@Module(
    includes = [RetrofitModule::class,
        GruWareModule::class,
        AccountNetworkModule::class,
        PushNotificationApiModule::class,
        ProfileNetworkModule::class,
        BrushingModule::class,
        EnvironmentManagerModule::class]
)
abstract class WSRepositoriesModule {
    @Binds
    internal abstract fun bindsOfflineUpdateDatastore(
        offlineUpdateDatastore: OfflineUpdateDao
    ): OfflineUpdateDatastore

    @Binds
    internal abstract fun bindsGoPirateDatastore(goPirateDao: GoPirateDao): GoPirateDatastore
}
