/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.dialog

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.kolibree.android.app.extensions.setPartColoredText
import com.kolibree.android.app.ui.dialog.SimpleRoundedDialog
import com.kolibree.android.extensions.setOnDebouncedClickListener
import com.kolibree.android.plaqless.R

class BestResultsDialog : SimpleRoundedDialog() {

    companion object {
        val TAG = BestResultsDialog::class.java.name

        @JvmStatic
        fun show(fragmentManager: FragmentManager) {
            if (fragmentManager.findFragmentByTag(TAG) == null) {
                val dialog = BestResultsDialog()
                dialog.showNow(fragmentManager, TAG)
            }
        }

        @JvmStatic
        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as DialogFragment?
            dialog?.dismissAllowingStateLoss()
        }
    }

    override fun layoutId() = R.layout.dialog_best_results

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCloseButton(view)
        initBody(view)
        initInstructionPreview(view)
    }

    private fun initBody(root: View) {
        val body = root.findViewById<TextView>(R.id.best_results_body)
        context?.let {
            val fullText = it.getString(R.string.pql_best_results_body)
            val coloredText = it.getString(R.string.pql_best_results_body_highlight)
            body.setPartColoredText(fullText, coloredText, R.color.colorPrimaryDark)
        }
    }

    private fun initCloseButton(root: View) {
        root.findViewById<Button>(R.id.best_results_ok).setOnDebouncedClickListener {
            dismissAllowingStateLoss()
        }
    }

    private fun initInstructionPreview(root: View) {
        val preview = root.findViewById<ImageView>(R.id.best_results_instruction)
        context?.let {
            Glide.with(it)
                .load(R.raw.plaqless_for_best_results)
                .into(preview)
        }
    }
}
