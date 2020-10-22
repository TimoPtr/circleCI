/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.models

import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import java.nio.ByteBuffer
import org.junit.Before
import org.junit.Test

/** [PlaqlessBrushHeadVbo] tests */
class PlaqlessBrushHeadVboTest : BaseUnitTest() {

    private lateinit var plaqlessBrushHeadVbo: PlaqlessBrushHeadVbo

    @Before
    fun before() {
        val fakeBuffer = ByteBuffer.allocate(4).asFloatBuffer()
        plaqlessBrushHeadVbo = spy(
            PlaqlessBrushHeadVbo(
                fakeBuffer,
                fakeBuffer
            )
        )
    }

    @Test
    fun `setLedColor calls setMaterialColor(LED, color)`() {
        val color = 10

        plaqlessBrushHeadVbo.setLedColor(color)

        verify(plaqlessBrushHeadVbo).setMaterialColor(PlaqlessBrushHeadMaterial.LED, color)
    }
}
