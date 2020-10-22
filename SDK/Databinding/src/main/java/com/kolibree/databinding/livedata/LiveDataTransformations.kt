/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.databinding.livedata

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.utils.callSafely

/**
 * @author lookashc
 */
@Suppress("Unused")
@Keep
object LiveDataTransformations {

    /**
     * Kotlin version of [Transformations.map] with some tweaks and improvements.
     *
     * @param dependency source LiveData
     * @param mapper P -> T transformation function
     * @return LiveData that returns value T, mapped from P by mapper
     */
    inline fun <P, T> map(
        dependency: LiveData<P>,
        crossinline mapper: (P?) -> T?
    ): LiveData<T> = MediatorLiveData<T>().also { mediatorLiveData ->
        mediatorLiveData.value = mapper(dependency.value)
        val observer = Observer<P> {
            FailEarly.failIfNotExecutedOnMainThread()
            mediatorLiveData.value = mapper(dependency.value)
        }
        mediatorLiveData.addSource(dependency, observer)
    }

    /**
     * Non-null version of [Transformations.map]. Prevents LiveData from emitting nulls.
     *
     * @param dependency source LiveData
     * @param defaultValue optional default value, null by default
     * @param mapper P -> T transformation function
     * @return LiveData that returns value T, mapped from P by mapper
     */
    inline fun <P, T> mapNonNull(
        dependency: LiveData<P>,
        defaultValue: T,
        crossinline mapper: (P) -> T
    ): LiveData<T> = MediatorLiveData<T>().also { mediatorLiveData ->
        mediatorLiveData.value = defaultValue
        val observer = Observer<P> {
            FailEarly.failIfNotExecutedOnMainThread()
            dependency.value?.let {
                mapper(it)?.let { mappedValue -> mediatorLiveData.value = mappedValue }
            }
        }
        mediatorLiveData.addSource(dependency, observer)
    }

    /**
     * Alternative version of [Transformations.map] which is able to merge more than one source LiveData.
     * Sources can return different types, mapper is responsible for combining them for final result.
     *
     * @param dependencies sources to be combined
     * @param initialValue if non-null, result will be initialized with this value upon creation, default - null
     * @param mapper transformation function
     * @return LiveData that returns value T, mapped from dependencies by mapper
     */
    inline fun <T> merge(
        vararg dependencies: LiveData<*>,
        initialValue: T? = null,
        crossinline mapper: () -> T?
    ): LiveData<T> = MediatorLiveData<T>().also { mediatorLiveData ->
        if (initialValue != null) {
            FailEarly.failIfNotExecutedOnMainThread()
            mediatorLiveData.value = initialValue
        }
        val observer = Observer<Any> {
            FailEarly.failIfNotExecutedOnMainThread()
            val newValue = mapper()
            if (mediatorLiveData.value != newValue) mediatorLiveData.value = newValue
        }
        dependencies.forEach { dependencyLiveData ->
            mediatorLiveData.addSource(dependencyLiveData, observer)
        }
    }

    /**
     * Alternative version of [merge]. Allows to combine 2 sources together, mapping is triggered
     * each time one of the sources emits new value.
     * Sources can return different types, mapper is responsible for combining them for final result.
     *
     * @param lhs left-hand side source to be combined or type [L]
     * @param rhs right-hand side source to be combined [R]
     * @param initialValue if non-null, result will be initialized with this value upon creation, default - null
     * @param mapper transformation function ([L], [R]) -> [T]
     * @return LiveData that returns value T, mapped from dependencies by mapper
     */
    inline fun <L, R, T> combineLatest(
        lhs: LiveData<L>,
        rhs: LiveData<R>,
        initialValue: T? = null,
        crossinline mapper: (L?, R?) -> T?
    ): LiveData<T> = MediatorLiveData<T>().also { mediatorLiveData ->
        if (initialValue != null) {
            FailEarly.failIfNotExecutedOnMainThread()
            mediatorLiveData.value = initialValue
        }
        val updateBlock = {
            FailEarly.failIfNotExecutedOnMainThread()
            val newValue = mapper(lhs.value, rhs.value)
            if (mediatorLiveData.value != newValue) mediatorLiveData.value = newValue
        }
        mediatorLiveData.addSource(lhs) { updateBlock() }
        mediatorLiveData.addSource(rhs) { updateBlock() }
    }

    /**
     * Map that allows us to link two-way data binding (denoted by `@={}` in XMLs). It will call its update handler
     * function each time its value changes.
     *
     * @param dependency source LiveData
     * @param mapper P -> T transformation function
     * @param updateHandler T -> () update function
     * @return LiveData that returns value T, mapped from P by mapper, and calls update handlers when its value changes
     */
    inline fun <T, P> twoWayMap(
        dependency: LiveData<P>,
        crossinline mapper: (P?) -> T?,
        crossinline updateHandler: ((T?) -> Unit)
    ) = MediatorLiveData<T>().also { mediatorLiveData ->
        mediatorLiveData.value = mapper(dependency.value)

        val mapperObserver = Observer<P> {
            FailEarly.failIfNotExecutedOnMainThread()
            val newValue = mapper(dependency.value)
            if (newValue != mediatorLiveData.value) {
                mediatorLiveData.value = newValue
            }
        }
        mediatorLiveData.addSource(dependency, mapperObserver)

        val updateObserver = object : Observer<T> {

            var first = true

            override fun onChanged(t: T) {
                if (!first) callSafely { updateHandler(mediatorLiveData.value) }
                first = false
            }
        }
        mediatorLiveData.addSource(mediatorLiveData, updateObserver)
    }
}

/**
 * Creates a new LiveData object that does not emit a value until the source LiveData value has
 * been changed. The value is considered changed if equals() yields false.
 *
 * We keep our own version of the operator, since arch components 2.1.0 are still in alpha.
 *
 * //TODO revisit once arch components 2.1.0 reaches stable state
 *
 * @see https://developer.android.com/reference/androidx/lifecycle/Transformations.html#distinctUntilChanged(androidx.lifecycle.LiveData%3CX%3E)
 * @see https://stackoverflow.com/a/54639585
 * @return LiveData that returns value new T only when it is different than previously emitted value
 */
@Keep
fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> =
    MediatorLiveData<T>().also { mediatorLiveData ->
        mediatorLiveData.addSource(this, object : Observer<T> {
            private var isInitialized = false
            private var previousValue: T? = null

            override fun onChanged(newValue: T?) {
                val wasInitialized = isInitialized
                if (!isInitialized) {
                    isInitialized = true
                }
                if (!wasInitialized || newValue != previousValue) {
                    previousValue = newValue
                    mediatorLiveData.postValue(newValue)
                }
            }
        })
    }
