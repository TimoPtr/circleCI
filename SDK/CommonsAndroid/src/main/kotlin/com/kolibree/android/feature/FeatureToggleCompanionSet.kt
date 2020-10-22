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

typealias FeatureToggleCompanionSet = Set<@JvmSuppressWildcards FeatureToggle.Companion<*>>

@Keep
@Suppress("UNCHECKED_CAST")
fun <T : Any> FeatureToggleCompanionSet.companionForFeature(
    feature: Feature<T>
): FeatureToggle.Companion<T> =
    first { it.feature == feature } as FeatureToggle.Companion<T>

@Keep
@Suppress("UNCHECKED_CAST")
fun <T : Any> FeatureToggleCompanionSet.companionForFeatureByNameAndType(
    featureName: String,
    clazz: KClass<out T>
): FeatureToggle.Companion<T>? =
    firstOrNull { it.feature.displayName == featureName } as? FeatureToggle.Companion<T>
