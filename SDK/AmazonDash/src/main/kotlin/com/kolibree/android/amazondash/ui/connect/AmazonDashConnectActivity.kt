/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.ui.connect

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spanned
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.kolibree.android.amazondash.R
import com.kolibree.android.amazondash.databinding.ActivityAmazonDashConnectBinding
import com.kolibree.android.amazondash.ui.AmazonDashAnalytics
import com.kolibree.android.amazondash.ui.connect.AmazonDashConnectAction.SetResult
import com.kolibree.android.amazondash.ui.connect.AmazonDashConnectAction.ShowError
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.widget.snackbar.showErrorSnackbar
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class AmazonDashConnectActivity : BaseMVIActivity<
    AmazonDashConnectViewState,
    AmazonDashConnectAction,
    AmazonDashConnectViewModel.Factory,
    AmazonDashConnectViewModel,
    ActivityAmazonDashConnectBinding>(),
    TrackableScreen {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.onNewIntent(intent)
    }

    override fun getViewModelClass(): Class<AmazonDashConnectViewModel> =
        AmazonDashConnectViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_amazon_dash_connect

    override fun execute(action: AmazonDashConnectAction) {
        when (action) {
            is ShowError -> binding.root.showErrorSnackbar(action.error)
            is SetResult -> setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(EXTRA_SEND_REQUEST_TIME, action.sendRequestTime)
            })
        }
    }

    override fun getScreenName(): AnalyticsEvent = AmazonDashAnalytics.connect()
}

internal const val EXTRA_SEND_REQUEST_TIME = "EXTRA_SEND_REQUEST_TIME"

@BindingAdapter("spannedText")
internal fun TextView.setSpannedText(getSpanned: ((Context) -> Spanned)?) {
    text = getSpanned?.invoke(context) ?: ""
}

internal fun createAmazonDashConnectIntent(context: Context): Intent {
    return Intent(context, AmazonDashConnectActivity::class.java)
}

@VisibleForApp
@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun startAmazonDashConnectActivity(context: Context) {
    context.startActivity(createAmazonDashConnectIntent(context))
}
