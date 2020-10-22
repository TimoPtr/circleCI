/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import androidx.annotation.VisibleForTesting
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

@VisibleForTesting
val SPLASH_DURATION: Duration = Duration.of(2, ChronoUnit.SECONDS)

@VisibleForTesting
val MINIMAL_PROGRESS_DURATION: Duration = Duration.of(1, ChronoUnit.SECONDS)
