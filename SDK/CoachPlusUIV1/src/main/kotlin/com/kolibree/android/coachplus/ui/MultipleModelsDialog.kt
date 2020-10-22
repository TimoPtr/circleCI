/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.kolibree.android.app.ui.dialog.SimpleRoundedDialog
import com.kolibree.android.coachplus.R
import com.kolibree.android.extensions.setOnDebouncedClickListener

@Deprecated("Should not be used anymore here for compatibility")
internal class MultipleModelsDialog : SimpleRoundedDialog() {

    companion object {
        val TAG = MultipleModelsDialog::class.java.name

        @JvmStatic
        fun show(fragmentManager: FragmentManager?) {
            fragmentManager?.let {
                if (it.findFragmentByTag(TAG) == null) {
                    val dialog = MultipleModelsDialog()
                    dialog.showNow(it,
                        TAG
                    )
                }
            }
        }

        @JvmStatic
        fun hide(fragmentManager: FragmentManager?) {
            val dialog = fragmentManager?.findFragmentByTag(TAG) as DialogFragment?
            dialog?.dismissAllowingStateLoss()
        }

        @JvmStatic
        fun isShowing(fragmentManager: FragmentManager?): Boolean {
            val dialog = fragmentManager?.findFragmentByTag(TAG) as DialogFragment?
            return dialog != null && dialog.dialog != null
        }
    }

    override fun layoutId() = R.layout.start_message_dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = view.findViewById<Button>(R.id.start_message_button)
        button.text = context?.getText(R.string.coach_dialog_start_message_cancel)
        button.setOnDebouncedClickListener {
            dismissAllowingStateLoss()
        }

        val body = view.findViewById<TextView>(R.id.start_message_body)
        body.text = context?.getText(R.string.coach_dialog_start_message_body)
    }
}
