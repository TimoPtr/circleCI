/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.profile

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.sdkws.core.IKolibreeConnector
import javax.inject.Inject

@SuppressLint("DeobfuscatedPublicSdkClass")
interface NonActiveProfilesUseCase {
    fun profiles(): List<Profile>
}

internal class NonActiveProfilesUseCaseImpl @Inject constructor(
    private val connector: IKolibreeConnector,
    private val profileProvider: CurrentProfileProvider
) : NonActiveProfilesUseCase {

    override fun profiles(): List<Profile> {
        return createNonActiveProfiles(connector.profileList)
    }

    @VisibleForTesting
    fun createNonActiveProfiles(profiles: List<Profile>): List<Profile> {
        return profiles.filter {
            it.id != profileProvider.currentProfile().id
        }
    }
}
