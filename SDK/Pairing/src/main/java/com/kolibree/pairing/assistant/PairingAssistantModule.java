package com.kolibree.pairing.assistant;

import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCaseModule;
import com.kolibree.pairing.session.PairingSessionCreator;
import com.kolibree.pairing.session.PairingSessionCreatorImpl;
import dagger.Binds;
import dagger.Module;

@Module(includes = SynchronizeBrushingModeUseCaseModule.class)
public abstract class PairingAssistantModule {

  @Binds
  abstract PairingAssistant bindsPairingAssistant(PairingAssistantImpl pairingAssistant);

  @Binds
  abstract PairingSessionCreator bindsPairingSessionCreator(
      PairingSessionCreatorImpl implementation);
}
