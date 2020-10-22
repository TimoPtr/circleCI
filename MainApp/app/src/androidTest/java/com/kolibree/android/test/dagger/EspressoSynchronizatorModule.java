package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.synchronization.SynchronizationStateUseCase;
import com.kolibree.android.synchronizator.Synchronizator;
import com.kolibree.android.synchronizator.models.BundleCreator;
import com.kolibree.sdkws.account.AccountApi;
import com.kolibree.sdkws.profile.ProfileApi;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.Multibinds;
import io.reactivex.Completable;
import java.util.Set;

@SuppressWarnings("KotlinInternalInJava")
@Module
public abstract class EspressoSynchronizatorModule {
  @Provides
  static Synchronizator providesSynchronizator() {
    Synchronizator mock = mock(Synchronizator.class);
    when(mock.delaySynchronizeCompletable()).thenReturn(Completable.complete());
    return mock;
  }

  @Provides
  static AccountApi providesAccountApiService() {
    return mock(AccountApi.class);
  }

  @Provides
  static ProfileApi providesProfileApi() {
    return mock(ProfileApi.class);
  }

  @Provides
  @AppScope
  static SynchronizationStateUseCase providesOngoingSynchronizationUseCase() {
    return mock(SynchronizationStateUseCase.class);
  }

  @Multibinds
  abstract Set<BundleCreator> bundleCreatorSet();
}
