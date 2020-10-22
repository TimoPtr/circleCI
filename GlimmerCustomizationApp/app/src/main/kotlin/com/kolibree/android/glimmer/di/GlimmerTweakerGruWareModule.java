/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.di;

import android.content.Context;
import com.kolibree.sdkws.api.gruware.GruwareManager;
import com.kolibree.sdkws.api.gruware.GruwareRepositoryImpl;
import com.kolibree.sdkws.core.GruwareRepository;
import dagger.Module;
import dagger.Provides;

@Module
public class GlimmerTweakerGruWareModule {

  @Provides
  public GruwareManager providesGruWareManager(Context context) {
    return new OtaMockGruwareManager(context);
  }

  @Provides
  public GruwareRepository providesGruWareRepository(Context context, GruwareManager manager) {
    return new GruwareRepositoryImpl(manager, new OtaMockFileDownloader(context));
  }
}
