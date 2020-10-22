/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.di;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import com.kolibree.android.accountinternal.account.AccountInternalModule;
import com.kolibree.android.network.NetworkModule;
import com.kolibree.sdkws.core.CoreModule;
import com.kolibree.sdkws.core.OnUserLoggedInCallback;
import com.kolibree.sdkws.data.database.ApiSDKDatabaseModule;
import com.kolibree.sdkws.di.ApiSdkBindingModule;
import com.kolibree.sdkws.room.ApiRoomModule;
import com.kolibree.sdkws.utils.ApiUtilsModule;
import dagger.Module;
import dagger.Provides;

@Keep
@Module(
    includes = {
      ApiUtilsModule.class,
      CoreModule.class,
      GlimmerTweakerWSRepositoriesModule.class,
      ApiRoomModule.class,
      ApiSdkBindingModule.class,
      ApiSDKDatabaseModule.class,
      NetworkModule.class,
      AccountInternalModule.class
    })
public abstract class GlimmerTweakerApiSDKModule {
  @Nullable
  @Provides
  static OnUserLoggedInCallback providesOnUserLoggedInCallback() {
    return null;
  }
}
