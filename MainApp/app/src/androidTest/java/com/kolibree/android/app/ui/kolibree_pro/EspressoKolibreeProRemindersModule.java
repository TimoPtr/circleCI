/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.kolibree_pro;

import static org.mockito.Mockito.mock;

import com.kolibree.android.app.dagger.AppScope;
import dagger.Module;
import dagger.Provides;

/** Created by miguelaragues on 5/2/18. */
@Module
public abstract class EspressoKolibreeProRemindersModule {

  @Provides
  @AppScope
  public static KolibreeProReminders providesKolibreeProReminders() {
    return mock(KolibreeProReminders.class);
  }
}
