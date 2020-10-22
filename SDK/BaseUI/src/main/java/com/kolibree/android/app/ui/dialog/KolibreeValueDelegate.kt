/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import androidx.annotation.Keep

typealias ValueListener<T, V> = (T.(V) -> Unit)

@Keep
interface KolibreeValueDelegate<T, V> {
    val currentValue: V

    /**
     * Provides a lambda which will be triggered whenever the value changes
     */
    val valueListener: ValueListener<T, V>

    /**
     * Sets a lambda which will be triggered whenever the value changes
     */
    fun valueListener(lambda: ValueListener<T, V>)
}

internal class KolibreeValueDelegateImpl<T, V>(initialValue: V) : KolibreeValueDelegate<T, V> {

    override var currentValue: V = initialValue
        private set

    override var valueListener: ValueListener<T, V> = { newValue -> currentValue = newValue }
        private set

    override fun valueListener(lambda: ValueListener<T, V>) {
        this.valueListener = { newValue ->
            currentValue = newValue
            lambda.invoke(this, currentValue)
        }
    }
}

internal class Invoker<T, V>(val view: T, val lambda: ValueListener<T, V>) {
    fun invoke(value: V) =
        view.lambda(value)
}
