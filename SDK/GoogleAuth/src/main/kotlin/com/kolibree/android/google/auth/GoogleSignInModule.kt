/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.google.auth

import dagger.Binds
import dagger.Module

@Module(includes = [GoogleSignInImplModule::class])
class GoogleSignInModule

@Module
internal abstract class GoogleSignInImplModule {

    @Binds
    abstract fun bindsGoogleSignInWrapper(impl: GoogleSignInWrapperImpl): GoogleSignInWrapper
}
