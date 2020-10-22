package com.kolibree.android.app.dagger;

import static org.mockito.Mockito.mock;

import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository;
import dagger.Module;
import dagger.Provides;

/** Created by miguelaragues on 14/9/17. */
@Module
abstract class EspressoRepositoriesModule {

  @Provides
  @AppScope
  static BrushingsRepository providesBrushingsRepository() {
    return mock(BrushingsRepository.class);
  }
}
