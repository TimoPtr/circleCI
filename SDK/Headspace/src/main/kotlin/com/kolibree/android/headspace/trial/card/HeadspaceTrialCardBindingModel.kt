/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.text.CustomTypefaceSpan
import com.kolibree.android.headspace.R
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@VisibleForApp
@Parcelize
data class HeadspaceTrialCardBindingModel(
    val data: HeadspaceTrialCardViewState,
    override val layoutId: Int = R.layout.card_headspace_trial
) : DynamicCardBindingModel(data), HeadSpaceTrialState by data {
    @IgnoredOnParcel
    private val applySemiBoldUseCase = ApplySemiBoldUseCase()

    @DrawableRes
    @IgnoredOnParcel
    val toggleDescriptionVisibilityIcon: Int =
        if (isDescriptionVisible) {
            R.drawable.ic_icon_navigation_expand_up_24_px
        } else {
            R.drawable.ic_icon_navigation_expand_down_24_px
        }

    @IgnoredOnParcel
    val isCallToActionVisible: Boolean = data.isUnlocked || data.isUnlockable

    fun copyCodeButtonText(context: Context): CharSequence {
        return if (copiedToClipboard) {
            codeCopiedButtonText(context)
        } else {
            tapToCodeButtonText(context)
        }
    }

    @SuppressLint("ExperimentalClassUse")
    private fun codeCopiedButtonText(context: Context): CharSequence {
        val text = context.getString(R.string.headspace_card_copied)
        val spannableString = SpannableString(text)

        applySemiBoldUseCase.applySemiBoldSpan(context, spannableString, text)

        return spannableString
    }

    private fun tapToCodeButtonText(context: Context): CharSequence {
        return data.discountCode?.let { discountCode ->
            val spannable = SpannableString(
                context.getString(R.string.headspace_card_tap_to_copy, discountCode)
            )

            applySemiBoldUseCase.applySemiBoldSpan(
                context,
                spannable,
                discountCode
            )

            spannable
        } ?: ""
    }

    fun callToActionText(context: Context): CharSequence {
        return context.getString(
            if (data.isUnlocked) R.string.headspace_card_visit_headspace
            else R.string.headspace_card_unlock_button
        )
    }

    fun descriptionText(context: Context): CharSequence {
        val description = SpannableString(context.getString(R.string.headspace_card_description))

        applySemiBoldUseCase.applySemiBoldSpan(
            context,
            description,
            context.getString(R.string.headspace_card_inprogress_description_highlight1),
            context.getString(R.string.headspace_card_inprogress_description_highlight2)
        )

        return description
    }

    /**
     * If Headspace trial is not unlocked, returns a [CharSequence] detailing the number of points
     * pending to unlock the card
     *
     * If Headspace trial is unlocked, returns an empty String
     */
    fun mutableDescriptionText(context: Context): CharSequence =
        when {
            isProgressVisible -> progressVisibleDescription(context)
            data.isUnlockable -> unlockableVisibileDescription(context)
            data.isUnlocked -> unlockedVisibleDescription(context)
            else -> ""
        }

    /**
     * @return [CharSequence] detailing the number of points pending to unlock the card and applying
     * Zeplin's style (https://zpl.io/amoW0K3)
     */
    private fun progressVisibleDescription(context: Context): CharSequence {
        val pointsAsString = context.getString(
            R.string.headspace_card_progress_points_to_unlock,
            data.pointsNeeded.toString()
        )

        val pointsToUnlockSpannable = SpannableString(
            context.getString(R.string.headspace_card_progress_to_unlock, pointsAsString)
        )

        applySemiBoldUseCase.applySemiBoldSpan(
            context,
            pointsToUnlockSpannable,
            pointsAsString
        )

        return pointsToUnlockSpannable
    }

    private fun unlockableVisibileDescription(context: Context): CharSequence {
        return context.getString(R.string.headspace_card_unlock_description)
    }

    private fun unlockedVisibleDescription(context: Context): CharSequence {
        val description =
            SpannableString(context.getString(R.string.headspace_card_unlocked_description))

        applySemiBoldUseCase.applySemiBoldSpan(
            context,
            description,
            context.getString(R.string.headspace_card_unlocked_description_highlight)
        )

        return description
    }
}

private class ApplySemiBoldUseCase {
    private lateinit var pointsNeededFont: Typeface

    fun applySemiBoldSpan(
        context: Context,
        spannableString: SpannableString,
        vararg stringsToSpan: String
    ) {
        maybeLoadSpanFont(context)

        stringsToSpan.forEach { stringToSpan ->
            val stringToSpanStart = spannableString.indexOf(stringToSpan)

            if (stringToSpanStart != -1) {
                val stringToSpanEnd = stringToSpanStart + stringToSpan.length

                spannableString.setSpan(
                    CustomTypefaceSpan(pointsNeededFont),
                    stringToSpanStart,
                    stringToSpanEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun maybeLoadSpanFont(context: Context) {
        if (!::pointsNeededFont.isInitialized) {
            pointsNeededFont = ResourcesCompat.getFont(context, R.font.hind_semibold)
                ?: throw IllegalStateException("Missing hind_semibold font")
        }
    }
}
