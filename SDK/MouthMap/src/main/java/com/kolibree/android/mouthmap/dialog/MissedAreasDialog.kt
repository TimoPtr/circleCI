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
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.kolibree.android.app.ui.dialog.SimpleRoundedDialog
import com.kolibree.android.extensions.setOnDebouncedClickListener
import com.kolibree.android.mouthmap.R

internal class MissedAreasDialog : SimpleRoundedDialog() {

    companion object {
        val TAG = MissedAreasDialog::class.java.name
        fun show(fragmentManager: FragmentManager?) {
            fragmentManager?.let {
                if (it.findFragmentByTag(TAG) == null) {
                    val dialog = MissedAreasDialog()
                    dialog.showNow(it,
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

    override fun layoutId() = R.layout.dialog_missed_areas

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCloseView(view)
        initBodyView(view)
    }

    private fun initBodyView(root: View) {
        val body = root.findViewById<TextView>(R.id.missed_area_body)
        context?.let {
            val text = it.getString(R.string.missed_area_dialog_body)
            body.text = Html.fromHtml(text)
        }
    }

    private fun initCloseView(root: View) {
        root.findViewById<View>(R.id.missed_area_ok).setOnDebouncedClickListener {
            dismissAllowingStateLoss()
        }
    }
}
