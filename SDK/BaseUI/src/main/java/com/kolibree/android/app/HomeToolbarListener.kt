/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app

import com.kolibree.android.annotation.VisibleForApp
import org.threeten.bp.LocalDateTime

@VisibleForApp
interface HomeToolbarListener {
    @Deprecated(
        message = "This is a legacy method and will be removed when V1 UI is dropped",
        replaceWith = ReplaceWith("Nothing")
    )
    fun showProfileSwitcher() { /* no-op */ }
    @Deprecated(
        message = "This is a legacy method and will be removed when V1 UI is dropped",
        replaceWith = ReplaceWith("Nothing")
    )
    fun onDrawerButtonClicked() { /* no-op */ }
    @Deprecated(
        message = "This is a legacy method and will be removed when V1 UI is dropped",
        replaceWith = ReplaceWith("Nothing")
    )
    fun showProfile() { /* no-op */ }
    fun showOrphanBrushings()
    fun showCheckup(date: LocalDateTime)
}
