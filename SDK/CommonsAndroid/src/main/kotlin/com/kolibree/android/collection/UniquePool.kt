/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.collection

import com.kolibree.android.annotation.VisibleForApp
import java.lang.ref.WeakReference
import java.util.ArrayList
import timber.log.Timber

/**
 * Collection of unique WeakReferences to objects on a given type.
 * Logic was extracted from ListenerPool from ToothbrushSDK.
 *
 * @author lookashc
 */
@VisibleForApp
class UniquePool<T> {

    @PublishedApi
    internal val internal = ArrayList<WeakReference<T>>()

    val isEmpty: Boolean
        get() = internal.isEmpty()

    /**
     * Adds new element to collection.
     * @param obj element to be added
     * @return size of collection after operation
     */
    fun add(obj: T): Int {
        synchronized(internal) {
            if (purgeLeakingAndGet(obj) != null) {
                Timber.i("$obj is already in the pool")
            } else {
                internal.add(WeakReference(obj))
            }
            return internal.size
        }
    }

    /**
     * Adds elements to collection.
     * @param objs elements to be added
     * @return size of collection after operation
     */
    fun addAll(objs: UniquePool<T>): Int {
        synchronized(internal) {
            val purgedCollection = objs.internal
                .map { obj -> obj to obj.get()?.let { purgeLeakingAndGet(it) } }
                .filter { pair -> pair.second == null }
                .map { pair -> pair.first }
                .toList()
            internal.addAll(purgedCollection)
            return internal.size
        }
    }

    /**
     * Remove element from collection.
     * @param obj elements to be removed
     * @return size of collection after operation
     */
    fun remove(obj: T): Int {
        synchronized(internal) {
            val ref = purgeLeakingAndGet(obj)
            if (ref != null) {
                internal.remove(ref)
            } else {
                Timber.i("$obj was not registered")
            }

            return internal.size
        }
    }

    /**
     * Removes element from collection.
     * @param objs elements to be removed
     * @return size of collection after operation
     */
    fun removeAll(objs: UniquePool<T>): Int {
        synchronized(internal) {
            val purgedCollection = objs.internal
                .map { obj -> obj.get()?.let { purgeLeakingAndGet(it) } }
                .filter { ref -> ref?.get() != null }
                .toList()
            internal.removeAll(purgedCollection)
            return internal.size
        }
    }

    /**
     * Removes all elements
     */
    fun clear() {
        synchronized(internal) {
            internal.clear()
        }
    }

    fun size() = internal.size

    inline fun forEach(action: (T) -> Unit) {
        synchronized(internal) {
            val purgedCollection = mutableListOf<WeakReference<T>>()
            return internal.forEach {
                it.get()?.let(action) ?: purgedCollection.add(it)
            }.also { internal.removeAll(purgedCollection) }
        }
    }

    /**
     * Get a weak reference to obj + purge leaking ones.
     *
     * @param obj non null object
     * @return null if the obj not yet in a collection, its reference otherwise
     */
    private fun purgeLeakingAndGet(obj: T): WeakReference<T>? {
        var reference: WeakReference<T>? = null

        val i = internal.iterator()
        while (i.hasNext()) {
            val ref = i.next()
            ref.get()?.let {
                if (it == obj) {
                    reference = ref
                }
            } ?: run {
                i.remove()
                Timber.w("A listener was leaking, make sure to unregister listeners after use")
            }
        }
        return reference
    }
}
