package com.kolibree.android.sba.testbrushing.base

import android.annotation.SuppressLint
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.sdk.disconnection.LostConnectionHandler

@SuppressLint("DeobfuscatedPublicSdkClass")
sealed class ViewAction : BaseAction

internal object NoneAction : ViewAction()

internal object ShowFinishBrushingDialog : ViewAction()
internal object HideFinishBrushingDialog : ViewAction()
internal data class LostConnectionStateChanged(
    val state: LostConnectionHandler.State
) : ViewAction()
internal class UpdateTimer(val elapsedTime: Long) : ViewAction()

internal object ShowReadDiagramDialog : ViewAction()
internal object ShowSpeedHintDialog : ViewAction()

internal object ShowAnalysisSuccessfulResult : ViewAction()
internal object ShowMouthCoverageResult : ViewAction()
internal object ShowSpeedResult : ViewAction()
internal object ShowAngleResult : ViewAction()

// SBA progress
internal class FadeInAction(val secOffset: Int) : ViewAction()

internal object ToggleJawsView : ViewAction()
