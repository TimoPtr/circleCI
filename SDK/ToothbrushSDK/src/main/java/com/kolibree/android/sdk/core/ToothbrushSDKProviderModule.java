package com.kolibree.android.sdk.core;

import com.kolibree.android.sdk.dagger.ToothbrushSDKScope;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import java.util.HashSet;
import java.util.Set;

@Module
public abstract class ToothbrushSDKProviderModule {
  @Provides
  @ToothbrushSDKScope
  static Set<BackgroundJobManager> primeEmptyBackgroundJobManager() {
    return new HashSet<>();
  }

  @Binds
  @ToothbrushSDKScope
  abstract ServiceProvider providesServiceProvider(ServiceProviderImpl impl);

  @Binds
  @ToothbrushSDKScope
  abstract KLTBConnectionProvider providesKLTBConnectionProviderImpl(
      KLTBConnectionProviderImpl impl);
}
