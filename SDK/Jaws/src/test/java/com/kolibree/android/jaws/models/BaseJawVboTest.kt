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
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.nio.ByteBuffer
import org.junit.Before
import org.junit.Test

/** [BaseJawVbo] tests */
class BaseJawVboTest : BaseUnitTest() {

    private lateinit var baseJawVbo: BaseJawVbo

    @Before
    fun before() {
        val fakeBuffer = ByteBuffer.allocate(4).asFloatBuffer()
        baseJawVbo = spy(
            BaseJawVbo(
                fakeBuffer,
                fakeBuffer,
                mapOf()
            )
        )
    }

    @Test
    fun `setMouthZoneColors calls setMaterialColor(map)`() {
        doNothing().whenever(baseJawVbo).setMaterialColor(any())

        baseJawVbo.setMouthZoneColors(mapOf())

        verify(baseJawVbo).setMaterialColor(any())
    }
}
