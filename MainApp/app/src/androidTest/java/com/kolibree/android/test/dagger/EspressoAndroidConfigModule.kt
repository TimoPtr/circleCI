/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger

import com.kolibree.android.jaws.coach.AndroidConfigModule
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object EspressoAndroidConfigModule {

    @Provides
    @Named(AndroidConfigModule.NamedKey.EXECUTE_GL_CONFIG_CHANGES)
    fun provideExecuteGlConfigChangesFlag(): Boolean = false
}
