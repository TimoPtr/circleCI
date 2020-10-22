/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.disconnection

import android.content.Context
import android.view.Gravity
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.baseui.hum.R
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_ACTIVE

@VisibleForApp
class LostConnectionDialogController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var dialog: AppCompatDialog? = null
    private var lifecycleObserver: DefaultLifecycleObserver? = null

    fun update(
        state: LostConnectionHandler.State,
        dismissCallback: () -> Unit
    ) {
        when {
            state == CONNECTION_ACTIVE && dialog != null -> dismissDialog()
            state != CONNECTION_ACTIVE && dialog == null -> showDialog(dismissCallback)
        }
    }

    @Suppress("LongMethod")
    private fun showDialog(dismissCallback: () -> Unit) {
        dialog = alertDialog(context) {
            cancellable(false)
            featureImage {
                drawable(R.drawable.ic_connection_lost_dialog)
                scaleType(ImageView.ScaleType.FIT_CENTER)
            }
            headlineText {
                text(R.string.dialog_lost_connection_title)
                gravity(Gravity.CENTER)
            }
            body(R.string.dialog_lost_connection_description, Gravity.CENTER)
            textButtonTertiary {
                title(R.string.dialog_lost_connection_quit_button)
                action {
                    LostConnectionDialogAnalytics.quit()
                    dismissCallback()
                }
            }
        }.also {
            registerToLifecycle(it, lifecycleOwner)
            it.show()
        }
    }

    private fun registerToLifecycle(dialog: AppCompatDialog, lifecycleOwner: LifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        lifecycleObserver = object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                dialog.show()
            }

            override fun onStop(owner: LifecycleOwner) {
                dialog.dismiss()
            }
        }.also {
            lifecycle.addObserver(it)
        }
    }

    private fun dismissDialog() {
        lifecycleObserver?.let { lifecycleOwner.lifecycle.removeObserver(it) }
        lifecycleObserver = null
        dialog?.dismiss()
        dialog = null
    }
}
