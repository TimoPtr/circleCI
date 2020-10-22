/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings

import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepoModule
import com.kolibree.android.coachplus.settings.persistence.room.CoachSettingsRoomModule
import dagger.Module

@Module(includes = [CoachSettingsRoomModule::class, CoachSettingsRepoModule::class])
abstract class CoachCommonSettingsModule
