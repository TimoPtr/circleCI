/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.studies

import androidx.annotation.Keep

// https://kolibree.atlassian.net/browse/KLTB002-10080
@Keep
interface StudiesRepository {

    fun getStudy(mac: String): String

    fun addStudy(mac: String, studyName: String?)
}
