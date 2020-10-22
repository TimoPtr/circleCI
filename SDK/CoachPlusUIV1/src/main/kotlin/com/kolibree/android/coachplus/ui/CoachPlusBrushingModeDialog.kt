/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.ui

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentManager
import com.kolibree.android.app.ui.dialog.SimpleRoundedDialog
import com.kolibree.android.coachplus.CoachPlusAnalyticsHelper
import com.kolibree.android.coachplus.R
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

private const val TAG = "CoachPlusBrushingModeDialogTag"

/** Vibration Speed Control (Brushing mode) selection dialog */
// TODO remove params and use newInstance to remove warning
internal class CoachPlusBrushingModeDialog(
    private val availableBrushingModes: List<BrushingMode>,
    private val currentBrushingMode: BrushingMode
) : SimpleRoundedDialog() {

    @Inject
    internal lateinit var analyticsHelper: CoachPlusAnalyticsHelper

    /**
     * Show the ConfirmationDialogFragment unless it's already displayed.
     *
     * @param fragmentManager the fragment manager used to show the fragment
     */
    fun showIfNotPresent(fragmentManager: FragmentManager) {
        fragmentManager.executePendingTransactions()
        val fragment = fragmentManager.findFragmentByTag(TAG)

        if (fragment == null) {
            showNow(fragmentManager, TAG)
            analyticsHelper.onBrushingModeDialogShown()
        }
    }

    override fun layoutId() = R.layout.dialog_coach_plus_brushing_mode

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateOptions(view)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun populateOptions(rootView: View) {
        val group: RadioGroup = rootView.findViewById(R.id.brushing_modes_radio_group)

        for (brushingMode in BrushingMode.sortByIntensity(availableBrushingModes)) {
            group.addView(createOptionView(brushingMode))
        }

        group.setOnCheckedChangeListener(onCheckedChangedListener)
    }

    private val onCheckedChangedListener = RadioGroup.OnCheckedChangeListener {
            radioGroup, checkedId ->
        val selectedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
        onBrushingModeSelected(selectedRadioButton.tag as BrushingMode)
        analyticsHelper
            .onBrushingModeDialogOptionChosen(radioGroup.indexOfChild(selectedRadioButton))
        dismissAllowingStateLoss()
    }

    private fun createOptionView(brushingMode: BrushingMode): RadioButton {
        val option = RadioButton(context)
        option.id = View.generateViewId()
        option.layoutParams = createOptionLayoutParams()
        option.text = getOptionText(brushingMode, brushingMode == currentBrushingMode)
        option.setBackgroundResource(R.drawable.brushing_mode_option_background_selector)
        option.buttonDrawable = null
        option.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.xbig_text))
        val padding = resources.getDimensionPixelOffset(R.dimen.rounded_dialog_margin_normal)
        option.setPadding(0, padding, 0, padding)
        option.gravity = Gravity.CENTER
        option.tag = brushingMode

        if (brushingMode == currentBrushingMode) {
            option.isChecked = true
        }

        return option
    }

    private fun createOptionLayoutParams() =
        RadioGroup.LayoutParams(context, null)
            .apply {
                bottomMargin = resources
                    .getDimensionPixelOffset(R.dimen.rounded_dialog_margin_normal)
                width = RadioGroup.LayoutParams.MATCH_PARENT
                height = RadioGroup.LayoutParams.WRAP_CONTENT
            }

    private fun getOptionText(brushingMode: BrushingMode, selected: Boolean) =
        if (selected) {
            getString(
                getOptionLabel(
                    brushingMode
                )
            ) +
                "\n" + getString(R.string.brushing_program_dialog_current)
        } else {
            getString(
                getOptionLabel(
                    brushingMode
                )
            )
        }

    private fun onBrushingModeSelected(brushingMode: BrushingMode) {
        (activity as? CoachPlusBrushingModeDialogListener)?.onBrushingModeSelected(brushingMode)
    }

    companion object {

        @VisibleForTesting
        @StringRes
        fun getOptionLabel(brushingMode: BrushingMode): Int =
            when (brushingMode) {
                BrushingMode.Regular -> R.string.brushing_program_dialog_regular
                BrushingMode.Slow -> R.string.brushing_program_dialog_slow
                BrushingMode.Strong -> R.string.brushing_program_dialog_strong
                BrushingMode.Polishing -> R.string.brushing_program_dialog_polishing
                BrushingMode.UserDefined -> R.string.brushing_program_dialog_custom
            }
    }
}

/** [CoachPlusBrushingModeDialog] callback, to be implemented by parent activity */
internal interface CoachPlusBrushingModeDialogListener {

    /**
     * Implement this method to get notified on a brushing mode selection
     *
     * @see [BrushingMode]
     */
    fun onBrushingModeSelected(brushingMode: BrushingMode)
}
