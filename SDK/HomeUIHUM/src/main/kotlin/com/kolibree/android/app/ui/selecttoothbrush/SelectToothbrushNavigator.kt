/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selecttoothbrush

import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.dialog.carouselDialog
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class SelectToothbrushNavigator : BaseNavigator<AppCompatActivity>() {

    fun selectToothbrush(toothbrushes: List<SelectToothbrushItem>): Maybe<SelectToothbrushItem> {
        return Maybe
            .create<SelectToothbrushItem> { emitter -> showPicker(toothbrushes, emitter) }
            .subscribeOn(AndroidSchedulers.mainThread())
    }

    @SuppressWarnings("LongMethod")
    private fun showPicker(
        toothbrushes: List<SelectToothbrushItem>,
        emitter: MaybeEmitter<SelectToothbrushItem>
    ) = withOwner {
        FailEarly.failIfNotExecutedOnMainThread()

        val dialog = carouselDialog<SelectToothbrushItem>(context = this) {
            headlineText {
                text(R.string.select_toothbrush_picker_header)
                gravity(Gravity.CENTER)
            }

            body(
                R.string.select_toothbrush_picker_body,
                gravity = Gravity.CENTER
            )

            items = toothbrushes

            val interaction = object : SelectToothbrushInteraction {
                override fun onItemClick(item: SelectToothbrushItem) {
                    if (items.anySelected()) {
                        return
                    }

                    items = items.select(item)

                    lifecycleScope.launch {
                        delay(SELECTION_DELAY_MS)
                        dismiss()
                        emitter.onSuccess(item)
                    }
                }
            }

            onItemBind { binding, _ ->
                binding.set(BR.item, R.layout.item_select_toothbrush)
                binding.bindExtra(BR.interaction, interaction)
            }
        }

        dialog.setOnDismissListener { emitter.onComplete() }
        dialog.show()
    }

    private fun List<SelectToothbrushItem>.anySelected(): Boolean {
        return any { it.isSelected }
    }

    private fun List<SelectToothbrushItem>.select(
        itemToSelect: SelectToothbrushItem
    ): List<SelectToothbrushItem> {
        return map { item ->
            item.copy(isSelected = item == itemToSelect)
        }
    }
}

internal const val SELECTION_DELAY_MS = 250L
