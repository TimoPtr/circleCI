/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.di;

import android.content.Context;
import com.kolibree.bttester.ota.data.OtaMockFileDownloader;
import com.kolibree.bttester.ota.data.OtaMockGruwareManager;
import com.kolibree.sdkws.api.gruware.GruwareManager;
import com.kolibree.sdkws.api.gruware.GruwareRepositoryImpl;
import com.kolibree.sdkws.core.GruwareRepository;
import dagger.Module;
import dagger.Provides;

@Module
public class BtTesterGruWareModule {

  @Provides
  public GruwareManager providesGruWareManager(Context context) {
    return new OtaMockGruwareManager(context);
  }

  @Provides
  public GruwareRepository providesGruWareRepository(Context context, GruwareManager manager) {
    return new GruwareRepositoryImpl(manager, new OtaMockFileDownloader(context));
  }
}
