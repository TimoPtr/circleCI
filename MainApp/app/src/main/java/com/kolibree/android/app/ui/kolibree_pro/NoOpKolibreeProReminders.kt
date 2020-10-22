/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.kolibree_pro

import io.reactivex.Single

object NoOpKolibreeProReminders : KolibreeProReminders {

    override fun stopAllPeriodicReminders() {
        // no-op
    }

    override fun hasMultiplePractitionersNeedingConsent(): Boolean {
        // no-op
        return false
    }

    override fun practitionerToken(): String? {
        // no-op
        return null
    }

    override fun schedulePeriodicReminders(practitionerToken: String?, practitionerName: String?) {
        // no-op
    }

    override fun practitionerName(): String? {
        // no-op
        return null
    }

    override fun shouldShowReminder(): Single<Boolean> {
        // no-op
        return Single.just(false)
    }

    override fun stopPeriodicRemindersForActiveProfile() {
        // no-op
    }

    override fun stopPeriodicRemindersForProfile(profileId: Long) {
        // no-op
    }
}
