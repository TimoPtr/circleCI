package com.kolibree.android.sdk.util;

import android.content.Context;
import com.kolibree.android.app.dagger.ApplicationContext;
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase;
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCaseImpl;
import com.kolibree.android.sdk.location.LocationStatusListener;
import com.kolibree.android.sdk.location.LocationStatusListenerImpl;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/** Created by miguelaragues on 19/9/17. */
@Module
public abstract class UtilsModule {
  private static final Object locationStatusListenerLock = new Object();
  private static volatile LocationStatusListener locationStatusListener;

  @Binds
  abstract IBluetoothUtils bindsBluetoothUtils(BluetoothUtilsImpl bluetoothUtils);

  @Provides
  static ApplicationContext providesApplicationContext(Context context) {
    return new ApplicationContext(context);
  }

  @Provides
  static LocationStatusListener bindsLocationStatusListener(LocationStatusListenerImpl impl) {
    LocationStatusListener localField = locationStatusListener;
    if (localField == null) {
      synchronized (locationStatusListenerLock) {
        localField = locationStatusListener;
        if (localField == null) {
          locationStatusListener = localField = impl;
        }
      }
    }

    return localField;
  }

  @Binds
  public abstract CheckConnectionPrerequisitesUseCase bindCheckConnectionPrerequisitesUseCase(
      CheckConnectionPrerequisitesUseCaseImpl impl);
}
