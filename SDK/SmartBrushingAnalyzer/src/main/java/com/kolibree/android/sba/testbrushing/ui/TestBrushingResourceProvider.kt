package com.kolibree.android.sba.testbrushing.ui

import androidx.annotation.ColorRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.kolibree.android.app.mvi.brushstart.BrushStartResourceProvider
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.HUM_BATTERY
import com.kolibree.android.commons.ToothbrushModel.HUM_ELECTRIC
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sba.R
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.ANALYZING_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.FINISH_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.START_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.TOOTHBRUSH_STEP

/** Provides R resources ids according to the used toothbrush model  */
internal class TestBrushingResourceProvider :
    BrushStartResourceProvider {

    @RawRes
    fun provideDuringSessionVideo(model: ToothbrushModel, step: SessionStep) = when (step) {
        START_STEP -> videoForStartStep(model)
        FINISH_STEP -> provideDuringSessionFinishStepVideo(model)
        TOOTHBRUSH_STEP -> videoForToothbrushStep(model)
        ANALYZING_STEP -> videoForAnaylzingStep(model)
    }

    private fun videoForStartStep(model: ToothbrushModel): Int {
        return when (model) {
            PLAQLESS -> R.raw.anim_step1_plaqless
            else -> R.raw.anim_step1
        }
    }

    private fun videoForAnaylzingStep(model: ToothbrushModel): Int {
        return when (model) {
            PLAQLESS -> R.raw.anim_step4_plaqless
            else -> R.raw.anim_step4
        }
    }

    private fun videoForToothbrushStep(model: ToothbrushModel): Int {
        return when (model) {
            CONNECT_M1 -> R.raw.anim_step3_manual
            else -> R.raw.anim_step3
        }
    }

    private fun provideDuringSessionFinishStepVideo(model: ToothbrushModel) = when (model) {
        ARA -> R.raw.anim_step2_ara
        CONNECT_E1 -> R.raw.anim_step2_e1
        CONNECT_E2 -> R.raw.anim_step2_e2
        CONNECT_M1 -> R.raw.anim_step2_m1
        CONNECT_B1 -> R.raw.anim_step2_b1
        PLAQLESS -> R.raw.anim_step2_pql
        HILINK -> R.raw.anim_step2_e2
        HUM_ELECTRIC -> R.raw.anim_step2_e2
        HUM_BATTERY -> R.raw.anim_step2_b1
        GLINT -> R.raw.anim_step2_e2
    }

    @ColorRes
    fun provideBackgroundColor(model: ToothbrushModel, step: SessionStep) = when (step) {
        FINISH_STEP -> backgroundColorForFinishStep(model)
        TOOTHBRUSH_STEP -> backgroundColorForToothbrushStep(model)
        ANALYZING_STEP -> backgroundColorForAnalyzingStep(model)
        START_STEP -> backgroundColorForStartStep(model)
    }

    private fun backgroundColorForFinishStep(model: ToothbrushModel): Int {
        return when (model) {
            CONNECT_M1 -> R.color.during_session_step2_manual
            else -> R.color.during_session_step2
        }
    }

    private fun backgroundColorForToothbrushStep(model: ToothbrushModel): Int {
        return when (model) {
            CONNECT_M1 -> R.color.during_session_step3_manual
            else -> R.color.white
        }
    }

    private fun backgroundColorForAnalyzingStep(model: ToothbrushModel): Int {
        return when (model) {
            PLAQLESS -> R.color.during_session_step_pql
            else -> R.color.white
        }
    }

    private fun backgroundColorForStartStep(model: ToothbrushModel): Int {
        return when (model) {
            PLAQLESS -> R.color.during_session_step_pql
            else -> R.color.white
        }
    }

    @StringRes
    fun provideDuringSessionDescription(model: ToothbrushModel, step: SessionStep) = when (step) {
        START_STEP -> when (model) {
            PLAQLESS -> R.string.pql_durring_session_description_step1
            else -> R.string.durring_session_description_step1
        }
        FINISH_STEP -> R.string.durring_session_description_step2
        TOOTHBRUSH_STEP -> when (model) {
            CONNECT_M1 -> R.string.durring_session_description_step3_manual
            else -> R.string.durring_session_description_step3
        }
        ANALYZING_STEP -> R.string.durring_session_description_step4
    }

    @StringRes
    fun provideDuringSessionHighlighted(model: ToothbrushModel, step: SessionStep) = when (step) {
        START_STEP -> when (model) {
            PLAQLESS -> R.string.pql_durring_session_description_step1_highlighted
            else -> R.string.durring_session_highlighted_step1
        }
        FINISH_STEP -> R.string.empty
        TOOTHBRUSH_STEP -> when (model) {
            CONNECT_M1 -> R.string.durring_session_highlighted_step3_manual
            else -> R.string.durring_session_highlighted_step3
        }
        ANALYZING_STEP -> R.string.durring_session_highlighted_step4
    }
}
