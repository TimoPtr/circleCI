/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.ui

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.ImageViewTarget
import com.kolibree.android.guidedbrushing.tips.R
import com.kolibree.android.guidedbrushing.ui.adapter.BrushingTipsData

internal class TipImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val image: ImageView
    private val description: TextView
    private val updateAngleHandler: Handler = Handler()
    private var gifDrawable: GifDrawable? = null
    private var tipData: BrushingTipsData? = null

    init {
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.tip_player_view, this, true)
        image = root.findViewById(R.id.tip_image)
        description = root.findViewById(R.id.tip_description)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postUpdateAngleDescription()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateAngleHandler.removeCallbacksAndMessages(null)
    }

    fun refresh(data: BrushingTipsData) {
        tipData = data

        Glide.with(context)
            .asGif()
            .load(data.animationRes)
            .into(object : ImageViewTarget<GifDrawable?>(image) {
                override fun setResource(resource: GifDrawable?) {
                    resource?.let {
                        gifDrawable = it
                        image.setImageDrawable(it)
                        postUpdateAngleDescription()
                    }
                }
            })
    }

    private fun postUpdateAngleDescription() {
        updateAngleHandler.removeCallbacksAndMessages(null)

        tipData?.let {
            val isPerfectAngle = isPerfectAngle(it.perfectMessageAfterFrame)
            @StringRes val descriptionRes = if (isPerfectAngle) it.perfectMessageRes else it.wrongMessageRes
            description.text = context.getString(descriptionRes)
        }

        updateAngleHandler.postDelayed(
            this::postUpdateAngleDescription,
            UPDATE_DESCRIPTION_INTERVAL
        )
    }

    private fun isPerfectAngle(firstPerfectAngleFrame: Int): Boolean {
        val currentFrame = gifDrawable?.frameIndex ?: 0
        return currentFrame > firstPerfectAngleFrame
    }
}

private const val UPDATE_DESCRIPTION_INTERVAL = 100L

@BindingAdapter("tipData")
internal fun TipImageView.bindData(data: BrushingTipsData?) {
    data?.let(::refresh)
}
