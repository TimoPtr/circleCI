/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.addprofile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.extention.showInBrowser
import com.kolibree.android.app.ui.input.hideSoftInput
import com.kolibree.android.app.ui.selectavatar.SelectAvatarDialogFragment
import com.kolibree.android.app.ui.text.TextPaintModifiers
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityAddProfileBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class AddProfileActivity : BaseMVIActivity<
        AddProfileViewState,
        AddProfileActions,
        AddProfileViewModel.Factory,
        AddProfileViewModel,
        ActivityAddProfileBinding>(),
    TrackableScreen {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.linkStyle = TextPaintModifiers.Builder()
            .withBoldText(true)
            .withUnderlineText(true)
            .build()
    }

    override fun getViewModelClass(): Class<AddProfileViewModel> =
        AddProfileViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_add_profile

    override fun getScreenName(): AnalyticsEvent = AddProfileAnalytics.main()

    override fun execute(action: AddProfileActions) {
        when (action) {
            is AddProfileActions.HideSoftInput -> binding.nameInputField.hideSoftInput()
            is AddProfileActions.OpenTermsAndConditions -> showInBrowser(R.string.terms_url)
            is AddProfileActions.OpenPrivacyPolicy -> showInBrowser(R.string.privacy_url)
            is AddProfileActions.OpenChooseAvatarDialog ->
                SelectAvatarDialogFragment.showIfNotPresent(supportFragmentManager, showCurrentProfile = false)
        }
    }
}

@Suppress("SdkPublicExtensionMethodWithoutKeep")
@VisibleForApp
fun startAddProfileIntent(context: Context): Intent = Intent(context, AddProfileActivity::class.java)

@Suppress("SdkPublicExtensionMethodWithoutKeep")
@VisibleForApp
fun startAddProfileActivity(context: Context) {
    context.startActivity(startAddProfileIntent(context))
}
