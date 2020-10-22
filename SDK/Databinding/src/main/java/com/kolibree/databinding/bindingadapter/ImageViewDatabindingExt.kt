/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.content.res.ColorStateList
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.RawRes
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kolibree.android.app.ui.dialog.colorIntFromAttribute
import com.kolibree.android.app.utils.EspressoUtils
import com.kolibree.android.app.utils.loop
import com.kolibree.android.failearly.FailEarly
import com.kolibree.databinding.R
import com.squareup.picasso.Picasso
import timber.log.Timber

/**
 * There is no default adapter for @DrawableRes, we need to add one
 */
@Keep
@BindingAdapter("android:src")
fun ImageView.bindImageResource(@DrawableRes resource: Int) {
    setImageResource(resource)
}

@Keep
@BindingAdapter("saturation")
fun ImageView.setSaturation(saturation: Float) {
    if (saturation !in 0f..1f) {
        FailEarly.fail("Saturation has to be in range <0, 1>") {
            when {
                saturation < 0f -> setSaturation(0f)
                saturation > 1f -> setSaturation(1f)
            }
        }
    } else {
        val matrix = ColorMatrix()
        matrix.setSaturation(saturation)
        val filter = ColorMatrixColorFilter(matrix)
        colorFilter = filter
    }
}

@Keep
@BindingAdapter(value = ["imageUrl", "placeholder"], requireAll = false)
fun ImageView.setImageUrl(
    previousUrl: String?,
    previousPlaceHolder: Drawable?,
    url: String?,
    placeHolder: Drawable?
) {
    if (url.isNullOrEmpty()) {
        Picasso.get().cancelRequest(this)
        if (placeHolder != null) {
            setImageDrawable(placeHolder)
        }
    } else if (url != previousUrl) {
        loadUrlInto(url, this, placeHolder)
    }
}

private fun loadUrlInto(url: String, destinationView: ImageView, placeHolder: Drawable?) {
    Picasso.get().cancelRequest(destinationView)
    var picassoRequest = Picasso.get()
        .load(url)
        .fit()
        .centerInside()

    if (placeHolder != null) {
        picassoRequest = picassoRequest
            .placeholder(placeHolder)
    }

    picassoRequest.into(destinationView)
}

@Keep
@BindingAdapter(value = ["animatedGif", "gifInCircle"], requireAll = false)
fun ImageView.loadAnimatedGif(@RawRes gifId: Int?, gifInCircle: Boolean? = false) {
    gifId?.let {
        val request = Glide.with(this)
            .asGif()
            .load(it)

        if (gifInCircle == true)
            request.apply(RequestOptions.circleCropTransform()).into(this)
        else request.into(this)
    }
}

@Keep
@BindingAdapter("animatedVectorDrawable")
fun ImageView.loadAnimatedVectorDrawable(@DrawableRes drawableRes: Int) =
    AnimatedVectorDrawableCompat
        .create(context, drawableRes)
        ?.let {
            setImageDrawable(it)
            if (EspressoUtils.areSystemAnimationsEnabled(context)) {
                it.loop()
            }
        }
        ?: Timber.e("AnimatedVectorDrawableCompat.create() returned null")

@Keep
@BindingAdapter("tint")
fun ImageView.bindTint(color: Int) {
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
}

@Keep
@BindingAdapter("tintAttr")
fun ImageView.setTintColor(@AttrRes attrId: Int) {
    if (attrId != 0) {
        val color = colorIntFromAttribute(context, attrId)
        setTag(R.attr.tint, color)
        bindTint(color)
    } else {
        ImageViewCompat.setImageTintList(this, null)
    }
}
