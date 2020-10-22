/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget.snackbar

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.snackbar.Snackbar
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.Error
import com.kolibree.android.app.Error.ErrorStyle.Indefinite
import com.kolibree.android.failearly.FailEarly
import kotlinx.android.parcel.Parcelize

/**
 * This View has been inspired by ConstraintLayout's Guideline, which mean the View is not meant
 * to be displayed and is only for configuration purpose.
 * It is used in order to let the Snackbars DataBinding compatible.
 *
 * The main reflexion was as the ViewState drives the UI and is the only source of truth,
 * we should be able to control Snackbars without pushing an Action,
 * especially if we need to dismiss the Snackbar later.
 *
 * Only Snackbars with a LENGTH_INDEFINITE state should be used with this Handler,
 * short-lived Snackbars should be used as always pushing an Action to the Activity.
 */
@VisibleForApp
class SnackbarHandler @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var snackbar: Snackbar? = null
    private var configuration: SnackbarConfiguration? = null
    private var twoWayListener: InverseBindingListener? = null

    private var clickCallback: Snackbar.Callback? = null
    private var swipeCallback: Snackbar.Callback? = null

    fun updateSnackbar(configuration: SnackbarConfiguration) {
        this.configuration = configuration
        if (configuration.isShown) {
            snackbar = showErrorSnackbar(configuration.error!!).apply {
                addCallback(configurationChangeCallback())
                addCallback(clickCallback)
                addCallback(swipeCallback)
            }
        } else {
            snackbar?.dismiss()
            snackbar = null
        }
    }

    private fun configurationChangeCallback() =
        object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                configuration = configuration?.copy(isShown = false)
                twoWayListener?.onChange()
            }
        }

    @VisibleForApp
    object SnackbarHandlerBindingAdapters {

        @VisibleForApp
        @BindingAdapter("configuration")
        @JvmStatic
        fun SnackbarHandler.setConfiguration(configuration: SnackbarConfiguration?) {
            if (this.configuration != configuration && configuration != null) {
                this.updateSnackbar(configuration)
            }
        }

        @VisibleForApp
        @BindingAdapter("configurationAttrChanged")
        @JvmStatic
        fun SnackbarHandler.setListener(twoWayListener: InverseBindingListener) {
            this.twoWayListener = twoWayListener
        }

        @VisibleForApp
        @InverseBindingAdapter(attribute = "configuration")
        @JvmStatic
        fun SnackbarHandler.getConfiguration(): SnackbarConfiguration? {
            return this.configuration
        }

        @VisibleForApp
        @BindingAdapter("onClickAction")
        @JvmStatic
        fun SnackbarHandler.onClickAction(dismissCallback: DismissActionListener) {
            clickCallback = object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == DISMISS_EVENT_ACTION) {
                        dismissCallback.onDismissed()
                    }
                }
            }
        }

        @VisibleForApp
        @BindingAdapter("onDismissAction")
        @JvmStatic
        fun SnackbarHandler.onDismissAction(dismissCallback: DismissActionListener) {
            swipeCallback = object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == DISMISS_EVENT_SWIPE) {
                        dismissCallback.onDismissed()
                    }
                }
            }
        }
    }

    @VisibleForApp
    interface DismissActionListener {
        fun onDismissed()
    }
}

@VisibleForApp
@Parcelize
data class SnackbarConfiguration(
    val isShown: Boolean = false,
    val error: Error? = null
) : Parcelable {
    init {
        FailEarly.failInConditionMet(
            error != null && error.style != Indefinite,
            "This Snackbar Handler should only be used with a LENGTH_INDEFINITE duration"
        )

        FailEarly.failInConditionMet(
            isShown && error == null,
            "Error is mandatory and should be set if showSnackbar is true"
        )
    }
}
