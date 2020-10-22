package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;

import com.kolibree.android.sdk.core.BackgroundJobManager;
import com.kolibree.android.sdk.core.KLTBConnectionProvider;
import com.kolibree.android.sdk.core.ServiceProvider;
import com.kolibree.android.sdk.dagger.ToothbrushSDKScope;
import dagger.Module;
import dagger.Provides;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("KotlinInternalInJava")
@Module
class EspressoToothbrushSDKProviderModule {

  private final ServiceProvider serviceProvider = mock(ServiceProvider.class);
  private final KLTBConnectionProvider kltbConnectionProvider = mock(KLTBConnectionProvider.class);

  @Provides
  ServiceProvider providesServiceProvider() {
    return serviceProvider;
  }

  @Provides
  KLTBConnectionProvider providesKLTBConnectionProvider() {
    return kltbConnectionProvider;
  }

  @Provides
  @ToothbrushSDKScope
  static Set<BackgroundJobManager> primeEmptyBackgroundJobManager() {
    return new HashSet<>();
  }
}
