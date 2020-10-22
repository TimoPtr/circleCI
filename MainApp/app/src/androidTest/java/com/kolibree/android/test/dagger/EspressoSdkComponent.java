/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger;

import static com.kolibree.android.test.dagger.EspressoSdkScannerModule.BLE_SCANNER;

import android.bluetooth.EspressoSdkBluetoothModule;
import android.content.Context;
import com.kolibree.android.app.dagger.CommonsAndroidModule;
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase;
import com.kolibree.android.sdk.core.BackgroundJobManager;
import com.kolibree.android.sdk.dagger.SdkComponent;
import com.kolibree.android.sdk.dagger.ToothbrushSDKScope;
import com.kolibree.android.sdk.dagger.ToothbrushSdkFeatureToggles;
import com.kolibree.android.sdk.location.LocationStatusListener;
import com.kolibree.android.sdk.persistence.repo.EspressoSdkDatabaseModule;
import com.kolibree.android.sdk.scan.ToothbrushScanner;
import dagger.BindsInstance;
import dagger.Component;
import javax.inject.Named;

/** Created by miguelaragues on 19/9/17. */
@SuppressWarnings("KotlinInternalInJava")
@ToothbrushSDKScope
@Component(
    modules = {
      EspressoSdkBluetoothModule.class,
      EspressoSdkDatabaseModule.class,
      EspressoSdkUtilsModule.class,
      EspressoSdkScannerModule.class,
      EspressoToothbrushSDKProviderModule.class,
      EspressoToothbrushSDKConnectionModule.class,
      CommonsAndroidModule.class,
      EspressoPlaqlessModule.class,
      ToothbrushSdkFeatureToggles.class
    })
public interface EspressoSdkComponent extends SdkComponent {

  @Named(BLE_SCANNER)
  ToothbrushScanner toothbrushBleScanner();

  LocationStatusListener locationStatusListener();

  CheckConnectionPrerequisitesUseCase connectionPrerequisitesUseCase();

  @Component.Builder
  interface Builder {

    EspressoSdkComponent build();

    @BindsInstance
    EspressoSdkComponent.Builder context(Context context);

    @BindsInstance
    EspressoSdkComponent.Builder backgroundJobManager(BackgroundJobManager backgroundJobManager);
  }
}
