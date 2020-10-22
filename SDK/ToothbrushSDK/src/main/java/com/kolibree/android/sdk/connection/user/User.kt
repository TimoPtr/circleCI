package com.kolibree.android.sdk.connection.user

import androidx.annotation.Keep
import com.kolibree.android.SHARED_MODE_PROFILE_ID
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by aurelien on 08/03/17.
 *
 * User mode settings interface
 */

@Keep
interface User {

    /**
     * Get toothbrush's current profile ID
     *
     * @return non null Single that will emit the profileId of the owner of the toothbrush, or
     * [ToothbrushInSharedModeException] if the toothbrush is in shared mode
     */
    fun profileId(): Single<Long>

    /**
     * Get toothbrush's current profile ID, including the Shared Mode reserved profile ID
     *
     * See KolibreeConst's SHARED_MODE_PROFILE_ID
     *
     * @return non null Single that will emit the profileId of the owner of the toothbrush, or
     * SHARED_MODE_PROFILE_ID if the toothbrush is in shared mode
     */
    fun profileOrSharedModeId(): Single<Long>

    /**
     * Get toothbrush's shared mode state
     *
     * @return a Single that will emit true if the toothbrush is in multi users mode, false otherwise
     */
    fun isSharedModeEnabled(): Single<Boolean>

    /**
     * Set the toothbrush profile ID
     *
     * This will disable the shared mode if it was enabled before
     *
     * @param profileId long profile ID
     * @return non null [Completable]
     */
    fun setProfileId(profileId: Long): Completable

    /**
     * Enable multi user mode, the toothbrush won't be associated to a profile ID anymore, until
     * another profile ID is set
     *
     * @return non null [Completable]
     */
    fun enableSharedMode(): Completable
}

@Keep
fun Long.isOwnedByOrShared(profileId: Long) = this == profileId || this == SHARED_MODE_PROFILE_ID
