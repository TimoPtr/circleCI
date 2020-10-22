/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.navigation.HomeScreenAction

@VisibleForApp
class CurrentProfileChanged(val profile: Profile) : HomeScreenAction

@VisibleForApp
object ShowProgressDialog : HomeScreenAction

@VisibleForApp
object HideProgressDialog : HomeScreenAction
