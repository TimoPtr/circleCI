package com.kolibree.pairing;

import com.kolibree.pairing.assistant.PairingAssistantModule;
import com.kolibree.pairing.usecases.UpdateToothbrushUseCase;
import com.kolibree.pairing.usecases.UpdateToothbrushUseCaseImpl;
import dagger.Binds;
import dagger.Module;

@Module(
    includes = {
      PairingAssistantModule.class,
    })
public abstract class PairingModule {

  @Binds
  public abstract UpdateToothbrushUseCase bindUpdateToothbrushUseCase(
      UpdateToothbrushUseCaseImpl impl);
}
