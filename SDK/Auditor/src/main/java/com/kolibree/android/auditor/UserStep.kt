/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.auditor

import androidx.annotation.Keep

/**
 * Interface to be implemented by Fragments that want to be considered a User Step
 *
 * By default, fragments aren't considered a User Step
 */
@Keep
interface UserStep

/**
 * Interface to be implemented by Fragments managed by a ViewPager that want to be considered a User Step
 *
 * By default, fragments aren't considered a User Step
 */
@Keep
interface ViewPagerUserStep

/**
 * Interface to be implemented by Activities that aren't a user step
 *
 * By default, all activities are considered a User Step
 */
@Keep
interface NoUserStep
