/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.data.database;

import com.kolibree.android.commons.interfaces.Truncable;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

/** Created by miguelaragues on 7/3/18. */
@Module
public abstract class ApiSDKDatabaseModule {

  @Binds
  @IntoSet
  abstract Truncable bindsApiSDKDatabase(ApiSDKDatabaseInteractorImpl apiSDKDatabase);
}
