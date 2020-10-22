/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.base

import android.app.Activity
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.homeui.hum.R

/** Base implementation for checkup screens */
@VisibleForApp
abstract class BaseCheckupActivity<
    VS : BaseCheckupViewState,
    F : BaseViewModel.Factory<VS>,
    VM : BaseCheckupViewModel<VS>,
    B : ViewDataBinding
    > : BaseMVIActivity<VS, CheckupActions, F, VM, B>() {

    @CallSuper
    override fun execute(action: CheckupActions) = when (action) {
        CheckupActions.FinishCancel -> finishCancel()
        CheckupActions.FinishOk -> finishOk()
        CheckupActions.ConfirmDeletion -> showDeleteBrushingConfirmationDialog()
    }

    private fun finishCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun finishOk() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun showDeleteBrushingConfirmationDialog() =
        alertDialog(this) {
            title(R.string.orphan_brushings_delete_title)
            body(R.string.orphan_brushings_delete_message)
            containedButton {
                title(R.string.um_no)
                action { dismiss() }
            }
            outlinedButton {
                title(R.string.um_yes)
                action {
                    dismiss()
                    viewModel.onDeleteConfirmed()
                }
            }
        }.show()
}
