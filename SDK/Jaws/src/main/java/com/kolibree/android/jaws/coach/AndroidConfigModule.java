/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach;

import android.annotation.SuppressLint;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;

@SuppressLint("DeobfuscatedPublicSdkClass")
@Module
public class AndroidConfigModule {

  @Provides
  @Named(NamedKey.EXECUTE_GL_CONFIG_CHANGES)
  boolean provideExecuteGlConfigChangesFlag() {
    return true;
  }

  public interface NamedKey {
    String EXECUTE_GL_CONFIG_CHANGES = "AndroidConfigModule.EXECUTE_GL_CONFIG_CHANGES";
  }
}
