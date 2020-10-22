/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.jakewharton.rx.ReplayingShare
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.accountinternal.profile.models.Profile
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import java.util.LinkedList
import java.util.NoSuchElementException
import javax.inject.Inject
import kotlin.reflect.KProperty
import timber.log.Timber

@Keep
interface CurrentProfileProvider {
    /**
     * Returns a Flowable that emits the current active profile, if any.
     *
     * If the active profile is updated, it'll emit it
     *
     * If a new observer subscribes, it'll receive the last com.kolibree.sdkws.profile.models.Profile emitted,
     * unless reset() was invoked
     *
     * The Flowable can complete, normally on logout
     */
    fun currentProfileFlowable(): Flowable<Profile>

    /**
     * @return a Single that will emit the active [Profile]. If there's none, it'll wait until it's
     * available.
     *
     * It can emit [NoSuchElementException] if [currentProfileFlowable] completes
     */
    @Throws(NoSuchElementException::class)
    fun currentProfileSingle(): Single<Profile>

    /**
     * @return a current [Profile] object, or throws the exception if there is none available.
     * @throws [NoSuchElementException] if there is no profile available.
     */
    @Throws(NoSuchElementException::class)
    fun currentProfile(): Profile

    /**
     * Terminates currentProfileFlowable stream
     *
     * Subscribers will need to resubscribe, and future subscribers won't receive the latest Profile emitted
     */
    fun reset()
}

internal class CurrentProfileProviderImpl
@Inject constructor(
    private val accountDatastore: AccountDatastore
) : CurrentProfileProvider {
    private val lazyManager = resettableManager()

    @VisibleForTesting
    val resetProcessor: PublishProcessor<Boolean> by resettableLazy(lazyManager) { PublishProcessor.create<Boolean>() }

    /*
    Listens to Account table changes and emits the active profile. If the Profile doesn't exist, it won't emit anything
    but it won't error.

    Future subscribers to the Flowable will receive the latest emitted Profile.

    When there are no more subscribers, the Flowable completes and we manually reset the instance so that a new Flowable
    is returned on the next invocation.

    We need this to be resettable because if we log out, we don't want whoever logs in in the future to receive an old
    Profile that no longer exists.
     */
    private val currentProfileFlowable: Flowable<Profile> by resettableLazy(lazyManager) {
        accountDatastore
            .accountFlowable()
            .takeUntil(resetProcessor)
            .switchMap { accountInternal ->
                val currentProfileId = accountInternal.currentProfileId

                if (currentProfileId != null) {
                    val currentProfile = accountInternal.getProfileInternalWithId(currentProfileId)
                    if (currentProfile != null) {
                        try {
                            return@switchMap Flowable.just(currentProfile.exportProfile())
                        } catch (e: Exception) {
                            Timber.w(e, "Exception trying to export profile $currentProfile")
                        }
                    } else {
                        Timber.w(RuntimeException(), "currentProfile is null, returning empty flowable!")
                    }
                } else {
                    Timber.w(RuntimeException(), "currentProfileId is null, returning empty flowable!")
                }

                Flowable.empty<Profile>()
            }
            .distinctUntilChanged()
            .doFinally { reset() }
            .compose(ReplayingShare.instance())
    }

    override fun currentProfileFlowable() = Flowable.defer { currentProfileFlowable }

    /**
     * @return a Single that will emit the active [Profile]. If there's none, it'll wait until it's
     * available.
     *
     * It can emit [NoSuchElementException] if [currentProfileFlowable] completes
     */
    override fun currentProfileSingle(): Single<Profile> {
        return currentProfileFlowable()
            .take(1)
            .singleOrError()
    }

    /**
     * @return a current [Profile] object, or throws the exception if there is none available.
     * @throws [NoSuchElementException] if there is no profile available.
     */
    @Throws(NoSuchElementException::class)
    override fun currentProfile(): Profile = currentProfileFlowable().take(1).blockingSingle()

    override fun reset() {
        resetProcessor.onComplete()

        lazyManager.reset()
    }
}

/*
See https://stackoverflow.com/a/35757638/218473
 */
private class ResettableLazyManager {
    // we synchronize to make sure the timing of a reset() call and new inits do not collide
    val managedDelegates = LinkedList<Resettable>()

    fun register(managed: Resettable) {
        synchronized(managedDelegates) {
            managedDelegates.add(managed)
        }
    }

    fun reset() {
        synchronized(managedDelegates) {
            managedDelegates.forEach { it.reset() }
            managedDelegates.clear()
        }
    }
}

private interface Resettable {
    fun reset()
}

private class ResettableLazy<PROPTYPE>(
    val manager: ResettableLazyManager,
    val init: () -> PROPTYPE
) : Resettable {
    @Volatile
    var lazyHolder = makeInitBlock()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): PROPTYPE {
        return lazyHolder.value
    }

    override fun reset() {
        lazyHolder = makeInitBlock()
    }

    fun makeInitBlock(): Lazy<PROPTYPE> {
        return lazy {
            manager.register(this)
            init()
        }
    }
}

private fun <PROPTYPE> resettableLazy(
    manager: ResettableLazyManager,
    init: () -> PROPTYPE
): ResettableLazy<PROPTYPE> {
    return ResettableLazy(manager, init)
}

private fun resettableManager(): ResettableLazyManager = ResettableLazyManager()
