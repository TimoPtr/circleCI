/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.dialogs

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.kolibree.BR
import com.kolibree.R
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.app.ui.dialog.carouselDialog
import com.kolibree.android.app.ui.dialog.durationDialog
import com.kolibree.android.app.ui.dialog.multiSelectDialog
import com.kolibree.android.app.ui.dialog.singleSelectDialog
import com.kolibree.android.app.ui.dialog.textInputDialog
import com.kolibree.databinding.ActivityDialogsPlaygroundBinding
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.BRUSHING_GOAL_TIME_STEP_SECONDS
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MAXIMUM_BRUSHING_GOAL_TIME_SECONDS
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
import org.threeten.bp.Duration
import timber.log.Timber

@Suppress("LargeClass")
internal class DialogsPlaygroundActivity : BaseMVIActivity<
    DialogsPlaygroundViewState,
    DialogsPlaygroundActions,
    DialogsPlaygroundViewModel.Factory,
    DialogsPlaygroundViewModel,
    ActivityDialogsPlaygroundBinding
    >() {

    override fun getViewModelClass(): Class<DialogsPlaygroundViewModel> =
        DialogsPlaygroundViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_dialogs_playground

    @Suppress("LongMethod", "ComplexMethod")
    override fun execute(action: DialogsPlaygroundActions) =
        when (action) {
            is DialogsPlaygroundActions.AlertWithStrings -> {
                Timber.d("Alert With Strings")
                alertWithStrings()
            }
            is DialogsPlaygroundActions.AlertWithStringIds -> {
                Timber.d("Alert With Strings ID")
                alertWithStringIds()
            }
            is DialogsPlaygroundActions.TextInputDialog -> {
                Timber.d("Alert With Strings ID")
                textInput()
            }
            is DialogsPlaygroundActions.AlertWithTintedFeatureImage -> {
                Timber.d("Alert With Tinted Feature Image")
                alertWithYellowTintedFeatureImage()
            }
            is DialogsPlaygroundActions.AlertWithFeatureImageId -> {
                Timber.d("Alert With Feature Image")
                alertWithFeatureImageId()
            }
            is DialogsPlaygroundActions.AlertWithStylizedHeadline -> {
                Timber.d("Alert With Stylized Headline & Feature Image Id")
                alertWithStylizedHeadline()
            }
            is DialogsPlaygroundActions.SingleSelectWithButtons -> {
                Timber.d("Single-Select With Buttons")
                singleSelectWithButtons()
            }
            is DialogsPlaygroundActions.SingleSelectWithoutButtons -> {
                Timber.d("Single-Select Without Buttons")
                singleSelectWithoutButtons()
            }
            is DialogsPlaygroundActions.MultiSelectWithStrings -> {
                Timber.d("Multi-select With Strings")
                multiSelectWithStrings()
            }
            is DialogsPlaygroundActions.DurationDialog -> {
                Timber.d("Duration")
                durationDialog()
            }
            is DialogsPlaygroundActions.AlertWithIcon -> {
                Timber.d("Icon")
                iconDialog()
            }
            is DialogsPlaygroundActions.Carousel -> {
                Timber.d("Carousel")
                carousel()
            }
        }

    private fun alertWithStrings() {
        alertDialog(this) {
            title("Test")
            body("Hello World!")
            containedButton {
                title("Contained Button")
                action { println("Contained Button: Pressed") }
            }
            outlinedButton {
                title("Outlined Button")
                action { println("Outlined Button: Pressed") }
            }
        }.show()
    }

    private fun alertWithStringIds() {
        alertDialog(this) {
            title("Toothbrush ID")
            body("Help Center")
            containedButton {
                title("Contained Button")
                action { println("Contained Button: Pressed") }
            }
            textButton {
                title("Text Button")
                action { println("Text Button: Pressed") }
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun textInput() {
        textInputDialog(this, "Initial Value") {
            title("Toothbrush ID")
            body("Help Center")
            textInput {
                hintText("Hint Text")
            }
            containedButton {
                title("Contained Button")
                action { value ->
                    println("Contained Button: Pressed; Input Text: $value")
                }
            }
            textButton {
                title("Text Button")
                action { value ->
                    println("Text Button: Pressed; Input Text: $value")
                }
            }
        }.show()
    }

    private fun alertWithYellowTintedFeatureImage() {
        alertDialog(this) {
            featureImage {
                drawable(getDrawable(R.drawable.dialog_feature_image)!!)
                tint(Color.YELLOW)
                scaleType(ImageView.ScaleType.FIT_CENTER)
            }
            body("Tinted Feature Image")
            containedButton {
                title("Contained Button")
                action { println("Contained Button: Pressed") }
            }
            textButton {
                title("Text Button")
                action { println("Text Button: Pressed") }
            }
        }.show()
    }

    private fun alertWithFeatureImageId() {
        alertDialog(this) {
            featureImage {
                drawable(R.drawable.dialog_feature_image)
            }
            body("Feature Image")
            containedButton {
                title("Contained Button")
                action { println("Contained Button: Pressed") }
            }
            textButton {
                title("Text Button")
                action { println("Text Button: Pressed") }
            }
        }.show()
    }

    private fun alertWithStylizedHeadline() {
        alertDialog(this) {
            featureImage {
                drawable(R.drawable.dialog_feature_image)
            }
            headlineText {
                text("Where do you live?")
            }
            body("Feature Image")
            containedButton {
                title("Contained Button")
                action { println("Contained Button: Pressed") }
            }
            textButton {
                title("Text Button")
                action { println("Text Button: Pressed") }
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun singleSelectWithButtons() {
        singleSelectDialog(this) {
            title("Test")
            body("Hello World!")
            selectAction {
                println("Selection: ${it.text}")
            }
            selection {
                title("Option 1")
                selected = true
            }
            selection {
                title("Option 2")
            }
            containedButton {
                title("Contained Button")
                action { println("Contained Button: Pressed") }
            }
            textButton {
                title("Text Button")
                action { println("Text Button: Pressed") }
            }
        }.show()
    }

    private fun singleSelectWithoutButtons() {
        singleSelectDialog(this) {
            title("Test")
            body("Hello World!")
            selectAction {
                println("Contained: ${it.text}")
            }
            selection {
                title("Option 1")
                selected = true
            }
            selection {
                title("Option 2")
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun multiSelectWithStrings() {
        multiSelectDialog(this) {
            title("Test")
            body("Hello World!")
            selectAction { selection ->
                println("Selected: ${selection.joinToString { it.text }}")
            }
            selection {
                title("Option 1")
                selected = true
            }
            selection {
                title("Option 2")
            }
            containedButton {
                title("Contained Button")
                action { println("Contained Button: Pressed") }
            }
            textButton {
                title("Text Button")
                action { println("Text Button: Pressed") }
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun durationDialog() {
        durationDialog(this, Duration.ofSeconds(INITIAL_BRUSHING_TIME)) {
            title("Duration")
            body("Enter your brushing duration")
            majorLabel("minutes")
            minorLabel("seconds")
            setRange(
                Duration.ofSeconds(MINIMUM_BRUSHING_GOAL_TIME_SECONDS.toLong()),
                Duration.ofSeconds(MAXIMUM_BRUSHING_GOAL_TIME_SECONDS.toLong()),
                Duration.ofSeconds(BRUSHING_GOAL_TIME_STEP_SECONDS.toLong())
            )
            containedButton {
                title("Save")
                action { value ->
                    println("Contained Button: Pressed: $value")
                }
            }
            textButton {
                title("Cancel")
                action { println("Text Button: Pressed") }
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun iconDialog() {
        alertDialog(this) {
            body("Some text")
            featureIcon {
                drawable(R.drawable.ic_nav_close)
            }
            iconContainedButton {
                title("Forget")
                icon(R.drawable.ic_nav_close)
                action { value ->
                    println("Contained Button: Pressed: $value")
                }
            }
            textButton {
                title("Cancel")
                action { println("Text Button: Pressed") }
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun carousel() {
        carouselDialog<SampleCarouselItem>(context = this) {
            headlineText {
                text("Aenean at magna lectus")
                gravity(Gravity.CENTER)
            }

            body(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                gravity = Gravity.CENTER
            )

            items = (0..SAMPLE_CAROUSEL_ITEM_COUNT).map {
                SampleCarouselItem(
                    "Brush $it",
                    R.drawable.ic_toothbrush_plq
                )
            }

            val interaction = object : SampleCarouselItemInteraction {
                override fun onItemClick(item: SampleCarouselItem) {
                    println("Item clicked: $item")
                    dismiss()
                }
            }

            onItemBind { binding, _ ->
                binding.set(BR.item, R.layout.item_sample_carousel_dialog)
                binding.bindExtra(BR.interaction, interaction)
            }
        }.show()
    }

    data class SampleCarouselItem(
        val name: String,
        @DrawableRes val iconRes: Int
    )

    interface SampleCarouselItemInteraction {
        fun onItemClick(item: SampleCarouselItem)
    }

    companion object {
        private const val INITIAL_BRUSHING_TIME = 180L
        private const val SAMPLE_CAROUSEL_ITEM_COUNT = 8
    }
}

fun startDialogsPlaygroundActivity(context: Context) =
    context.startActivity(Intent(context, DialogsPlaygroundActivity::class.java))
