/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.livedata

import android.annotation.SuppressLint
import androidx.arch.core.util.Function
import androidx.core.util.Consumer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jraska.livedata.TestObserver
import java.util.concurrent.TimeUnit

/**
 * Test observer for two-way data binding.
 *
 * Wraps [TestObserver] since it's a final class and duplicates its interface
 * to keep its functionality.
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
class TwoWayTestObserver<T> private constructor(
    private val liveData: MutableLiveData<T>
) : Observer<T> {

    private val impl = TestObserver.test(liveData)

    fun update(value: T) {
        liveData.postValue(value)
    }

    override fun onChanged(value: T?) {
        impl.onChanged(value)
    }

    fun value(): T = impl.value()

    fun valueHistory(): MutableList<T> = impl.valueHistory()

    fun assertHasValue(): TestObserver<T> = impl.assertHasValue()

    fun assertNoValue(): TestObserver<T> = impl.assertNoValue()

    fun assertHistorySize(expectedSize: Int): TestObserver<T> = impl.assertHistorySize(expectedSize)

    fun assertValue(expected: T): TestObserver<T> = impl.assertValue(expected)

    fun assertValue(valuePredicate: Function<T?, Boolean?>): TestObserver<T> =
        impl.assertValue(valuePredicate)

    fun assertValueHistory(vararg values: T): TestObserver<T> =
        impl.assertValueHistory(*values)

    fun assertNever(valuePredicate: Function<T, Boolean>): TestObserver<T> =
        impl.assertNever(valuePredicate)

    fun <N> map(mapper: Function<T?, N>): TestObserver<N> = impl.map(mapper)

    fun doOnChanged(onChanged: Consumer<T?>): TestObserver<T> = impl.doOnChanged(onChanged)

    fun awaitValue(): TestObserver<T> = impl.awaitValue()

    fun awaitValue(
        timeout: Long,
        timeUnit: TimeUnit?
    ): TestObserver<T> = impl.awaitValue(timeout, timeUnit)

    @Throws(InterruptedException::class)
    fun awaitNextValue(): TestObserver<T> = impl.awaitNextValue()

    @Throws(InterruptedException::class)
    fun awaitNextValue(
        timeout: Long,
        timeUnit: TimeUnit?
    ): TestObserver<T> = impl.awaitNextValue(timeout, timeUnit)

    companion object {

        @SuppressLint("SdkPublicExtensionMethodWithoutKeep")
        fun <T> MutableLiveData<T>.testTwoWay(): TwoWayTestObserver<T> {
            return TwoWayTestObserver(this)
        }
    }
}
