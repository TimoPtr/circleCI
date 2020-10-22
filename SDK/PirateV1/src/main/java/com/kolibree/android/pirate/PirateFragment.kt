/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate

import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import com.kolibree.android.app.ui.dialog.PopupDialogFragment
import com.kolibree.android.commons.ToothbrushModel

internal class PirateFragment : BasePirateFragment(),
    PopupDialogFragment.PopupClosedListener {
    private var popUpShown = false

    override fun maybeShowProfileNotAllowedToBrushPopup() {
        if (!popUpShown) {
            activity?.let {
                val popup = PopupDialogFragment.newInstance(
                    getString(R.string.game_brush_not_allow_title),
                    getString(R.string.game_brush_not_allow_msg),
                    POPUP_GAME_NOT_ALLOWED
                )
                popup.setTargetFragment(this, POPUP_GAME_NOT_ALLOWED)

                // avoid FragmentManager is already executing transactions
                Handler(Looper.getMainLooper()).post {
                    if (isVisible) {
                        popup.showNow(it.supportFragmentManager, null)
                        popUpShown = true
                    }
                }
            }
        }
    }

    override fun onPopupClosed(popupId: Int) = when (popupId) {
        POPUP_GAME_NOT_ALLOWED -> popUpShown = false
        else -> {
            /* no-op */
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

@Keep
fun publicCreatePirateFragment(
    toothbrushModel: ToothbrushModel,
    macAddress: String
): BasePirateFragment =
    createPirateFragment(toothbrushModel, macAddress)
