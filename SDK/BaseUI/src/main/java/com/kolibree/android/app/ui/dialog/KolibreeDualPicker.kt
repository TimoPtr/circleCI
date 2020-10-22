/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.kolibree.android.app.ui.dialog.KolibreeDialogDsl.AlertBase
import com.kolibree.android.baseui.R
import com.kolibree.android.failearly.FailEarly

@Keep
abstract class KolibreeDualPicker<VALUE>(
    context: Context,
    initialValue: VALUE
) : AlertBase<VALUE>(context, initialValue) {

    @LayoutRes
    override val bodyLayout: Int = R.layout.kolibree_dialog_dual_picker

    var majorLabel: String? = null
        private set

    /**
     * Sets the label of the major NumberPicker to the specified string
     */
    fun majorLabel(majorLabel: String) {
        this.majorLabel = majorLabel
    }

    /**
     * Sets the label of the major NumberPicker to the specified string resource
     */
    fun majorLabel(@StringRes majorLabelId: Int) {
        this.majorLabel = context.getString(majorLabelId)
    }

    var minorLabel: String? = null
        private set

    /**
     * Sets the label of the minor NumberPicker to the specified string
     */
    fun minorLabel(minorLabel: String) {
        this.minorLabel = minorLabel
    }

    /**
     * Sets the label of the minor NumberPicker to the specified string resource
     */
    fun minorLabel(@StringRes minorLabelId: Int) {
        this.minorLabel = context.getString(minorLabelId)
    }

    override fun featureImage(lambda: KolibreeDialogDrawable.() -> Unit) {
        FailEarly.fail(
            "Feature images are not supported on picker dialogs because they're likely " +
                "to cause problems on smaller screens."
        )
    }

    override fun featureIcon(lambda: KolibreeDialogDrawable.() -> Unit) {
        FailEarly.fail(
            "Feature icon are not supported on picker dialogs because they're likely " +
                "to cause problems on smaller screens."
        )
    }

    override fun doBuild(parent: ConstraintLayout, constraintSet: ConstraintSet): View {
        super.doBuild(parent, constraintSet)
        majorLabel?.also {
            parent.safeFind<TextView>(R.id.picker_major_label).apply {
                text = majorLabel
            }
        }
        minorLabel?.also {
            parent.safeFind<TextView>(R.id.picker_minor_label).apply {
                text = minorLabel
            }
        }
        return parent.safeFind(R.id.picker_minor_container)
    }
}
