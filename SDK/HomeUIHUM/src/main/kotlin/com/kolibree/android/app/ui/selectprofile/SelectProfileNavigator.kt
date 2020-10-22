/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectprofile

import android.os.Looper
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.addprofile.startAddProfileActivity
import com.kolibree.android.app.ui.dialog.carouselDialog
import com.kolibree.android.app.ui.selecttoothbrush.SELECTION_DELAY_MS
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import java.lang.RuntimeException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class SelectProfileNavigator : BaseNavigator<AppCompatActivity>() {

    private var selectProfileDialog: AppCompatDialog? = null

    /**
     * Displays dialog that allows user to select a profile or add a new one.
     * Returns [Maybe]<[SelectProfileItem]> that will emit a [ProfileItem] or [AddProfileItem].
     * If nothing is selected, it will complete.
     */
    fun selectProfileDialogMaybe(profiles: List<SelectProfileItem>): Maybe<SelectProfileItem> {
        return Maybe
            .create<SelectProfileItem> { emitter -> showPickerMaybe(profiles, emitter) }
            .subscribeOn(AndroidSchedulers.mainThread())
    }

    @SuppressWarnings("LongMethod")
    private fun showPickerMaybe(
        profiles: List<SelectProfileItem>,
        emitter: MaybeEmitter<SelectProfileItem>
    ) = withOwner {
        if (!isMainThread()) {
            whenNotDisposed(emitter) { it.tryOnError(NotOnMainThreadException()) }
            return@withOwner
        }

        selectProfileDialog = carouselDialog<SelectProfileItem>(context = this) {
            headlineText {
                text(R.string.select_profile_header)
                gravity(Gravity.CENTER)
            }

            textButton {
                title(R.string.select_profile_close)

                action {
                    dismiss()
                }
            }

            items = profiles
            var isDismissing = false

            val interaction = object : SelectProfileInteraction {
                override fun onItemClick(item: SelectProfileItem) {
                    if (isDismissing) return

                    isDismissing = true
                    items = items.select(item)

                    lifecycleScope.launch {
                        delay(SELECTION_DELAY_MS)
                        dismiss()
                        whenNotDisposed(emitter) { it.onSuccess(item) }
                    }
                }
            }

            onItemBind { binding, item ->
                val layout = when (item) {
                    is ProfileItem -> R.layout.item_select_profile
                    else -> R.layout.item_add_profile_item
                }
                binding.set(BR.item, layout)
                binding.bindExtra(BR.interaction, interaction)
            }
        }

        selectProfileDialog?.setOnDismissListener {
            whenNotDisposed(emitter) { it.onComplete() }
        }
        selectProfileDialog?.show()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        selectProfileDialog?.dismiss()
        selectProfileDialog = null
        super.onDestroy(owner)
    }

    private fun whenNotDisposed(
        emitter: MaybeEmitter<SelectProfileItem>,
        block: (MaybeEmitter<SelectProfileItem>) -> Unit
    ) {
        if (!emitter.isDisposed) {
            block(emitter)
        }
    }

    private fun isMainThread(): Boolean = Thread.currentThread() == Looper.getMainLooper().thread

    private fun List<SelectProfileItem>.select(
        itemToSelect: SelectProfileItem
    ): List<SelectProfileItem> {
        return map { item ->
            when (item) {
                is ProfileItem -> item.copy(isSelected = item == itemToSelect)
                is AddProfileItem -> item.copy(isSelected = item == itemToSelect)
            }
        }
    }

    fun showAddProfileScreen() = withOwner {
        startAddProfileActivity(this)
    }
}

private class NotOnMainThreadException :
    RuntimeException("Code should be executed on the main thread")
