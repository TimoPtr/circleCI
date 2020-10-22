package com.kolibree.android.app.dagger;

import com.kolibree.android.network.environment.DefaultEnvironment;
import com.kolibree.android.network.environment.Environment;
import dagger.Module;
import dagger.Provides;

@Module(includes = {FlipperModule.class})
public abstract class VariantModule {

  @Provides
  static DefaultEnvironment providesDefaultEnvironment() {
    return new DefaultEnvironment(Environment.STAGING);
  }
}
