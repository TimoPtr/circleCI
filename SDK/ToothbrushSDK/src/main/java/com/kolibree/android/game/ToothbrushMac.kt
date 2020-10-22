/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game

import androidx.annotation.Keep
import javax.inject.Qualifier

/**
 * Dagger qualifier, used to pre-populate toothbrush MAC address in [GameInteractor] when the game
 * starts. Can improve the start time of the game, especially when more than 1 brush are connected.
 *
 * Example:
 *
 *  @Provides
 *  @ActivityScope
 *  @ToothbrushMac
 *  fun providesToothbrushMac(): Optional<String> {
 *      return Optional.of("00:00:00:00:00:00")
 *  }
 */
@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Keep
annotation class ToothbrushMac
