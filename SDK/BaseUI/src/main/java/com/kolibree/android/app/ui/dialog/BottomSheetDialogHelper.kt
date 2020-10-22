package com.kolibree.android.app.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
object BottomSheetDialogHelper {
    fun create(context: Context, theme: Int) = BottomSheetDialog(context, theme).apply {
        setOnShowListener { dialog: DialogInterface? ->
            dialog?.apply {
                // hack to display the dialog in fullscreen like in zeplin
                val bottomSheetViewId = com.google.android.material.R.id.design_bottom_sheet
                (findViewById<View>(bottomSheetViewId) as? FrameLayout)?.let { bottomSheet ->
                    BottomSheetBehavior.from(bottomSheet)?.apply {
                        skipCollapsed = true
                        state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }
        }
    }
}
