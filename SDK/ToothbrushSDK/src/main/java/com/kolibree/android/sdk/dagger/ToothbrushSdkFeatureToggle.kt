/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.dagger

import androidx.annotation.Keep
import javax.inject.Qualifier

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Keep
internal annotation class ToothbrushSdkFeatureToggle
