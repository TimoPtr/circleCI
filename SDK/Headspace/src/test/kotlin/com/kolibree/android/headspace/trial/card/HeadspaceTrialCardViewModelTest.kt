/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.headspace.R
import com.kolibree.android.partnerships.data.DiscountCodeNotAvailableException
import com.kolibree.android.partnerships.domain.DisablePartnershipUseCase
import com.kolibree.android.partnerships.domain.PartnershipStatusUseCase
import com.kolibree.android.partnerships.domain.UnlockPartnershipUseCase
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.partnerships.domain.model.PartnershipStatus
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.assertHasObserversAndComplete
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.utils.CopyToClipboardUseCase
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HeadspaceTrialCardViewModelTest : BaseUnitTest() {
    private val statusUseCase: PartnershipStatusUseCase = mock()
    private val unlockUseCase: UnlockPartnershipUseCase = mock()
    private val disableUseCase: DisablePartnershipUseCase = mock()
    private val navigator: HeadspaceTrialNavigator = mock()
    private val clipboardUseCase: CopyToClipboardUseCase = mock()

    lateinit var viewModel: HeadspaceTrialCardViewModel

    private val statusSubject by lazy { PublishProcessor.create<PartnershipStatus>() }
    private val refreshSubject by lazy { CompletableSubject.create() }

    /*
    onCreate
     */
    @Test
    fun `onCreate subscribes to getPartnershipStatusStream`() {
        initWithViewState()

        pushToOnCreate()

        assertTrue(statusSubject.hasSubscribers())
    }

    @Test
    fun `when statusStream emits Inactive expected ViewState is emitted`() {
        initWithViewState()

        pushToOnCreate()

        val viewStateObserver = viewModel.viewStateFlowable.test()

        val status = HeadspacePartnershipStatus.Inactive(profileId = 1L)
        statusSubject.onNext(status)

        val expectedViewState = initialViewState().withInactiveStatus(status)

        viewStateObserver.assertLastValue(expectedViewState)
    }

    @Test
    fun `when statusStream emits Unlocked expected ViewState is emitted`() {
        initWithViewState()

        pushToOnCreate()

        val viewStateObserver = viewModel.viewStateFlowable.test()

        val status = HeadspacePartnershipStatus.Unlocked(
            profileId = 1L,
            discountCode = "discountCode",
            redeemUrl = "redeemUrl"
        )
        statusSubject.onNext(status)

        val expectedViewState = initialViewState().withUnlockedStatus(status)

        viewStateObserver.assertLastValue(expectedViewState)
    }

    @Test
    fun `when statusStream emits InProgress expected ViewState is emitted`() {
        initWithViewState()

        pushToOnCreate()

        val viewStateObserver = viewModel.viewStateFlowable.test()

        val status = HeadspacePartnershipStatus.InProgress(
            profileId = 1L,
            pointsNeeded = 33,
            pointsThreshold = 200
        )
        statusSubject.onNext(status)

        val expectedViewState = initialViewState().withInProgressStatus(status)

        viewStateObserver.assertLastValue(expectedViewState)
    }

    /*
    onResume
     */

    @Test
    fun `onResume refreshes partnership status`() {
        initWithViewState()

        pushToOnResume()

        refreshSubject.assertHasObserversAndComplete()
    }

    /*
    onToggleDescriptionClick
     */

    @Test
    fun `onToggleDescriptionClick emits a ViewState with descriptionVisible=true`() {
        initWithViewState()

        val viewStateObserver = viewModel.viewStateFlowable.test()

        /*
        for now, we are force emitting a value in init block, so we need to fetch the latest
        emitted value
         */
        val emittedViewState = viewStateObserver.values().single()

        viewModel.onToggleDescriptionClick()

        viewStateObserver
            .assertValueCount(2)
            .assertLastValue(emittedViewState.copy(isDescriptionVisible = true))
    }

    @Test
    fun `onToggleDescriptionClick sends analytics event showMore when description is hidden`() {
        initWithViewState()

        viewModel.onToggleDescriptionClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Headspace_ShowMore"))
    }

    @Test
    fun `onToggleDescriptionClick sends analytics event showLess when description is visible`() {
        initWithViewState(viewState = initialViewState().copy(isDescriptionVisible = true))

        viewModel.onToggleDescriptionClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Headspace_ShowLess"))
    }

    /*
    onCallToActionClicked
     */
    @Test
    fun `when viewState is not unlocked, onCallToActionClicked sends Unlock analytics event independently of unlock result`() {
        initWithViewState(unlockableViewState())

        pushToOnResume()

        val unlockSubject = CompletableSubject.create()
        whenever(unlockUseCase.unlockCompletable(Partner.HEADSPACE))
            .thenReturn(unlockSubject)

        viewModel.onCallToActionClicked()

        verify(eventTracker).sendEvent(AnalyticsEvent("Headspace_UnlockCode"))
    }

    @Test
    fun `when viewState is not unlocked, onCallToActionClicked subscribes to unlockCompletable`() {
        initWithViewState(unlockableViewState())

        pushToOnResume()

        val unlockSubject = CompletableSubject.create()
        whenever(unlockUseCase.unlockCompletable(Partner.HEADSPACE))
            .thenReturn(unlockSubject)

        viewModel.onCallToActionClicked()

        assertTrue(unlockSubject.hasObservers())
    }

    @Test
    fun `when viewState is not unlocked and unlock completes successfully, nothing happens in onCallToActionClicked`() {
        initWithViewState(unlockableViewState())

        pushToOnResume()

        whenever(unlockUseCase.unlockCompletable(Partner.HEADSPACE))
            .thenReturn(Completable.complete())

        viewModel.onCallToActionClicked()

        verify(navigator, never()).showSomethingWentWrong()
    }

    @Test
    fun `when viewState is not unlocked and unlock emits error, onCallToActionClicked invokes something went wrong`() {
        initWithViewState(unlockableViewState())

        pushToOnResume()

        whenever(unlockUseCase.unlockCompletable(Partner.HEADSPACE))
            .thenReturn(Completable.error(TestForcedException()))

        viewModel.onCallToActionClicked()

        verify(navigator).showSomethingWentWrong()
    }

    @Test
    fun `when viewState is not unlocked and unlock emits DiscountCodeNotAvailableException, onCallToActionClicked invokes showSnackbarError`() {
        initWithViewState(unlockableViewState())

        pushToOnResume()

        whenever(unlockUseCase.unlockCompletable(Partner.HEADSPACE))
            .thenReturn(Completable.error(DiscountCodeNotAvailableException(TestForcedException())))

        viewModel.onCallToActionClicked()

        verify(navigator).showSnackbarError(R.string.headspace_card_unlock_error_no_codes)
    }

    @Test
    fun `when viewState is unlocked and redeemUrl is not null, onCallToActionClicked invokes openUrl`() {
        val redeemUrl = "redeeeem"
        initWithViewState(unlockableViewState().copy(isUnlocked = true, redeemUrl = redeemUrl))

        viewModel.onCallToActionClicked()

        verify(navigator).openUrl(redeemUrl)
    }

    @Test
    fun `when viewState is unlocked and redeemUrl is not null, onCallToActionClicked sends visit headspace analytics event`() {
        val redeemUrl = "redeeeem"
        initWithViewState(unlockableViewState().copy(isUnlocked = true, redeemUrl = redeemUrl))

        viewModel.onCallToActionClicked()

        verify(eventTracker).sendEvent(AnalyticsEvent("Headspace_VisitHeadSpace"))
    }

    @Test
    fun `when viewState is unlocked and redeemUrl is null, onCallToActionClicked never invokes openUrl`() {
        initWithViewState(unlockableViewState().copy(isUnlocked = true, redeemUrl = null))

        viewModel.onCallToActionClicked()

        verify(navigator, never()).openUrl(any())
    }

    /*
    onTapToCopyClicked
     */

    @Test
    fun `onTapToCopyClicked invokes copyToClipboardUseCase`() {
        val expectedText = "dasd"
        initWithViewState(initialViewState().copy(discountCode = expectedText))

        viewModel.onTapToCopyClicked()

        verify(clipboardUseCase).copy(text = expectedText, label = expectedText)
    }

    @Test
    fun `onTapToCopyClicked emits viewState with copiedToClipboard = true when copy succeeds`() {
        val expectedText = "dasd"
        val viewState = initialViewState().copy(discountCode = expectedText)
        initWithViewState(viewState)

        val viewStateObserver = viewModel.viewStateFlowable.test()

        viewModel.onTapToCopyClicked()

        verify(clipboardUseCase).copy(text = expectedText, label = expectedText)

        viewStateObserver.assertLastValue(viewState.copy(copiedToClipboard = true))
    }

    @Test
    fun `onTapToCopyClicked doesn't emit a viewState when copy fails`() {
        val viewState = initialViewState().copy(discountCode = "da")
        initWithViewState(viewState)

        val viewStateObserver = viewModel.viewStateFlowable.test()
            .assertValueCount(1)

        whenever(clipboardUseCase.copy(any(), any()))
            .thenAnswer { throw TestForcedException() }

        viewModel.onTapToCopyClicked()

        verify(clipboardUseCase).copy(any(), any())

        viewStateObserver.assertValueCount(1)
    }

    @Test
    fun `onTapToCopyClicked invokes showSomethingWentWrong when copy fails`() {
        val viewState = initialViewState().copy(discountCode = "das")
        initWithViewState(viewState)

        whenever(clipboardUseCase.copy(any(), any()))
            .thenAnswer { throw TestForcedException() }

        viewModel.onTapToCopyClicked()

        verify(navigator).showSomethingWentWrong()
    }

    @Test
    fun `onTapToCopyClicked sends copy analytics event`() {
        val expectedText = "dasd"
        initWithViewState(initialViewState().copy(discountCode = expectedText))

        viewModel.onTapToCopyClicked()

        verify(eventTracker).sendEvent(AnalyticsEvent("Headspace_CopyCode"))
    }

    /*
    onCloseClicked
     */
    @Test
    fun `onCloseClicked subscribes to disableCompletable after user confirms disable`() {
        val disableSubject = CompletableSubject.create()
        whenever(disableUseCase.disableCompletable(Partner.HEADSPACE))
            .thenReturn(disableSubject)

        initWithViewState()

        pushToOnResume()

        mockConfirmDismiss()

        viewModel.onCloseClicked()

        disableSubject.assertHasObserversAndComplete()
    }

    @Test
    fun `onCloseClicked sends analytics event after user confirms disable`() {
        val disableSubject = CompletableSubject.create()
        whenever(disableUseCase.disableCompletable(Partner.HEADSPACE))
            .thenReturn(disableSubject)

        initWithViewState()

        pushToOnResume()

        mockConfirmDismiss()

        viewModel.onCloseClicked()

        verify(eventTracker).sendEvent(AnalyticsEvent("Headspace_RemoveCode"))
    }

    @Test
    fun `onCloseClicked invokes something went wrong if disableCompletable emits error`() {
        whenever(disableUseCase.disableCompletable(Partner.HEADSPACE))
            .thenReturn(Completable.error(TestForcedException()))

        initWithViewState()

        pushToOnResume()

        mockConfirmDismiss()

        viewModel.onCloseClicked()

        verify(navigator).showSomethingWentWrong()
    }

    @Test
    fun `onCloseClicked does nothing if user doesn't confirm close`() {
        val disableSubject = CompletableSubject.create()
        whenever(disableUseCase.disableCompletable(Partner.HEADSPACE))
            .thenReturn(disableSubject)

        initWithViewState()

        pushToOnResume()

        mockCancelQuit()

        viewModel.onCloseClicked()

        verify(navigator, never()).showSomethingWentWrong()

        assertFalse(disableSubject.hasObservers())
    }

    @Test
    fun `onCloseClicked sends analytics event after user doesn't confirm close`() {
        val disableSubject = CompletableSubject.create()
        whenever(disableUseCase.disableCompletable(Partner.HEADSPACE))
            .thenReturn(disableSubject)

        initWithViewState()

        pushToOnResume()

        mockCancelQuit()

        viewModel.onCloseClicked()

        verify(eventTracker).sendEvent(AnalyticsEvent("Headspace_KeepCode"))
    }

    @Test
    fun `onCloseClicked sends Quit analytics event before user confirms any action`() {
        val disableSubject = CompletableSubject.create()
        whenever(disableUseCase.disableCompletable(Partner.HEADSPACE))
            .thenReturn(disableSubject)

        initWithViewState()

        pushToOnResume()

        mockConfirmDismiss()

        viewModel.onCloseClicked()

        verify(eventTracker).sendEvent(AnalyticsEvent("Headspace_Quit"))
    }

    /*
    Utils
     */
    private fun initWithViewState(viewState: HeadspaceTrialCardViewState = initialViewState()) {
        viewModel = HeadspaceTrialCardViewModel(
            initialViewState = viewState,
            statusUseCase = statusUseCase,
            unlockUseCase = unlockUseCase,
            navigator = navigator,
            clipboardUseCase = clipboardUseCase,
            disableUseCase = disableUseCase
        )
    }

    private fun initialViewState(): HeadspaceTrialCardViewState {
        return HeadspaceTrialCardViewState.initial(
            position = DynamicCardPosition.EIGHT
        )
    }

    private fun unlockableViewState() =
        initialViewState().copy(pointsNeeded = 0, isUnlocked = false).also {
            assertTrue(it.isUnlockable)
        }

    fun pushToOnCreate() {
        mockStatusStream()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
    }

    private fun pushToOnResume() {
        mockStatusStream()

        whenever(statusUseCase.refreshPartnershipData())
            .thenReturn(refreshSubject)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
    }

    private fun mockStatusStream() {
        whenever(statusUseCase.getPartnershipStatusStream(Partner.HEADSPACE))
            .thenReturn(statusSubject)
    }

    private fun mockConfirmDismiss() {
        whenever(navigator.confirmDismissCard(any(), any()))
            .thenAnswer { it.getArgument<() -> Unit>(0).invoke() }
    }

    private fun mockCancelQuit() {
        whenever(navigator.confirmDismissCard(any(), any()))
            .thenAnswer { it.getArgument<() -> Unit>(1).invoke() }
    }
}
