/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.utils

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import io.reactivex.Flowable

/**
 * Provides [KLTBConnection]s that a given profile can use. This includes toothbrushes he owns as
 * well as shared toothbrushes, but it DOES NOT take into account their [KLTBConnectionState]
 */
@Keep
interface ToothbrushesForProfileUseCase {

    /**
     * @return a Flowable that will emit [List]<[KLTBConnection]> the [profileId] has rights
     * to use each time once of the following events takes place
     * - a toothbrush is paired, forgotten or reassigned
     * - [KolibreeService] is recreated
     *
     * The stream won't terminate unless there's an error or it's disposed
     */
    fun profileToothbrushesOnceAndStream(profileId: Long): Flowable<List<KLTBConnection>>

    /**
     * @return a Flowable that will emit [List]<[KLTBConnection]> the active profile has rights
     * to use each time once of the following events takes place
     * - a toothbrush is paired, forgotten or reassigned
     * - [KolibreeService] is recreated
     * - Active profile changes
     *
     * The stream won't terminate unless there's an error or it's disposed
     */
    fun currentProfileToothbrushesOnceAndStream(): Flowable<List<KLTBConnection>>

    /**
     * @return a Flowable that will emit [List]<[AccountToothbrush]> the active profile has rights
     * to use each time once of the following events takes place
     * - a toothbrush is paired, forgotten or reassigned
     * - Active profile changes
     *
     * The stream won't terminate unless there's an error or it's disposed
     */
    fun currentProfileAccountToothbrushesOnceAndStream(): Flowable<List<AccountToothbrush>>

    /**
     * @return a Flowable that will emit [List]<[AccountToothbrush]> the [profileId] has rights
     * to use each time once of the following events takes place
     * - a toothbrush is paired, forgotten or reassigned
     *
     * The stream won't terminate unless there's an error or it's disposed
     */
    fun profileAccountToothbrushesOnceAndStream(profileId: Long): Flowable<List<AccountToothbrush>>
}
