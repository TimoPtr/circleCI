/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.persistence

import com.kolibree.android.rewards.models.LifetimeSmiles

internal data class FakeLifetimeSmiles(
    override val profileId: Long,
    override val lifetimePoints: Int
) : LifetimeSmiles
