/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import com.kolibree.android.app.ui.text.ClickableStringResource
import com.kolibree.android.app.ui.text.ClickableText
import com.kolibree.android.app.ui.text.TextPaintModifiers
import com.kolibree.android.app.ui.text.addLink

@Keep
@BindingAdapter(value = ["linkText", "linkStyle", "onLinkClick"], requireAll = false)
fun TextView.setClickableLink(
    text: String?,
    textPaintModifiers: TextPaintModifiers? = null,
    onClickListener: View.OnClickListener?
) {
    if (text != null && onClickListener != null) {
        addLink(ClickableText(text, textPaintModifiers, onClickListener))
    }
}

@Keep
@BindingAdapter(value = ["linkText", "linkStyle", "onLinkClick"], requireAll = false)
fun TextView.setClickableLink(
    @StringRes text: Int?,
    textPaintModifiers: TextPaintModifiers? = null,
    onClickListener: View.OnClickListener?
) {
    if (text != null && onClickListener != null) {
        addLink(ClickableStringResource(text, textPaintModifiers, onClickListener))
    }
}
