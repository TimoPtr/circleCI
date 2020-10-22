package com.kolibree.android.test.dagger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kolibree.android.app.ui.settings.secret.persistence.repo.ModelsAvailableRepository;
import dagger.Module;
import dagger.Provides;

/** Created by Guillaume Agis on 12/11/2018. */
@Module
public class EspressoModelsAvailableModule {

  @SuppressWarnings("KotlinInternalInJava")
  @Provides
  static ModelsAvailableRepository bindsModelsAvailableRepository() {
    ModelsAvailableRepository mock = mock(ModelsAvailableRepository.class);
    when(mock.isAvailable(any())).thenReturn(true);
    return mock;
  }
}
