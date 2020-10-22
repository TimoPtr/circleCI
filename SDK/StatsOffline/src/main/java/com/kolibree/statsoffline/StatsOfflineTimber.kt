/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import kotlin.reflect.KClass

internal const val STATS_OFFLINE_TAG = "[STOFF]"

internal fun timberTagFor(clazz: KClass<*>): String = "$STATS_OFFLINE_TAG|${clazz.java.simpleName}"
