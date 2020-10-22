/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.account.AccountFacade
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.settings.notifications.NotificationsActions.ShowTimePicker
import com.kolibree.android.brushreminder.BrushReminderUseCase
import com.kolibree.android.brushreminder.formatter.BrushReminderTimeFormatter
import com.kolibree.android.brushreminder.model.BrushingReminder
import com.kolibree.android.brushreminder.model.BrushingReminderType
import com.kolibree.android.brushreminder.model.BrushingReminders
import com.kolibree.android.brushsyncreminder.BrushSyncReminderUseCase
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import org.threeten.bp.LocalTime
import timber.log.Timber

internal class NotificationsViewModel(
    initialViewState: NotificationsViewState,
    private val accountFacade: AccountFacade,
    private val brushSyncReminderUseCase: BrushSyncReminderUseCase,
    private val systemNotificationsEnabledUseCase: SystemNotificationsEnabledUseCase,
    private val notificationsNavigator: NotificationsNavigator,
    private val brushReminderUseCase: BrushReminderUseCase,
    private val timeFormatter: BrushReminderTimeFormatter
) : BaseViewModel<NotificationsViewState, NotificationsActions>(
    initialViewState
) {

    val isBrushingReminderOn: LiveData<Boolean> = mapNonNull(
        viewStateLiveData, initialViewState.isBrushingSyncReminderOn
    ) { viewState ->
        viewState.isBrushingSyncReminderOn
    }

    val isNewsletterSubscriptionOn: LiveData<Boolean> = mapNonNull(
        viewStateLiveData, initialViewState.isNewsletterSubscriptionOn
    ) { viewState ->
        viewState.isNewsletterSubscriptionOn
    }

    val isChangingNewsletterSubscription: LiveData<Boolean> = mapNonNull(
        viewStateLiveData, initialViewState.isChangingNewsletterSubscription
    ) { viewState ->
        viewState.isChangingNewsletterSubscription
    }

    val isChangingReminder: LiveData<Boolean> = mapNonNull(
        viewStateLiveData, initialViewState.isChangingReminder
    ) { viewState ->
        viewState.isChangingReminder
    }

    val morningReminder: LiveData<BrushingReminder> = mapNonNull(
        viewStateLiveData, initialViewState.morningReminder
    ) { viewState ->
        viewState.morningReminder
    }

    val afternoonReminder: LiveData<BrushingReminder> = mapNonNull(
        viewStateLiveData, initialViewState.afternoonReminder
    ) { viewState ->
        viewState.afternoonReminder
    }

    val eveningReminder: LiveData<BrushingReminder> = mapNonNull(
        viewStateLiveData, initialViewState.eveningReminder
    ) { viewState ->
        viewState.eveningReminder
    }

    val morningReminderTime: LiveData<String> = mapNonNull(
        viewStateLiveData, ""
    ) { viewState ->
        timeFormatter.format(viewState.morningReminder.time)
    }

    val afternoonReminderTime: LiveData<String> = mapNonNull(
        viewStateLiveData, ""
    ) { viewState ->
        timeFormatter.format(viewState.afternoonReminder.time)
    }

    val eveningReminderTime: LiveData<String> = mapNonNull(
        viewStateLiveData, ""
    ) { viewState ->
        timeFormatter.format(viewState.eveningReminder.time)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        disposeOnStop(::fetchNewsletterSubscription)
        disposeOnStop(::fetchBrushSyncReminder)
        disposeOnStop(::fetchBrushReminder)
    }

    @VisibleForTesting
    fun fetchBrushSyncReminder(): Disposable = brushSyncReminderUseCase
        .isCurrentProfileReminderOn()
        .map(::isBrushSyncReminderNotificationEnabled)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(::refreshBrushSyncReminder, Timber::e)

    private fun refreshBrushSyncReminder(isReminderOn: Boolean) {
        updateViewState { copy(isBrushingSyncReminderOn = isReminderOn) }
    }

    @VisibleForTesting
    fun fetchBrushReminder(): Disposable = brushReminderUseCase
        .fetchBrushingReminders()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(::refreshSyncReminder, Timber::e)

    private fun refreshSyncReminder(brushingReminders: BrushingReminders) {
        updateViewState { withReminders(brushingReminders) }
    }

    private fun isBrushSyncReminderNotificationEnabled(isBrushReminderOn: Boolean): Boolean =
        isBrushReminderOn && systemNotificationsEnabledUseCase.areNotificationsEnabled()

    private fun showNotificationsDisabledDialog() {
        notificationsNavigator.showNotificationsDisabledDialog()
    }

    private fun fetchNewsletterSubscription(): Disposable = accountFacade
        .isEmailNewsletterSubscriptionEnabled()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(::refreshNewsletterSubscription, Timber::e)

    private fun refreshNewsletterSubscription(isSubscriptionOn: Boolean) {
        updateViewState { copy(isNewsletterSubscriptionOn = isSubscriptionOn) }
    }

    fun onBrushingReminderClick(isOn: Boolean) {
        if (getViewState()?.isBrushingSyncReminderOn != isOn) {
            onBrushingReminderChanged(isOn)
        }
    }

    fun onBrushingReminderClick(type: BrushingReminderType, isOn: Boolean) {
        val brushingReminder = getViewState()?.brushingReminder(type)
        if (isOn != brushingReminder?.isOn) {
            updateBrushingReminder(isOn, type)
        }
    }

    fun onBrushingReminderTimeClick(type: BrushingReminderType) {
        getViewState()?.brushingReminder(type)?.let { brushingReminder ->
            pushAction(ShowTimePicker(type, brushingReminder.time))
        }
    }

    private fun updateBrushingReminder(isOn: Boolean, type: BrushingReminderType) {
        updateViewState { withReminderOn(isOn, type) }

        saveCurrentBrushingReminders()
    }

    fun userSelectedReminderTime(newTime: LocalTime, type: BrushingReminderType) {
        updateViewState { withReminderTime(newTime, type) }

        saveCurrentBrushingReminders()
    }

    private fun saveCurrentBrushingReminders() {
        getViewState()?.let { viewState ->
            disposeOnCleared {
                brushReminderUseCase.updateBrushingReminders(viewState.brushingReminders)
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, Timber::e)
            }
        }
    }

    private fun areSystemNotificationDisabled() =
        !systemNotificationsEnabledUseCase.areNotificationsEnabled()

    private fun onBrushingReminderChanged(isReminderOn: Boolean) {
        updateViewState { copy(isBrushingSyncReminderOn = isReminderOn) }

        if (areSystemNotificationDisabled()) {
            updateViewState { copy(isBrushingSyncReminderOn = false) }
            showNotificationsDisabledDialog()
        } else {
            NotificationsAnalytics.syncReminder(isReminderOn)
            disposeOnCleared { updateBrushingReminder(isReminderOn) }
        }
    }

    private fun updateBrushingReminder(isReminderOn: Boolean): Disposable =
        brushSyncReminderUseCase.setCurrentProfileReminder(isReminderOn)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { updateViewState { copy(isChangingReminder = true) } }
            .doFinally { updateViewState { copy(isChangingReminder = false) } }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {
                Timber.e(it)
                updateViewState { copy(isBrushingSyncReminderOn = !isReminderOn) }
            })

    fun onSubscribeNewsletterClick(isOn: Boolean) {
        if (getViewState()?.isNewsletterSubscriptionOn != isOn) {
            onSubscribeNewsletterChanged(isOn)
        }
    }

    private fun onSubscribeNewsletterChanged(isSubscriptionOn: Boolean) {
        NotificationsAnalytics.joinMailing(isSubscriptionOn)
        updateViewState { copy(isNewsletterSubscriptionOn = isSubscriptionOn) }
        disposeOnCleared { subscribeNewsletter(isSubscriptionOn) }
    }

    private fun subscribeNewsletter(isSubscriptionOn: Boolean): Disposable {
        val accountId = accountFacade.accountId
        return accountFacade.emailNewsletterSubscription(accountId, isSubscriptionOn)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { updateViewState { copy(isChangingNewsletterSubscription = true) } }
            .doFinally { updateViewState { copy(isChangingNewsletterSubscription = false) } }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {
                Timber.e(it)
                updateViewState { copy(isNewsletterSubscriptionOn = !isSubscriptionOn) }
            })
    }

    fun onGoToSettingsClick() {
        notificationsNavigator.openAppNotificationSettings()
    }

    fun onCloseScreen() {
        NotificationsAnalytics.goBack()
        notificationsNavigator.closeScreen()
    }

    class Factory @Inject constructor(
        private val accountFacade: AccountFacade,
        private val brushSyncReminderUseCase: BrushSyncReminderUseCase,
        private val systemNotificationsEnabledUseCase: SystemNotificationsEnabledUseCase,
        private val notificationsNavigator: NotificationsNavigator,
        private val brushReminderUseCase: BrushReminderUseCase,
        private val timeFormatter: BrushReminderTimeFormatter
    ) : BaseViewModel.Factory<NotificationsViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotificationsViewModel(
                initialViewState = viewState ?: NotificationsViewState.initial(),
                accountFacade = accountFacade,
                brushSyncReminderUseCase = brushSyncReminderUseCase,
                systemNotificationsEnabledUseCase = systemNotificationsEnabledUseCase,
                notificationsNavigator = notificationsNavigator,
                brushReminderUseCase = brushReminderUseCase,
                timeFormatter = timeFormatter
            ) as T
    }
}
