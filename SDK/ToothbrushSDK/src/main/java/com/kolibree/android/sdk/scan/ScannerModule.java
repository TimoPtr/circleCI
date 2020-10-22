package com.kolibree.android.sdk.scan;

import dagger.Binds;
import dagger.Module;

/** Created by miguelaragues on 21/9/17. */
@Module
public abstract class ScannerModule {

  @Binds
  abstract ToothbrushScannerFactory providesToothbrushScannerFactory(
      ToothbrushScannerFactoryImpl toothbrushScannerFactory);
}
