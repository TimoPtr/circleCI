/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package android.bluetooth;

import com.kolibree.android.sdk.bluetooth.BluetoothAdapterWrapper;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import org.mockito.Mockito;

/**
 * This class lives in android.bluetooth.le package so that we can instantiate the final class
 * BluetoothLeScanner
 *
 * <p>Created by miguelaragues on 19/9/17.
 */
@SuppressWarnings("KotlinInternalInJava")
@Module
public class EspressoSdkBluetoothModule {

  /*
  I can't use the singleton scope that I need for testing, so I'm manually returning the same instance
   */
  private final BluetoothAdapterWrapper bluetoothAdapter =
      Mockito.mock(BluetoothAdapterWrapper.class);

  public EspressoSdkBluetoothModule() {}

  @Provides
  Scheduler provideScheduler() {
    return Mockito.mock(Scheduler.class);
  }

  @Provides
  BluetoothAdapterWrapper providesBluetoothAdapterWrapper() {
    return bluetoothAdapter;
  }
}
