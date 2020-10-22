/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.about

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.utils.KolibreeAppVersions
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class AboutViewState(
    val appVersion: String = "",
    val accountId: String = ""
) : BaseViewState {
    companion object {
        fun withAppVersions(appVersions: KolibreeAppVersions): AboutViewState =
            AboutViewState(appVersion = "${appVersions.appVersion} (${appVersions.buildVersion})")
    }
}
