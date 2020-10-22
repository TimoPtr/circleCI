/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.scan

import android.content.Intent
import android.os.Parcelable
import com.kolibree.android.commons.ToothbrushModel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SelectedToothbrushData(
    val mac: String,
    val model: ToothbrushModel
) : Parcelable {

    fun toResult() = Intent().apply {
        putExtra(EXTRA_SELECTED_TOOTHBRUSH, this@SelectedToothbrushData)
    }

    companion object {
        fun extractSelectedToothbrushData(intent: Intent?): SelectedToothbrushData? {
            return intent?.getParcelableExtra(EXTRA_SELECTED_TOOTHBRUSH) as? SelectedToothbrushData
        }
    }
}

private const val EXTRA_SELECTED_TOOTHBRUSH = "EXTRA_SELECTED_TOOTHBRUSH"
