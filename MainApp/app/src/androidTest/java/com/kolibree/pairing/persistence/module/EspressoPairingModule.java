package com.kolibree.pairing.persistence.module;

import static org.mockito.Mockito.mock;

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.pairing.assistant.PairingAssistant;
import com.kolibree.pairing.usecases.UpdateToothbrushUseCase;
import com.kolibree.pairing.usecases.UpdateToothbrushUseCaseImpl;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("KotlinInternalInJava")
@Module
public abstract class EspressoPairingModule {

  @Provides
  @AppScope
  static PairingAssistant providesPairingAssistant() {
    return mock(PairingAssistant.class);
  }

  @Binds
  public abstract UpdateToothbrushUseCase bindUpdateToothbrushUseCase(
      UpdateToothbrushUseCaseImpl impl);
}
