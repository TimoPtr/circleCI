/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.legacy;

import com.kolibree.bttester.di.scopes.FragmentScope;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/** Created by miguelaragues on 23/11/17. */
@Module
public abstract class PairDialogFragmentModule {

  @FragmentScope
  @ContributesAndroidInjector
  abstract PairDialogFragment contributePairDialogFragment();
}
