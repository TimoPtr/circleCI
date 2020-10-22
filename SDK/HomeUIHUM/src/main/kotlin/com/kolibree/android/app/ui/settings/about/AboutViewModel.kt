/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.about

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.account.Account
import com.kolibree.account.AccountFacade
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.settings.about.AboutAnalytics.accountId
import com.kolibree.android.app.ui.settings.about.AboutAnalytics.facebook
import com.kolibree.android.app.ui.settings.about.AboutAnalytics.goBack
import com.kolibree.android.app.ui.settings.about.AboutAnalytics.instagram
import com.kolibree.android.app.ui.settings.about.AboutAnalytics.licenses
import com.kolibree.android.app.ui.settings.about.AboutAnalytics.twitter
import com.kolibree.android.app.ui.settings.about.AboutAnalytics.website
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.ui.settings.SecretSettingsManager
import com.kolibree.android.utils.CopyToClipboardUseCase
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class AboutViewModel(
    initialViewState: AboutViewState?,
    private val accountFacade: AccountFacade,
    private val aboutNavigator: AboutNavigator,
    private val secretSettingsManager: SecretSettingsManager,
    private val copyToClipboardUseCase: CopyToClipboardUseCase,
    appVersions: KolibreeAppVersions
) : BaseViewModel<AboutViewState, AboutActions>(
    initialViewState ?: AboutViewState.withAppVersions(appVersions)
) {

    @VisibleForTesting
    internal var secretSettingsCount = 0

    val appVersion: LiveData<String> = map(viewStateLiveData) { viewState ->
        viewState?.appVersion ?: ""
    }

    val accountId: LiveData<String> = map(viewStateLiveData) { viewState ->
        viewState?.accountId ?: ""
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        disposeOnStop {
            accountFacade.getAccountSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::refreshAccountId, Timber::e)
        }
    }

    private fun refreshAccountId(account: Account) {
        updateViewState { copy(accountId = account.pubId) }
    }

    fun onCloseClick() {
        Analytics.send(goBack())

        aboutNavigator.closeScreen()
    }

    fun onFacebookClick() {
        Analytics.send(facebook())

        aboutNavigator.showFacebookPage()
    }

    fun onTwitterClick() {
        Analytics.send(twitter())

        aboutNavigator.showTwitterPage()
    }

    fun onInstagramClick() {
        Analytics.send(instagram())

        aboutNavigator.showInstagramPage()
    }

    fun onWebsiteClick() {
        Analytics.send(website())

        aboutNavigator.showColgateWebsite()
    }

    fun onLicensesClick() {
        Analytics.send(licenses())

        aboutNavigator.showLicensesPage()
    }

    fun onLogoClick() {
        secretSettingsCount++
        if (secretSettingsCount >= SECRET_SETTINGS_ON_COUNT) {
            secretSettingsManager.enableSecretSettings()
            pushAction(AboutActions.ShowSecretSettingsEnabled)
        }
    }

    fun onAccountIdClick() {
        val id = getViewState()?.accountId ?: run {
            FailEarly.fail("Account id not available!")
            return
        }

        Analytics.send(accountId())
        copyToClipboardUseCase.copy(id)
        pushAction(AboutActions.AccountIdCopied)
    }

    class Factory @Inject constructor(
        private val accountFacade: AccountFacade,
        private val aboutNavigator: AboutNavigator,
        private val secretSettingsManager: SecretSettingsManager,
        private val copyToClipboardUseCase: CopyToClipboardUseCase,
        private val appVersions: KolibreeAppVersions
    ) : BaseViewModel.Factory<AboutViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AboutViewModel(
                viewState,
                accountFacade,
                aboutNavigator,
                secretSettingsManager,
                copyToClipboardUseCase,
                appVersions
            ) as T
    }
}

private const val SECRET_SETTINGS_ON_COUNT = 6
