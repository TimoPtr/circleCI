/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.databinding.bindingadapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.Keep
import androidx.annotation.StringDef
import androidx.annotation.StringRes
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import com.kolibree.android.failearly.FailEarly
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber

@Keep
@BindingAdapter("textColorResource")
fun TextView.setTextColorResource(@ColorRes color: Int) {
    setTextColor(context.getColor(color))
}

@Keep
@BindingAdapter(
    value = ["drawableStartCompat", "drawableTopCompat", "drawableEndCompat", "drawableBottomCompat"],
    requireAll = false
)
fun TextView.setCompoundDrawablesCompat(
    startCompoundDrawable: Drawable?,
    topCompoundDrawable: Drawable?,
    endCompoundDrawable: Drawable?,
    bottomCompoundDrawable: Drawable?
) {
    setCompoundDrawablesWithIntrinsicBounds(
        startCompoundDrawable,
        topCompoundDrawable,
        endCompoundDrawable,
        bottomCompoundDrawable
    )
}

/**
 * Supports adding a single remote compound drawable to a TextView using Picasso
 */
@Keep
@BindingAdapter(value = ["position", "imageUrl", "placeholder"], requireAll = false)
fun TextView.setCompoundDrawable(
    @CompoundDrawablePosition position: String,
    url: String?,
    placeHolder: Drawable?
) {
    if (url.isNullOrEmpty()) {
        setCompoundDrawableAtPosition(this, position, placeHolder)
    } else {
        var picassoRequest = Picasso.get()
            .load(url)

        if (placeHolder != null) {
            picassoRequest = picassoRequest
                .placeholder(placeHolder)
        }

        picassoRequest.into(object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                setCompoundDrawableAtPosition(
                    this@setCompoundDrawable,
                    position,
                    placeHolderDrawable
                )
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                if (placeHolder == null) {
                    setCompoundDrawableAtPosition(this@setCompoundDrawable, position, placeHolder)
                }

                Timber.v(e)
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    val drawable = BitmapDrawable(context.resources, bitmap)

                    setCompoundDrawableAtPosition(this@setCompoundDrawable, position, drawable)
                }
            }
        })
    }
}

private fun setCompoundDrawableAtPosition(
    textView: TextView,
    @CompoundDrawablePosition position: String,
    drawable: Drawable?
) {
    when (position) {
        POSITION_LEFT -> textView.setCompoundDrawablesWithIntrinsicBounds(
            drawable,
            null,
            null,
            null
        )
        POSITION_TOP -> textView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
        POSITION_RIGHT -> textView.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            drawable,
            null
        )
        POSITION_BOTTOM -> textView.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            null,
            drawable
        )
    }
}

const val POSITION_LEFT = "left"
const val POSITION_RIGHT = "right"
const val POSITION_TOP = "top"
const val POSITION_BOTTOM = "bottom"

@StringDef(POSITION_LEFT, POSITION_RIGHT, POSITION_TOP, POSITION_BOTTOM)
@Retention(AnnotationRetention.SOURCE)
@Keep
annotation class CompoundDrawablePosition

@SuppressLint("DefaultLocale")
@Keep
@BindingAdapter(value = ["android:text", "textHighlight", "textHighlightColor"], requireAll = true)
fun TextView.setHighlightText(fullText: String, highlight: String, @ColorInt color: Int) {
    val startIndex = fullText.indexOf(highlight, ignoreCase = true)
    text = if (startIndex >= 0) {
        val spannable = SpannableString(fullText)
        val endIndex = startIndex + highlight.length
        spannable.setSpan(
            ForegroundColorSpan(color),
            startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spannable
    } else {
        FailEarly.fail(
            "Highlight string `$highlight` is not present in full string `$fullText`, " +
                "please check your translations!"
        )
        fullText
    }
}

@SuppressLint("DefaultLocale")
@Keep
@BindingAdapter(value = ["android:text", "textHighlight", "textHighlightColor"], requireAll = true)
fun TextView.setHighlightText(@StringRes fullText: Int, highlight: String, @ColorInt color: Int) {
    setHighlightText(context.getString(fullText), highlight, color)
}

@Keep
@BindingAdapter(value = ["android:text", "textHighlight", "textHighlightColor"], requireAll = true)
fun TextView.setHighlightText(
    @StringRes fullText: Int,
    @StringRes highlight: Int,
    @ColorInt color: Int
) {
    setHighlightText(resources.getString(fullText), resources.getString(highlight), color)
}

@Keep
@BindingAdapter(value = ["htmlText"])
fun TextView.setHtmlText(htmlText: String) {
    text = Html.fromHtml(htmlText)
}

@Keep
@BindingAdapter("android:drawableTint")
fun TextView.setDrawableTintColor(@ColorInt color: Int) {
    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(color))
}

@Keep
@BindingAdapter("underlineText")
fun TextView.setUnderlineText(underlineText: Boolean) {
    paintFlags = if (underlineText) {
        paintFlags or Paint.UNDERLINE_TEXT_FLAG
    } else {
        paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
    }
}
