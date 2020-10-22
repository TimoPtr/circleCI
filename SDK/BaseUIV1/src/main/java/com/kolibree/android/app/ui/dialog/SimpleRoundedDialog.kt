/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import com.kolibree.android.KolibreeExperimental
import com.kolibree.android.baseui.v1.R

@Keep
abstract class SimpleRoundedDialog(
    // TODO remove once https://kolibree.atlassian.net/browse/KLTB002-8777 is done
    @KolibreeExperimental
    private val persistentDialog: Boolean = false
) : BaseDialogFragment() {

    @LayoutRes
    abstract fun layoutId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val rootView = inflater.inflate(R.layout.fragment_rounded_dialog, container, false) as FrameLayout
        inflater.inflate(layoutId(), rootView, true)
        return rootView
    }

    override fun onPause() {
        super.onPause()

        // TODO remove once https://kolibree.atlassian.net/browse/KLTB002-8777 is done
        if (!persistentDialog) dismissAllowingStateLoss()
    }
}
