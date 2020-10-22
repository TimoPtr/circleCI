/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings.persistence.repo

import androidx.annotation.VisibleForTesting
import com.kolibree.android.coachplus.settings.persistence.dao.CoachSettingsDao
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettings
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettingsEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/*
Check DaggerTruncableSetTest on rename
 */
@VisibleForTesting
internal class CoachSettingsRepositoryImpl
@Inject constructor(private val coachSettingsDao: CoachSettingsDao) : CoachSettingsRepository {

    /**
     * Add or replace a coach settings
     */
    override fun save(coachSettings: CoachSettings) = Completable.fromCallable {
        coachSettingsDao.save(CoachSettingsEntity.from(coachSettings))
    }

    /**
     * Get the coach settings for a given user or create a new Settings
     */
    override fun getSettingsByProfileId(profileId: Long): Flowable<CoachSettings> =
        coachSettingsDao.find(profileId)
            .subscribeOn(Schedulers.io())
            .map {
                if (it.isEmpty()) {
                    CoachSettingsEntity(profileId = profileId)
                } else {
                    it.first()
                }
            }

    override fun truncate(): Completable = Completable.fromCallable {
        coachSettingsDao.deleteAll()
    }
}
