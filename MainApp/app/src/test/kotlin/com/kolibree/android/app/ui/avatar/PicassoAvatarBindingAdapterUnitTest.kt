/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.avatar

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.extensions.sanitizedUrl
import junit.framework.TestCase.assertNull
import org.junit.Test

class PicassoAvatarBindingAdapterUnitTest : BaseUnitTest() {

    @Test
    fun `when string is null, sanitized avatar returns null`() {
        val nullString = null
        assertNull(nullString.sanitizedUrl())
    }

    @Test
    fun `when string is empty, sanitized avatar returns null`() {
        arrayOf("", " ", "\t").forEach { emptyString ->
            assertNull(emptyString.sanitizedUrl())
        }
    }
}
