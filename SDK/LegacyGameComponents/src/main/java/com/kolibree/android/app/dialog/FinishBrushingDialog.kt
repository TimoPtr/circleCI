/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dialog

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.kolibree.android.app.ui.dialog.SimpleRoundedDialog
import com.kolibree.game.legacy.R

@Keep
class FinishBrushingDialog : SimpleRoundedDialog(persistentDialog = true) {

    companion object {

        private val TAG = FinishBrushingDialog::class.java.name

        fun show(fragmentManager: FragmentManager?, answerCallback: (Boolean) -> Unit) {
            fragmentManager?.let {
                if (it.findFragmentByTag(TAG) == null) {
                    val dialog = FinishBrushingDialog()
                    dialog.isCancelable = false
                    dialog.answerCallback = answerCallback
                    dialog.showNow(it, TAG)
                }
            }
        }

        fun hide(fragmentManager: FragmentManager?) {
            val dialog = fragmentManager?.findFragmentByTag(TAG) as DialogFragment?
            dialog?.dismissAllowingStateLoss()
        }
    }

    private var answerCallback = { isBrushingFinished: Boolean -> }

    override fun layoutId() = R.layout.fragment_finish_brushing_dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.finish_brushing_yes).setOnClickListener { onAnswer(true) }
        view.findViewById<View>(R.id.finish_brushing_no).setOnClickListener { onAnswer(false) }
    }

    private fun onAnswer(isBrushingFinished: Boolean) {
        answerCallback.invoke(isBrushingFinished)
        dismissAllowingStateLoss()
    }
}
