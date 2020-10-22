/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding

import androidx.annotation.Keep

/**
 * Common interface for collection binding listeners.
 */
@Keep
sealed class CollectionBindingListener

@Keep
abstract class OnItemClickListener<T> : CollectionBindingListener() {

    abstract fun onItemClick(item: T)
}

@Keep
abstract class OnValueChangedListener<T, V> : CollectionBindingListener() {

    abstract fun onValueChanged(item: T, value: V)
}

@Keep
abstract class OnValueToggledListener<T> : CollectionBindingListener() {

    abstract fun onValueToggled(item: T, value: Boolean)
}
