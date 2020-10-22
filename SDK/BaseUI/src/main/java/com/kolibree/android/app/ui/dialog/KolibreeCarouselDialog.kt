/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.baseui.R
import com.kolibree.android.failearly.FailEarly
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapters
import me.tatarka.bindingcollectionadapter2.ItemBinding

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@VisibleForApp
fun <MODEL> carouselDialog(
    context: Context,
    lambda: KolibreeCarouselDialog<MODEL>.() -> Unit
): AppCompatDialog =
    KolibreeCarouselDialog<MODEL>(context)
        .apply(lambda)
        .build()

@VisibleForApp
class KolibreeCarouselDialog<MODEL>(
    context: Context
) : KolibreeDialogDsl.Alert(context) {

    @LayoutRes
    override val bodyLayout: Int = R.layout.kolibree_dialog_carousel

    private val bindingAdapter = BindingRecyclerViewAdapter<MODEL>()

    private var onItemBind: (ItemBinding<*>, MODEL?) -> Unit = { _, _ -> }

    var items: List<MODEL> = emptyList()
        set(value) {
            field = value
            bindingAdapter.setItems(value)
        }

    override fun featureImage(lambda: KolibreeDialogDrawable.() -> Unit) {
        FailEarly.fail("Feature Image is not supported on Carousel Dialog")
    }

    override fun featureIcon(lambda: KolibreeDialogDrawable.() -> Unit) {
        FailEarly.fail("Feature Icon is not supported on Carousel Dialog")
    }

    override fun valueListener(lambda: ValueListener<AppCompatDialog, Unit>) {
        FailEarly.fail("Value listener is not supported on Carousel Dialog")
    }

    @Suppress("LongMethod")
    override fun build(): AppCompatDialog {
        return MaterialAlertDialogBuilder(context).run {
            setCancelable(cancellable)
            val bodyLayout =
                LayoutInflater.from(context).inflate(bodyLayout, null) as ConstraintLayout
            val titleTextView: TextView = bodyLayout.safeFind(R.id.title_text)
            val headlineTextView: TextView = bodyLayout.safeFind(R.id.headline_text)
            val carousel: RecyclerView = bodyLayout.safeFind(R.id.carousel)
            val constraintSet = ConstraintSet().apply { clone(bodyLayout) }
            titleTextView.setTitle(constraintSet)
            headlineTextView.setHeadline(constraintSet)
            if (buttons.isNotEmpty()) {
                bodyLayout.addViews(constraintSet, buttons, carousel)
                val bottomMargin = bodyLayout.context.resources.getDimensionPixelOffset(R.dimen.dot_trip)
                constraintSet.setMargin(R.id.carousel, ConstraintSet.BOTTOM, bottomMargin)
            }
            roundDialogCorners(context, background)
            initDialogInset()
            carousel.setAdapter()
            doBuild(bodyLayout, constraintSet)
            setView(bodyLayout)
            constraintSet.applyTo(bodyLayout)
            create()
        }.apply {
            postCreate()
            applyWrapWidth()
        }
    }

    fun onItemBind(action: (ItemBinding<*>, MODEL?) -> Unit) {
        onItemBind = action
    }

    private fun RecyclerView.setAdapter() {
        val itemBinding = ItemBinding.of<MODEL> { itemBinding, _, item ->
            onItemBind(itemBinding, item)
        }

        BindingRecyclerViewAdapters.setAdapter<MODEL>(
            this,
            itemBinding,
            items,
            bindingAdapter,
            null,
            null,
            null
        )
    }
}
