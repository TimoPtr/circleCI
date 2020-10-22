package com.kolibree.android.sdk.connection.user

import androidx.annotation.VisibleForTesting
import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.error.CommandNotSupportedException
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicLong

/** Base [User] implementation */
@Suppress("TooManyFunctions")
internal abstract class UserBaseImpl(private val toothbrushModel: ToothbrushModel) : UserInternal {

    @VisibleForTesting
    val profileIdCache = AtomicLong(NO_CACHE_PROFILE_ID)

    override fun profileId(): Single<Long> =
        if (isToothbrushRunningBootloader())
            Single.error(CommandNotSupportedException("Toothbrush is in bootloader"))
        else {
            when (val currentProfileId = profileIdCache.get()) {
                SHARED_MODE_PROFILE_ID -> Single.error(ToothbrushInSharedModeException())
                NO_CACHE_PROFILE_ID -> queryProfileIdAndUpdateCache().map { profileId ->
                    when (profileId) {
                        SHARED_MODE_PROFILE_ID -> throw ToothbrushInSharedModeException()
                        else -> profileId
                    }
                }
                else -> Single.just(currentProfileId) // Already in cache, no need to call ble
            }
        }

    final override fun profileOrSharedModeId(): Single<Long> =
        profileId()
            .onErrorResumeNext { throwable ->
                when (throwable) {
                    is ToothbrushInSharedModeException -> Single.just(SHARED_MODE_PROFILE_ID)
                    else -> Single.error(throwable)
                }
            }

    /*
    profileId() emits a long when the toothbrush has a real profile ID set, a
    ToothbrushInSharedModeException otherwise. Here we don't care about the profile ID but we know
    that the toothbrush is not in shared mode so we map to false. When it emits the exception then
    we know that the toothbrush is in shared mode.
     */
    override fun isSharedModeEnabled(): Single<Boolean> =
        profileId()
            .map { false }
            .onErrorReturn {
                if (it is ToothbrushInSharedModeException)
                    true
                else
                    throw it
            }

    override fun setProfileId(profileId: Long): Completable =
        when {
            isToothbrushRunningBootloader() -> Completable
                .error(CommandNotSupportedException("Toothbrush is in bootloader"))
            profileIdCache.get() == profileId -> Completable.complete()
            else -> sendProfileIdAndUpdateCache(profileId)
        }

    override fun enableSharedMode(): Completable =
        if (toothbrushModel.isMultiUser)
            setProfileId(SHARED_MODE_PROFILE_ID)
        else
            Completable.error(ToothbrushNotShareableException())

    override fun clearCache() = profileIdCache.set(NO_CACHE_PROFILE_ID)

    @SuppressWarnings("TooGenericExceptionCaught")
    @VisibleForTesting
    fun queryProfileIdAndUpdateCache(): Single<Long> =
        Single.create {
            try {
                var toothbrushProfileId: Long

                synchronized(profileIdCache) {
                    toothbrushProfileId = getToothbrushProfileId()
                    profileIdCache.set(toothbrushProfileId)
                }

                if (!it.isDisposed) {
                    it.onSuccess(toothbrushProfileId)
                }
            } catch (exception: Exception) {
                it.tryOnError(exception)
            }
        }

    @SuppressWarnings("TooGenericExceptionCaught")
    @VisibleForTesting
    fun sendProfileIdAndUpdateCache(profileId: Long): Completable =
        Completable.create {
            try {
                synchronized(profileIdCache) {
                    setToothbrushProfileId(profileId)
                    profileIdCache.set(profileId)
                }

                if (!it.isDisposed) {
                    it.onComplete()
                }
            } catch (exception: Exception) {
                it.tryOnError(exception)
            }
        }

    @Throws(Exception::class)
    abstract fun setToothbrushProfileId(profileId: Long)

    @Throws(Exception::class)
    abstract fun getToothbrushProfileId(): Long

    abstract fun isToothbrushRunningBootloader(): Boolean

    companion object {

        // Value of the cache when the toothbrush's profile ID has never been queried
        @VisibleForTesting
        const val NO_CACHE_PROFILE_ID = -1L
    }
}
