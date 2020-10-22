/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.account.ProfileFacade
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.navigation.NavigationHelper
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tab.profile.completeprofile.CompleteProfileBubbleViewModel
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.app.ui.home.tracker.BottomNavigationEventTracker
import com.kolibree.android.app.ui.host.DynamicCardHostViewModel
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.selectavatar.StoreAvatarProducer
import com.kolibree.android.app.ui.selectavatar.StoreAvatarResult
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.sdkws.core.AvatarCache
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

internal class ProfileViewModel(
    initialViewState: ProfileViewState,
    val cardHostViewModel: DynamicCardHostViewModel,
    val toolbarViewModel: HomeToolbarViewModel,
    val completeProfileBubbleViewModel: CompleteProfileBubbleViewModel,
    private val homeNavigator: HumHomeNavigator,
    private val profileNavigator: ProfileNavigator,
    private val currentProfileProvider: CurrentProfileProvider,
    private val storeAvatarProducer: StoreAvatarProducer,
    private val debounceScheduler: Scheduler,
    private val avatarCache: AvatarCache,
    private val profileFacade: ProfileFacade,
    private val navigationHelper: NavigationHelper
) : BaseViewModel<ProfileViewState, HomeScreenAction>(
    initialViewState,
    children = setOf(toolbarViewModel, completeProfileBubbleViewModel)
) {

    fun onEditAvatarClicked() {
        ProfileEventTracker.changePicture()
        profileNavigator.showChooseAvatar()
    }

    fun onEditProfileClicked() {
        ProfileEventTracker.goToSetting()
        homeNavigator.showSettingsScreen()
    }

    val avatarUrl = mapNonNull<ProfileViewState, String?>(
        viewStateLiveData,
        initialViewState.avatarUrl
    ) { viewState -> viewState.avatarUrl }

    val firstName = mapNonNull<ProfileViewState, String?>(
        viewStateLiveData,
        initialViewState.firstName
    ) { viewState -> viewState.firstName }

    init {
        disposeOnCleared(::subscribeToAvatarProducer)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        disposeOnDestroy(::subscribeToProfileStream)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        BottomNavigationEventTracker.profileVisible()
    }

    private fun subscribeToAvatarProducer(): Disposable =
        storeAvatarProducer.avatarResultStream()
            .subscribeOn(Schedulers.io())
            .onStoreAvatarResult()
            .subscribe({ /*no-op*/ }, Timber::e)

    private fun subscribeToProfileStream() =
        currentProfileProvider.currentProfileFlowable()
            .filter { profile -> profile.avatarOrNameChanged() }
            .throttleFirst(throttlingDuration.toMillis(), TimeUnit.MILLISECONDS, debounceScheduler)
            .subscribe(
                ::onProfileLoaded,
                ::onProfileError
            )

    private fun Flowable<StoreAvatarResult>.onStoreAvatarResult(): Completable =
        flatMapCompletable { result ->
            when (result) {
                is StoreAvatarResult.Success -> {
                    updateAvatarCurrentProfile(result.avatarPath)
                }
                is StoreAvatarResult.Error -> {
                    onStoreAvatarResultError(result)
                }
            }
        }

    private fun updateAvatarCurrentProfile(avatarPath: String): Completable =
        currentProfileProvider.currentProfileSingle().flatMapCompletable {
            profileFacade.changeProfilePicture(it, avatarPath).ignoreElement()
        }

    private fun onStoreAvatarResultError(result: StoreAvatarResult.Error): Completable {
        Timber.w(result.exception)
        result.exception.message?.let(navigationHelper::showSnackbarError)
        return Completable.complete()
    }

    @VisibleForTesting
    fun onProfileLoaded(profile: Profile) {
        updateViewState {
            copy(
                avatarUrl = avatarCache.getAvatarUrl(profile),
                firstName = profile.firstName
            )
        }
    }

    private fun onProfileError(throwable: Throwable) {
        Timber.e(throwable)
    }

    private fun Profile.avatarOrNameChanged(): Boolean {
        val viewState = getViewState() ?: return true

        return pictureUrl != viewState.avatarUrl || firstName != viewState.firstName
    }

    class Factory @Inject constructor(
        private val cardHostViewModel: DynamicCardHostViewModel,
        private val toolbarViewModel: HomeToolbarViewModel,
        private val completeProfileBubbleViewModel: CompleteProfileBubbleViewModel,
        private val homeNavigator: HumHomeNavigator,
        private val profileNavigator: ProfileNavigator,
        private val currentProfileProvider: CurrentProfileProvider,
        private val storeAvatarProducer: StoreAvatarProducer,
        @SingleThreadScheduler private val debounceScheduler: Scheduler,
        private val avatarCache: AvatarCache,
        private val profileFacade: ProfileFacade,
        private val navigationHelper: NavigationHelper
    ) : BaseViewModel.Factory<ProfileViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(
            viewState ?: ProfileViewState.initial(),
            cardHostViewModel,
            toolbarViewModel,
            completeProfileBubbleViewModel,
            homeNavigator,
            profileNavigator,
            currentProfileProvider,
            storeAvatarProducer,
            debounceScheduler,
            avatarCache,
            profileFacade,
            navigationHelper
        ) as T
    }
}

/**
 * Minimum amount of milliseconds that must elapse before we process a new Profile from database
 *
 * This was introduced to avoid the following scenario when the user changes the avatar
 * t0 = user updates the avatar
 * t1 = local path to avatar is inserted to DB -> currentProfileProvider emits changes
 * t2 = avatar change is pushed to backend
 * t3 = insert to DB backend url at which avatar is hosted -> currentProfileProvider emits changes
 *
 * What happens without throttling is that the avatar View reloads both at t1 and t3
 * They both refer the same Drawable, but one is local and the other is remote
 *
 * By throttling, if (t3 - t1) < AVATAR_CHANGED_THROTTLE_MILLIS we will drop the value emitted at t3
 * and avoid reloading
 */
private const val AVATAR_CHANGED_THROTTLE_MILLIS = 3000L

@VisibleForTesting
internal val throttlingDuration = Duration.of(AVATAR_CHANGED_THROTTLE_MILLIS, ChronoUnit.MILLIS)
