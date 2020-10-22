/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger;

import com.kolibree.android.sdk.scan.ToothbrushScanner;
import com.kolibree.android.sdk.scan.ToothbrushScannerFactory;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import org.mockito.Mockito;

/** Created by miguelaragues on 21/9/17. */
@Module
public class EspressoSdkScannerModule {

  public static final String BLE_SCANNER = "BLE_SCANNER";
  private final ToothbrushScanner bleScanner;

  public EspressoSdkScannerModule() {
    this.bleScanner = Mockito.mock(ToothbrushScanner.class);
  }

  @Provides
  @Named(BLE_SCANNER)
  ToothbrushScanner providesBleToothbrushScanner() {
    return bleScanner;
  }

  @Provides
  ToothbrushScannerFactory providesToothbrushScannerFactory(
      @Named(BLE_SCANNER) ToothbrushScanner bleScanner) {
    ToothbrushScannerFactory scannerFactory = Mockito.mock(ToothbrushScannerFactory.class);

    Mockito.when(scannerFactory.getCompatibleBleScanner()).thenReturn(bleScanner);

    return scannerFactory;
  }
}
