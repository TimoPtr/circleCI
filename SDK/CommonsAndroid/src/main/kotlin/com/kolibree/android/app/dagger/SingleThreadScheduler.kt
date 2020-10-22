/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.annotation.VisibleForApp
import javax.inject.Qualifier

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@VisibleForApp
annotation class SingleThreadScheduler
