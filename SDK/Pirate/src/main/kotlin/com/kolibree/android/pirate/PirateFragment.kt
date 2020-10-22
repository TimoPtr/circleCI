package com.kolibree.android.pirate

import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.commons.ToothbrushModel

internal class PirateFragment : BasePirateFragment() {
    private var popUpShown = false

    override fun maybeShowProfileNotAllowedToBrushPopup() {
        if (!popUpShown) {
            activity?.let { activity ->
                alertDialog(activity) {
                    lifecycleOwner(activity)
                    /*title(R.string.game_brush_not_allow_title)
                    body(R.string.game_brush_not_allow_msg)*/
                    dismissAction {
                        popUpShown = false
                    }
                }.show().also {
                    popUpShown = true
                }
            }
        }
    }
}

internal fun createPirateFragment(
    toothbrushModel: ToothbrushModel,
    macAddress: String
): PirateFragment =
    PirateFragment().apply {
        addArguments(this, toothbrushModel, macAddress)
    }
