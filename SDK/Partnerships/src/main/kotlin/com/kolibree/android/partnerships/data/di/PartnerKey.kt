/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.di

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.partnerships.domain.model.Partner
import dagger.MapKey

@VisibleForApp
@MapKey
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PartnerKey(val value: Partner)
