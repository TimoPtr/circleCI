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
import android.widget.CompoundButton
import androidx.annotation.Keep
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.kolibree.android.app.ui.dialog.KolibreeDialogBuilder.Companion.UNDEFINED_RESOURCE_ID
import com.kolibree.android.baseui.R

@Keep
sealed class KolibreeSelectionControls<T : CompoundButton, V>(
    protected val context: Context,
    private val initialValue: V
) : KolibreeDialogCommon<T, V>(initialValue) {

    var selected: Boolean = false

    class CheckBox<V>(context: Context, initialValue: V) :
        KolibreeSelectionControls<MaterialCheckBox, V>(context, initialValue) {

        override fun createCompoundButton() =
            MaterialCheckBox(context, null, R.attr.checkboxStyle)
    }

    class RadioButton<V>(context: Context, initialValue: V) :
        KolibreeSelectionControls<MaterialRadioButton, V>(context, initialValue) {

        override fun createCompoundButton() =
            MaterialRadioButton(context, null, R.attr.radioButtonStyle)
    }

    override fun build(): T =
        createCompoundButton().apply {
            title?.also { text = it }
            titleId.takeIf { it != UNDEFINED_RESOURCE_ID }?.also { text = context.getText(it) }
            isChecked = selected
        }

    protected abstract fun createCompoundButton(): T
}

/**
 * Represents a currently selected item.
 *
 * @param index The index of this item
 * @param view The view of this item
 * @param text The text of this item
 */
@Keep
data class Selection(val index: Int, val view: CompoundButton, val text: CharSequence)
