package com.kolibree.android.sba.testbrushing.ui

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sba.R
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.ANALYZING_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.FINISH_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.START_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.TOOTHBRUSH_STEP
import org.junit.Assert.assertEquals
import org.junit.Test

class TestBrushingResourceProviderTest {

    private val provider = TestBrushingResourceProvider()

    /*
    PROVIDE TOOTHBRUSH VIDEO
     */

    @Test
    fun `provideToothbrushVideo E1 returns anim_step2_e1`() {
        assertEquals(R.raw.anim_step2_e1, provider.provideToothbrushVideo(CONNECT_E1))
    }

    @Test
    fun `provideToothbrushVideo M1 returns anim_m1`() {
        assertEquals(R.raw.anim_m1, provider.provideToothbrushVideo(CONNECT_M1))
    }

    @Test
    fun `provideToothbrushVideo E2 returns anim_step2_e2`() {
        assertEquals(R.raw.anim_step2_e2, provider.provideToothbrushVideo(CONNECT_E2))
    }

    @Test
    fun `provideToothbrushVideo Ara returns anim_step2_ara`() {
        assertEquals(R.raw.anim_step2_ara, provider.provideToothbrushVideo(ARA))
    }

    @Test
    fun `provideToothbrushVideo B1 returns anim_step2_b1`() {
        assertEquals(R.raw.anim_step2_b1, provider.provideToothbrushVideo(CONNECT_B1))
    }

    @Test
    fun `provideToothbrushVideo PQL returns anim_step2_pql`() {
        assertEquals(R.raw.anim_step2_pql, provider.provideToothbrushVideo(PLAQLESS))
    }

    /*
    PROVIDE TOOTHBRUSH PREVIEW IMAGE
     */

    @Test
    fun provideToothbrushPreviewImage_ara_returns_ic_ara_preview() {
        assertEquals(R.drawable.ic_ara_preview, provider.provideToothbrushPreviewImage(ARA))
    }

    @Test
    fun provideToothbrushPreviewImage_e1_returns_ic_e1_preview() {
        assertEquals(R.drawable.ic_e1_preview, provider.provideToothbrushPreviewImage(CONNECT_E1))
    }

    @Test
    fun provideToothbrushPreviewImage_e2_returns_ic_e2_preview() {
        assertEquals(R.drawable.ic_e2_preview, provider.provideToothbrushPreviewImage(CONNECT_E2))
    }

    @Test
    fun provideToothbrushPreviewImage_m1_returns_ic_m1_preview() {
        assertEquals(R.drawable.ic_m1_preview, provider.provideToothbrushPreviewImage(CONNECT_M1))
    }

    @Test
    fun provideToothbrushPreviewImage_b1_returns_ic_b1_preview() {
        assertEquals(R.drawable.ic_b1_preview, provider.provideToothbrushPreviewImage(CONNECT_B1))
    }

    @Test
    fun provideToothbrushPreviewImage_pql_returns_ic_pql_start_session() {
        assertEquals(R.drawable.ic_pql_start_session, provider.provideToothbrushPreviewImage(PLAQLESS))
    }

    /*
    PROVIDE DURING SESSION VIDEO
     */

    @Test
    fun provideDuringSessionVideo_forStep_START_STEP_1() {
        assertForModels(
            expectedAra = R.raw.anim_step1,
            expectedE1 = R.raw.anim_step1,
            expectedE2 = R.raw.anim_step1,
            expectedM1 = R.raw.anim_step1,
            expectedB1 = R.raw.anim_step1,
            expectedPQL = R.raw.anim_step1_plaqless) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionVideo(model, START_STEP))
        }
    }

    @Test
    fun provideDuringSessionVideo_forStep_FINISH_STEP_2() {
        assertForModels(
            expectedAra = R.raw.anim_step2_ara,
            expectedE1 = R.raw.anim_step2_e1,
            expectedE2 = R.raw.anim_step2_e2,
            expectedM1 = R.raw.anim_step2_m1,
            expectedB1 = R.raw.anim_step2_b1,
            expectedPQL = R.raw.anim_step2_pql) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionVideo(model, FINISH_STEP))
        }
    }

    @Test
    fun provideDuringSessionVideo_forStep_TOOTHBRUSH_STEP_3() {
        assertForModels(
            expectedAra = R.raw.anim_step3,
            expectedE1 = R.raw.anim_step3,
            expectedE2 = R.raw.anim_step3,
            expectedM1 = R.raw.anim_step3_manual,
            expectedB1 = R.raw.anim_step3,
            expectedPQL = R.raw.anim_step3) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionVideo(model, TOOTHBRUSH_STEP))
        }
    }

    @Test
    fun provideDuringSessionVideo_forStep_ANALYZING_STEP_4() {
        assertForModels(
            expectedAra = R.raw.anim_step4,
            expectedE1 = R.raw.anim_step4,
            expectedE2 = R.raw.anim_step4,
            expectedM1 = R.raw.anim_step4,
            expectedB1 = R.raw.anim_step4,
            expectedPQL = R.raw.anim_step4_plaqless) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionVideo(model, ANALYZING_STEP))
        }
    }

    /*
    PROVIDE BACKGROUND COLOR
     */

    @Test
    fun provideBackgroundColor_forStep_START_STEP_1() {
        assertForModels(
            expectedAra = R.color.white,
            expectedE1 = R.color.white,
            expectedE2 = R.color.white,
            expectedM1 = R.color.white,
            expectedB1 = R.color.white,
            expectedPQL = R.color.during_session_step_pql) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideBackgroundColor(model, START_STEP))
        }
    }

    @Test
    fun provideBackgroundColor_forStep_FINISH_STEP_2() {
        assertForModels(
            expectedAra = R.color.during_session_step2,
            expectedE1 = R.color.during_session_step2,
            expectedE2 = R.color.during_session_step2,
            expectedM1 = R.color.during_session_step2_manual,
            expectedB1 = R.color.during_session_step2,
            expectedPQL = R.color.during_session_step2) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideBackgroundColor(model, FINISH_STEP))
        }
    }

    @Test
    fun provideBackgroundColor_forStep_TOOTHBRUSH_STEP_3() {
        assertForModels(
            expectedAra = R.color.white,
            expectedE1 = R.color.white,
            expectedE2 = R.color.white,
            expectedM1 = R.color.during_session_step3_manual,
            expectedB1 = R.color.white,
            expectedPQL = R.color.white) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideBackgroundColor(model, TOOTHBRUSH_STEP))
        }
    }

    @Test
    fun provideBackgroundColor_forStep_ANALYZING_STEP_4() {
        assertForModels(
            expectedAra = R.color.white,
            expectedE1 = R.color.white,
            expectedE2 = R.color.white,
            expectedM1 = R.color.white,
            expectedB1 = R.color.white,
            expectedPQL = R.color.during_session_step_pql) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideBackgroundColor(model, ANALYZING_STEP))
        }
    }

    /**
     * PROVIDE DURING SESSION DESCRIPTION
     */

    @Test
    fun provideDuringSessionDescription_forStep_START_STEP_1() {
        assertForModels(
            expectedAra = R.string.durring_session_description_step1,
            expectedE1 = R.string.durring_session_description_step1,
            expectedE2 = R.string.durring_session_description_step1,
            expectedM1 = R.string.durring_session_description_step1,
            expectedB1 = R.string.durring_session_description_step1,
            expectedPQL = R.string.pql_durring_session_description_step1) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionDescription(model, START_STEP))
        }
    }

    @Test
    fun provideDuringSessionDescription_forStep_FINISH_STEP_2() {
        assertForModels(
            expectedAra = R.string.durring_session_description_step2,
            expectedE1 = R.string.durring_session_description_step2,
            expectedE2 = R.string.durring_session_description_step2,
            expectedM1 = R.string.durring_session_description_step2,
            expectedB1 = R.string.durring_session_description_step2,
            expectedPQL = R.string.durring_session_description_step2) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionDescription(model, FINISH_STEP))
        }
    }

    @Test
    fun provideDuringSessionDescription_forStep_TOOTHBRUSH_STEP_3() {
        assertForModels(
            expectedAra = R.string.durring_session_description_step3,
            expectedE1 = R.string.durring_session_description_step3,
            expectedE2 = R.string.durring_session_description_step3,
            expectedM1 = R.string.durring_session_description_step3_manual,
            expectedB1 = R.string.durring_session_description_step3,
            expectedPQL = R.string.durring_session_description_step3) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionDescription(model, TOOTHBRUSH_STEP))
        }
    }

    @Test
    fun provideDuringSessionDescription_forStep_ANALYZING_STEP_4() {
        assertForModels(
            expectedAra = R.string.durring_session_description_step4,
            expectedE1 = R.string.durring_session_description_step4,
            expectedE2 = R.string.durring_session_description_step4,
            expectedM1 = R.string.durring_session_description_step4,
            expectedB1 = R.string.durring_session_description_step4,
            expectedPQL = R.string.durring_session_description_step4) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionDescription(model, ANALYZING_STEP))
        }
    }

    /**
     * PROVIDE DURING SESSION HIGHLIGHTED
     */

    @Test
    fun provideDuringSessionHighlighted_forStep_START_STEP_1() {
        assertForModels(
            expectedAra = R.string.durring_session_highlighted_step1,
            expectedE1 = R.string.durring_session_highlighted_step1,
            expectedE2 = R.string.durring_session_highlighted_step1,
            expectedM1 = R.string.durring_session_highlighted_step1,
            expectedB1 = R.string.durring_session_highlighted_step1,
            expectedPQL = R.string.pql_durring_session_description_step1_highlighted) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionHighlighted(model, START_STEP))
        }
    }

    @Test
    fun provideDuringSessionHighlighted_forStep_FINISH_STEP_2() {
        assertForModels(
            expectedAra = R.string.empty,
            expectedE1 = R.string.empty,
            expectedE2 = R.string.empty,
            expectedM1 = R.string.empty,
            expectedB1 = R.string.empty,
            expectedPQL = R.string.empty) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionHighlighted(model, FINISH_STEP))
        }
    }

    @Test
    fun provideDuringSessionHighlighted_forStep_TOOTHBRUSH_STEP_3() {
        assertForModels(
            expectedAra = R.string.durring_session_highlighted_step3,
            expectedE1 = R.string.durring_session_highlighted_step3,
            expectedE2 = R.string.durring_session_highlighted_step3,
            expectedM1 = R.string.durring_session_highlighted_step3_manual,
            expectedB1 = R.string.durring_session_highlighted_step3,
            expectedPQL = R.string.durring_session_highlighted_step3) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionHighlighted(model, TOOTHBRUSH_STEP))
        }
    }

    @Test
    fun provideDuringSessionHighlighted_forStep_ANALYZING_STEP_4() {
        assertForModels(
            expectedAra = R.string.durring_session_highlighted_step4,
            expectedE1 = R.string.durring_session_highlighted_step4,
            expectedE2 = R.string.durring_session_highlighted_step4,
            expectedM1 = R.string.durring_session_highlighted_step4,
            expectedB1 = R.string.durring_session_highlighted_step4,
            expectedPQL = R.string.durring_session_highlighted_step4) { model, expectedResult ->
            assertEquals(expectedResult, provider.provideDuringSessionHighlighted(model, ANALYZING_STEP))
        }
    }

    private fun assertForModels(
        expectedAra: Int,
        expectedE1: Int,
        expectedE2: Int,
        expectedM1: Int,
        expectedB1: Int,
        expectedPQL: Int,
        block: (model: ToothbrushModel, expected: Int) -> Unit
    ) {

        block.invoke(ARA, expectedAra)
        block.invoke(CONNECT_E1, expectedE1)
        block.invoke(CONNECT_E2, expectedE2)
        block.invoke(CONNECT_M1, expectedM1)
        block.invoke(CONNECT_B1, expectedB1)
        block.invoke(PLAQLESS, expectedPQL)
    }
}
