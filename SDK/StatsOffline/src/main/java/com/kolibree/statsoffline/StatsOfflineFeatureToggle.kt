/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import androidx.annotation.Keep
import com.kolibree.android.feature.FeatureToggle

/**
 * This interface is needed so that SDK users can inject it and enable the feature manually
 */
@Keep
class StatsOfflineFeatureToggle(internal val featureToggle: FeatureToggle<Boolean>) :
    FeatureToggle<Boolean> by featureToggle
