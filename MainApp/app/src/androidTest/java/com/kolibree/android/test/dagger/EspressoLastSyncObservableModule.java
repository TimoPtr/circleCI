package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.offlinebrushings.sync.LastSyncObservable;
import com.kolibree.android.offlinebrushings.sync.LastSyncObservableInternal;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("KotlinInternalInJava")
@Module
public class EspressoLastSyncObservableModule {

  @Provides
  @AppScope
  LastSyncObservable providesLastSyncObservable() {
    return mock(LastSyncObservable.class);
  }

  @Provides
  @AppScope
  LastSyncObservableInternal providesLastSyncObservableInternal() {
    return mock(LastSyncObservableInternal.class);
  }
}
