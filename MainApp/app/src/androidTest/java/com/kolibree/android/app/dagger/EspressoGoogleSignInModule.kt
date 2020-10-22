/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.app.dagger

import com.kolibree.android.google.auth.GoogleSignInWrapper
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module
object EspressoGoogleSignInModule {

    val wrapperMock: GoogleSignInWrapper = mock()

    @Provides
    fun providesGoogleSignInWrapper(): GoogleSignInWrapper = wrapperMock
}
