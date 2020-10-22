/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.di;

import com.kolibree.android.network.environment.EnvironmentManagerModule;
import com.kolibree.android.network.retrofit.RetrofitModule;
import com.kolibree.sdkws.account.AccountNetworkModule;
import com.kolibree.sdkws.brushing.BrushingModule;
import com.kolibree.sdkws.data.model.gopirate.GoPirateDao;
import com.kolibree.sdkws.data.model.gopirate.GoPirateDatastore;
import com.kolibree.sdkws.internal.OfflineUpdateDao;
import com.kolibree.sdkws.internal.OfflineUpdateDatastore;
import com.kolibree.sdkws.profile.ProfileNetworkModule;
import dagger.Binds;
import dagger.Module;

@Module(
    includes = {
      RetrofitModule.class,
      GlimmerTweakerGruWareModule.class,
      AccountNetworkModule.class,
      ProfileNetworkModule.class,
      BrushingModule.class,
      EnvironmentManagerModule.class
    })
public abstract class GlimmerTweakerWSRepositoriesModule {

  @Binds
  abstract OfflineUpdateDatastore bindsOfflineUpdateDatastore(
      OfflineUpdateDao offlineUpdateDatastore);

  @Binds
  abstract GoPirateDatastore bindsGoPirateDatastore(GoPirateDao goPirateDao);
}
