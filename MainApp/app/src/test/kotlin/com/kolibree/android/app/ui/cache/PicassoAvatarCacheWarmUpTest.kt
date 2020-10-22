/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.cache

import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Test

class PicassoAvatarCacheWarmUpTest : BaseUnitTest() {

    private val avatarCacheWarmUp = PicassoAvatarCacheWarmUp()

    @Test
    fun cacheWarmUp_withNullPicture_doesNothing() {
        avatarCacheWarmUp.cache(null)
    }

    @Test
    fun cacheWarmUp_withEmptyPicture_doesNothing() {
        avatarCacheWarmUp.cache("")
    }

    /*
    We can't mock Picasso, so let's assert that there was an initialization exception
   */
    @Test(expected = IllegalStateException::class)
    fun cacheWarmUp_withPicture_invokesLoadAsyncInPicasso() {
        val url = "da"
        avatarCacheWarmUp.cache(url)
    }
}
