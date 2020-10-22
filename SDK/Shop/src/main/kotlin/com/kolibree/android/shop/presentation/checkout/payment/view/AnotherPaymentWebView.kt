/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.payment.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.BindingAdapter
import java.util.concurrent.atomic.AtomicReference

internal interface AnotherPaymentListener {
    fun onViewPrepared()
}

@SuppressLint("SetJavaScriptEnabled")
internal class AnotherPaymentWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var listenerReference = AtomicReference<AnotherPaymentListener?>(null)

    init {
        settings.javaScriptEnabled = true
        webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                listenerReference.get()?.onViewPrepared()
            }
        }
    }
}

@BindingAdapter("paymentUrl")
internal fun AnotherPaymentWebView.bindPaymentUrl(url: String?) {
    url?.let {
        loadUrl(it)
    }
}

@BindingAdapter("paymentListener")
internal fun AnotherPaymentWebView.bindPaymentListener(listener: AnotherPaymentListener) {
    this.listenerReference.set(listener)
}

@BindingAdapter("paymentUrl")
internal fun WebView.bindPaymentUrl(url: String?) {
    url?.let {
        loadUrl(it)
    }
}
