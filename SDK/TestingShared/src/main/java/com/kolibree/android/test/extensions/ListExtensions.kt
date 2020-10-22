/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.extensions

import androidx.annotation.Keep
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue

@Keep
fun <E : Any> List<E>.assertContainsExclusively(other: List<E>) {
    assertEquals(size, other.size)

    assertTrue(containsAll(other))
}
