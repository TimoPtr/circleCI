/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature

import androidx.annotation.Keep
import kotlin.reflect.KClass

typealias FeatureToggleSet = Set<@JvmSuppressWildcards FeatureToggle<*>>

@Keep
@Suppress("UNCHECKED_CAST")
fun <T : Any> FeatureToggleSet.toggleForFeature(feature: Feature<T>): FeatureToggle<T> =
    first { it.feature == feature } as FeatureToggle<T>

@Keep
@Suppress("UNCHECKED_CAST")
fun FeatureToggleSet.toggleIsOn(feature: Feature<Boolean>): Boolean =
    (first { it.feature == feature } as FeatureToggle<Boolean>).value

@Keep
@Suppress("UNCHECKED_CAST")
fun <T : Any> FeatureToggleSet.toggleForFeatureByNameAndType(
    featureName: String,
    clazz: KClass<out T>
): FeatureToggle<T> =
    firstOrNull { it.feature.displayName == featureName } as FeatureToggle<T>
