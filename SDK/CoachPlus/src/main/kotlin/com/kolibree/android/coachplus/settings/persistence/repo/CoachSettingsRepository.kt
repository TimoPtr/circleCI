/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings.persistence.repo

import androidx.annotation.Keep
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettings
import com.kolibree.android.commons.interfaces.Truncable
import io.reactivex.Completable
import io.reactivex.Flowable

@Keep
interface CoachSettingsRepository : Truncable {

    fun getSettingsByProfileId(profileId: Long): Flowable<CoachSettings>

    fun save(coachSettings: CoachSettings): Completable
}
