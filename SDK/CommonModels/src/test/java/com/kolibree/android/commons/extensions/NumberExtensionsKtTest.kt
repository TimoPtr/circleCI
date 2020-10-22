/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.extensions

import junit.framework.TestCase
import org.junit.Test

class NumberExtensionsKtTest {

    /*
    zeroIfNan
     */
    @Test
    fun `zeroIfNan returns zero for Double Nan`() {
        TestCase.assertEquals(0.0, Double.NaN.zeroIfNan())
    }

    @Test
    fun `zeroIfNan returns same number for any other Double`() {
        TestCase.assertEquals(1.0, 1.0.zeroIfNan())
        TestCase.assertEquals(67654.0, 67654.0.zeroIfNan())
    }
}
