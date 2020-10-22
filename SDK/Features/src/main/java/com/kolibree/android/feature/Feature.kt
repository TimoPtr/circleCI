/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature

import androidx.annotation.CallSuper
import androidx.annotation.Keep
import kotlin.reflect.KClass

/**
 * Represents the feature of type [T] that we want to have changeable in the app.
 * To create new feature, you need to implement this interface.
 *
 * Currently supported feature types:
 * - [Boolean]
 * - [Long]
 * - [String]
 *
 * NOTE:
 * This interface should be implemented ONLY by non-anonymous classes.
 */
// TODO add support for single-choice enums https://kolibree.atlassian.net/browse/KLTB002-9495
// TODO add support for multi-choice enums https://kolibree.atlassian.net/browse/KLTB002-9496
@Keep
interface Feature<T : Any> {

    /**
     * Represents feature's initial value of type [T], before user attempts any change.
     */
    val initialValue: T

    /**
     * If true, configuration field will be displayed in secret settings.
     */
    val displayable: Boolean

    /**
     * Will be displayed in secret settings
     */
    val displayName: String

    /**
     * If set to true, user will be prompted that he needs to restart the app before configuration takes effect.
     */
    val requiresAppRestart: Boolean

    /**
     * Allows validation of new value before setting it.
     * By default, all values are accepted
     * @param newValue new value for validation
     * @return true if value is acceptable, false otherwise
     */
    fun validate(newValue: T): Boolean = true

    /**
     * Returns [KClass] type for this feature's [T] value
     */
    @CallSuper
    fun type(): KClass<out T> = initialValue::class
}
