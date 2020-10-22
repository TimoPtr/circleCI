/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons

import androidx.annotation.Keep
import com.kolibree.android.KolibreeExperimental
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

/**
 * This can simplify the use of weak references by utilising Delegate Properties
 *
 * To initialize a value. For example:
 * var activity by Weak{ context }
 *
 * You have to specify generics if you don't initialize it. For example:
 * var activity:Activity? by Weak() or var activity by Weak<Activity>()
 */

@Keep
@KolibreeExperimental
class Weak<T : Any>(initializer: () -> T?) {
    private var weakReference = WeakReference(initializer())

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return weakReference.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        weakReference = WeakReference(value)
    }
}
