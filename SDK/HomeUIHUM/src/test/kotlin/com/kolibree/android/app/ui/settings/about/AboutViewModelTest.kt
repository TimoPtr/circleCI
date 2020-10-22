/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.about

import androidx.lifecycle.Lifecycle
import com.kolibree.account.Account
import com.kolibree.account.AccountFacade
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.ui.settings.SecretSettingsManager
import com.kolibree.android.utils.CopyToClipboardUseCase
import com.kolibree.android.utils.KolibreeAppVersions
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

internal class AboutViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: AboutViewModel

    private val accountFacade: AccountFacade = mock()
    private val navigator: AboutNavigator = mock()
    private val secretSettingsManager: SecretSettingsManager = mock()
    private val copyToClipboardUseCase: CopyToClipboardUseCase = mock()

    override fun setup() {
        super.setup()

        viewModel = AboutViewModel(
            initialViewState = null,
            accountFacade = accountFacade,
            aboutNavigator = navigator,
            appVersions = KolibreeAppVersions(APP_VERSION, BUILD_VERSION),
            secretSettingsManager = secretSettingsManager,
            copyToClipboardUseCase = copyToClipboardUseCase
        )
    }

    @Test
    fun `appVersionName is composed from appVersions`() {
        assertEquals("$APP_VERSION ($BUILD_VERSION)", viewModel.getViewState()?.appVersion)
    }

    @Test
    fun `onStart updates view state with accountId value`() {
        val testId = "123456"
        whenever(accountFacade.getAccountSingle())
            .thenReturn(Single.just(mockAccount(testId)))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        assertEquals(testId, viewModel.getViewState()?.accountId)
    }

    @Test
    fun `onCloseClick closes screen`() {
        viewModel.onCloseClick()

        verify(navigator).closeScreen()
    }

    @Test
    fun `onCloseClick sends goBack event`() {
        viewModel.onCloseClick()

        verify(eventTracker).sendEvent(AboutAnalytics.goBack())
    }

    @Test
    fun `onFacebookClick shows FB page`() {
        viewModel.onFacebookClick()

        verify(navigator).showFacebookPage()
    }

    @Test
    fun `onFacebookClick sends facebook event`() {
        viewModel.onFacebookClick()

        verify(eventTracker).sendEvent(AboutAnalytics.facebook())
    }

    @Test
    fun `onTwitterClick shows Twitter page`() {
        viewModel.onTwitterClick()

        verify(navigator).showTwitterPage()
    }

    @Test
    fun `onTwitterClick sends twitter event`() {
        viewModel.onTwitterClick()

        verify(eventTracker).sendEvent(AboutAnalytics.twitter())
    }

    @Test
    fun `onInstagramClick shows instagram page`() {
        viewModel.onInstagramClick()

        verify(navigator).showInstagramPage()
    }

    @Test
    fun `onInstagramClick sends instagram event`() {
        viewModel.onInstagramClick()

        verify(eventTracker).sendEvent(AboutAnalytics.instagram())
    }

    @Test
    fun `onWebsiteClick shows Colgate shop website`() {
        viewModel.onWebsiteClick()

        verify(navigator).showColgateWebsite()
    }

    @Test
    fun `onWebsiteClick sends website event`() {
        viewModel.onWebsiteClick()

        verify(eventTracker).sendEvent(AboutAnalytics.website())
    }

    @Test
    fun `onLicensesClick shows Licenses activity`() {
        viewModel.onLicensesClick()

        verify(navigator).showLicensesPage()
    }

    @Test
    fun `onLicensesClick sends licenses event`() {
        viewModel.onLicensesClick()

        verify(eventTracker).sendEvent(AboutAnalytics.licenses())
    }

    @Test
    fun `onLogoClick increases secretSettingsCount variable`() {
        assertEquals(0, viewModel.secretSettingsCount)

        viewModel.onLogoClick()
        assertEquals(1, viewModel.secretSettingsCount)

        viewModel.onLogoClick()
        assertEquals(2, viewModel.secretSettingsCount)
    }

    @Test
    fun `onLogoClick emits ShowSecretSettingsEnabled on 6th click`() {
        val actions = viewModel.actionsObservable.test()
        viewModel.onLogoClick() // 1st time
        actions.assertNoValues()
        viewModel.onLogoClick() // 2nd time
        actions.assertNoValues()
        viewModel.onLogoClick() // 3th time
        actions.assertNoValues()
        viewModel.onLogoClick() // 4th time
        actions.assertNoValues()
        viewModel.onLogoClick() // 5th time
        actions.assertNoValues()
        viewModel.onLogoClick() // 6th time
        actions.assertValue(AboutActions.ShowSecretSettingsEnabled)
    }

    @Test
    fun `onAccountIdClick copies id to the clipboard`() {
        val testId = "12345"
        whenever(accountFacade.getAccountSingle())
            .thenReturn(Single.just(mockAccount(testId)))

        val actionsObserver = viewModel.actionsObservable.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.onAccountIdClick()

        verify(copyToClipboardUseCase).copy(testId)
        actionsObserver.assertValue(AboutActions.AccountIdCopied)
    }

    private fun mockAccount(id: String): Account {
        return Account(id, 0, null, null, 0, null, emptyList())
    }
}

private const val APP_VERSION = "1.6.4-SUPER"
private const val BUILD_VERSION = "100"
