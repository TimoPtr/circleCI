/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.renderer

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.jaws.Kolibree3DModel
import com.kolibree.android.jaws.MemoryManagerInternal
import com.kolibree.android.jaws.coach.animation.PlaqlessBrushHeadAnimationMapper
import com.kolibree.android.jaws.coach.animation.RegularBrushHeadAnimationMapper
import com.kolibree.android.jaws.coach.brushhead.PlaqlessBrushHeadPositionMapper
import com.kolibree.android.jaws.coach.brushhead.RegularBrushHeadPositionMapper
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.isA
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

/** [CoachPlusRendererFactoryImpl] tests */
class CoachPlusRendererFactoryImplTest : BaseUnitTest() {

    private val memoryManager = mock<MemoryManagerInternal>()

    private lateinit var rendererFactory: CoachPlusRendererFactoryImpl

    override fun setup() {
        super.setup()

        rendererFactory = spy(CoachPlusRendererFactoryImpl(memoryManager))
    }

    @Test
    fun `createCoachPlusRenderer with PLAQLESS creates a plaqless renderer`() {
        rendererFactory.createCoachPlusRenderer(ToothbrushModel.PLAQLESS)
        verify(rendererFactory).createCoachPlusRenderer(
            eq(Kolibree3DModel.PLAQLESS),
            isA<PlaqlessBrushHeadPositionMapper>(),
            isA<PlaqlessBrushHeadAnimationMapper>()
        )
    }

    @Test
    fun `createCoachPlusRenderer with ARA creates a regular renderer`() {
        rendererFactory.createCoachPlusRenderer(ToothbrushModel.ARA)
        verify(rendererFactory).createCoachPlusRenderer(
            eq(Kolibree3DModel.TOOTHBRUSH),
            isA<RegularBrushHeadPositionMapper>(),
            isA<RegularBrushHeadAnimationMapper>()
        )
    }

    @Test
    fun `createCoachPlusRenderer with E1 creates a regular renderer`() {
        rendererFactory.createCoachPlusRenderer(ToothbrushModel.CONNECT_E1)
        verify(rendererFactory).createCoachPlusRenderer(
            eq(Kolibree3DModel.TOOTHBRUSH),
            isA<RegularBrushHeadPositionMapper>(),
            isA<RegularBrushHeadAnimationMapper>()
        )
    }

    @Test
    fun `createCoachPlusRenderer with E2 creates a regular renderer`() {
        rendererFactory.createCoachPlusRenderer(ToothbrushModel.CONNECT_E2)
        verify(rendererFactory).createCoachPlusRenderer(
            eq(Kolibree3DModel.TOOTHBRUSH),
            isA<RegularBrushHeadPositionMapper>(),
            isA<RegularBrushHeadAnimationMapper>()
        )
    }

    @Test
    fun `createCoachPlusRenderer with M1 creates a regular renderer`() {
        rendererFactory.createCoachPlusRenderer(ToothbrushModel.CONNECT_M1)
        verify(rendererFactory).createCoachPlusRenderer(
            eq(Kolibree3DModel.TOOTHBRUSH),
            isA<RegularBrushHeadPositionMapper>(),
            isA<RegularBrushHeadAnimationMapper>()
        )
    }

    @Test
    fun `createCoachPlusRenderer with B1 creates a regular renderer`() {
        rendererFactory.createCoachPlusRenderer(ToothbrushModel.CONNECT_B1)
        verify(rendererFactory).createCoachPlusRenderer(
            eq(Kolibree3DModel.TOOTHBRUSH),
            isA<RegularBrushHeadPositionMapper>(),
            isA<RegularBrushHeadAnimationMapper>()
        )
    }

    @Test
    fun `createCoachPlusRenderer with null creates a regular renderer`() {
        rendererFactory.createCoachPlusRenderer(null)
        verify(rendererFactory).createCoachPlusRenderer(
            eq(Kolibree3DModel.TOOTHBRUSH),
            isA<RegularBrushHeadPositionMapper>(),
            isA<RegularBrushHeadAnimationMapper>()
        )
    }
}
