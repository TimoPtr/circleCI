/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities

import com.kolibree.android.app.ui.home.tab.activities.card.games.GamesCardModule
import dagger.Module

@Module(includes = [GamesCardModule::class])
internal class ActivitiesTabModule
