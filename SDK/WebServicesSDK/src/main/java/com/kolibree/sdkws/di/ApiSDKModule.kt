/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.sdkws.di

import com.kolibree.android.accountinternal.account.AccountInternalModule
import com.kolibree.android.network.NetworkModule
import com.kolibree.sdkws.amazondrs.sync.AmazonDrsSynchronizationModule
import com.kolibree.sdkws.core.CoreModule
import com.kolibree.sdkws.core.feature.MarkAccountAsBetaFeatureToggleModule
import com.kolibree.sdkws.data.database.ApiSDKDatabaseModule
import com.kolibree.sdkws.room.ApiRoomModule
import com.kolibree.sdkws.utils.ApiUtilsModule
import dagger.Module

/** Created by miguelaragues on 6/3/18.  */
@Module(includes = [
    ApiUtilsModule::class,
    CoreModule::class,
    WSRepositoriesModule::class,
    ApiRoomModule::class,
    ApiSdkBindingModule::class,
    ApiSDKDatabaseModule::class,
    NetworkModule::class,
    AccountInternalModule::class,
    AmazonDrsSynchronizationModule::class,
    MarkAccountAsBetaFeatureToggleModule::class])
object ApiSDKModule
