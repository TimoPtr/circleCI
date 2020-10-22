/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.feedback

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kolibree.android.coachplus.R

internal class FeedbackMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val feedbackContainer: View
    private val feedbackImg: ImageView
    private val feedbackMessage: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.feedback_message_view, this, true)
        feedbackContainer = findViewById(R.id.feedback_container)
        feedbackMessage = findViewById(R.id.feedback_message)
        feedbackImg = findViewById(R.id.feedback_img)

        feedbackContainer.visibility = View.GONE
    }

    fun setMessage(feedBackMessage: FeedBackMessage) {
        setWarningShadow(feedBackMessage.warningLevel)

        if (!feedBackMessage.shouldShow) {
            feedbackContainer.visibility = View.GONE
            return
        }

        val feedBackMessageResources = FeedBackMessageResources.from(feedBackMessage)

        loadImage(feedBackMessageResources.imageId)
        feedbackMessage.text = context.getString(feedBackMessageResources.message)
        feedbackContainer.visibility = View.VISIBLE
    }

    private fun loadImage(@RawRes @DrawableRes image: Int = R.drawable.image_placeholder) {
        Glide.with(feedbackImg)
            .load(image)
            .apply(RequestOptions.circleCropTransform())
            .into(feedbackImg)
    }

    private fun setWarningShadow(warningLevel: FeedbackWarningLevel) =
        when (warningLevel) {
            FeedbackWarningLevel.None -> setBackgroundColor(Color.TRANSPARENT)
            FeedbackWarningLevel.Normal -> setBackgroundResource(R.drawable.bg_feedback_warning)
            FeedbackWarningLevel.Severe,
            FeedbackWarningLevel.Critical ->
                setBackgroundResource(R.drawable.bg_feedback_warning_severe)
        }
}

@BindingAdapter("feedback")
internal fun FeedbackMessageView.setFeedback(feedBackMessage: FeedBackMessage?) {
    setMessage(feedBackMessage ?: FeedBackMessage.EmptyFeedback)
}
