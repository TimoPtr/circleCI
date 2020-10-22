/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.completion

import android.content.Context
import android.text.Spannable
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.text.highlightString
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class BrushingStreakCompletionViewState(
    val smiles: Int = 0
) : BaseViewState {

    companion object {
        fun initial() = BrushingStreakCompletionViewState()
    }

    fun body(context: Context): Spannable {
        val smilePoints =
            context.getString(R.string.challenge_completed_dialog_body_highlight, smiles.toString())
        val fullText =
            context.getString(R.string.challenge_completed_dialog_body, smilePoints)

        return highlightString(fullText, smilePoints)
    }
}
