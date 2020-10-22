/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.feedback

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.databinding.BindingAdapter
import com.kolibree.android.baseui.hum.R

@Keep
class FeedbackMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val feedbackContainer: View
    private val feedbackImg: ImageView
    private val feedbackMessage: TextView

    private val defaultImg: ImageView
    private val defaultMessage: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_hum_feedback_message, this, true)
        feedbackContainer = findViewById(R.id.feedback_container)
        feedbackMessage = findViewById(R.id.feedback_message)
        feedbackImg = findViewById(R.id.feedback_img)

        defaultMessage = findViewById(R.id.default_message)
        defaultImg = findViewById(R.id.default_img)

        attrs?.also {
            context.withStyledAttributes(it, R.styleable.FeedbackMessageView, defStyleAttr) {
                val image = getDrawable(
                    R.styleable.FeedbackMessageView_defaultIcon
                ) ?: ContextCompat.getDrawable(context, R.drawable.ic_feedback_all_good)
                val message = getString(R.styleable.FeedbackMessageView_defaultMessage)

                defaultImg.setImageDrawable(image)
                defaultMessage.text = message
            }
        }

        feedbackContainer.visibility = View.GONE
    }

    fun setMessage(feedBackMessage: FeedbackMessageResource) {
        if (!feedBackMessage.shouldShow) {
            feedbackContainer.visibility = View.GONE
            return
        }

        feedbackImg.setImageResource(feedBackMessage.imageId)
        feedbackMessage.text = context.getString(feedBackMessage.message)
        feedbackContainer.visibility = View.VISIBLE
    }
}

@Keep
@BindingAdapter("feedback")
fun FeedbackMessageView.setFeedback(
    feedBackMessage: FeedbackMessageResource
) {
    setMessage(feedBackMessage)
}
