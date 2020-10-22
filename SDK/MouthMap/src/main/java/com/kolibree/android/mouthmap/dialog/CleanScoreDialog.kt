/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap.dialog

import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.View.GONE
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.kolibree.android.app.ui.dialog.SimpleRoundedDialog
import com.kolibree.android.extensions.setOnDebouncedClickListener
import com.kolibree.android.mouthmap.R
import java.text.NumberFormat

internal class CleanScoreDialog : SimpleRoundedDialog() {

    companion object {
        val TAG = CleanScoreDialog::class.java.name
        val EXTRA_HAS_PLAQLESS_DATA = "$TAG:EXTRA_HAS_PLAQLESS_DATA"
        val EXTRA_MISSED = "$TAG:EXTRA_MISSED"
        val EXTRA_REMAINS = "$TAG:EXTRA_REMAINS"
        val EXTRA_SCORE = "$TAG:EXTRA_SCORE"

        fun show(
            fragmentManager: FragmentManager?,
            hasPlaqlessData: Boolean,
            missed: Int,
            remains: Int,
            score: Int
        ) {
            fragmentManager?.let {
                if (it.findFragmentByTag(TAG) == null) {
                    val args = Bundle()
                    args.putBoolean(EXTRA_HAS_PLAQLESS_DATA, hasPlaqlessData)
                    args.putInt(EXTRA_MISSED, missed)
                    args.putInt(EXTRA_REMAINS, remains)
                    args.putInt(EXTRA_SCORE, score)
                    val dialog = CleanScoreDialog()
                    dialog.arguments = args
                    dialog.showNow(
                        it,
                        TAG
                    )
                }
            }
        }

        fun hide(fragmentManager: FragmentManager?) {
            val dialog = fragmentManager?.findFragmentByTag(TAG) as DialogFragment?
            dialog?.dismissAllowingStateLoss()
        }
    }

    override fun layoutId() = R.layout.dialog_clean_score

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCleanLabel(view)
        initCloseView(view)
        initScoreView(view)
        initCleanText(view)
        initRemainsText(view)
        initMissedText(view)
        initScoreProgress(view)
    }

    private fun initScoreProgress(root: View) {
        val progressView = root.findViewById<ProgressBar>(R.id.clean_score_progress)
        progressView.progress = score()
    }

    private fun initMissedText(root: View) {
        val missedView = root.findViewById<TextView>(R.id.clean_score_missed)
        fillWithPercentText(missedView, R.string.clean_score_dialog_missed, missed())
    }

    private fun initRemainsText(root: View) {
        val remainsView = root.findViewById<TextView>(R.id.clean_score_remains)
        if (hasPlaqlessData())
            fillWithPercentText(remainsView, R.string.clean_score_dialog_remains, remains())
        else
            remainsView.visibility = GONE
    }

    private fun initCleanText(root: View) {
        val cleanView = root.findViewById<TextView>(R.id.clean_score_cleaned)
        fillWithPercentText(cleanView, R.string.clean_score_dialog_clean, score())
    }

    private fun initCleanLabel(root: View) {
        val cleanView = root.findViewById<TextView>(R.id.clean_score_label)
        cleanView.setText(
            if (hasPlaqlessData()) R.string.mouth_map_clean_score
            else R.string.dashboard_coverage
        )
    }

    private fun initScoreView(root: View) {
        val score = root.findViewById<TextView>(R.id.clean_score_score)
        score.text = score().toString()
    }

    private fun initCloseView(root: View) {
        root.findViewById<View>(R.id.clean_score_ok).setOnDebouncedClickListener {
            dismissAllowingStateLoss()
        }
    }

    private fun fillWithPercentText(textView: TextView, @StringRes textRes: Int, progress: Int) {
        context?.let {
            val textualPercent = toPercent(progress)
            val formattedHtml = it.getString(textRes, textualPercent)
            textView.text = Html.fromHtml(formattedHtml)
        }
    }

    private fun toPercent(progress: Int): String {
        val numberFormat = NumberFormat.getPercentInstance()
        return numberFormat.format(progress / 100f)
    }

    private fun hasPlaqlessData() = arguments?.getBoolean(EXTRA_HAS_PLAQLESS_DATA, false) ?: false

    private fun score() = arguments?.getInt(EXTRA_SCORE, 0) ?: 0

    private fun remains() = arguments?.getInt(EXTRA_REMAINS, 0) ?: 0

    private fun missed() =
        if (hasPlaqlessData()) arguments?.getInt(EXTRA_MISSED, 0) ?: 0 else 100 - score()
}
