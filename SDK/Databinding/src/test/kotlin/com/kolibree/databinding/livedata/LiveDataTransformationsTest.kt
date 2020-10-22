/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.livedata

import androidx.lifecycle.MutableLiveData
import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.databinding.livedata.LiveDataTransformations.combineLatest
import org.junit.Test

class LiveDataTransformationsTest : BaseUnitTest() {

    @Test
    fun `map applies transformation to every element emitted by the source live data`() {
        val sourceLiveData = MutableLiveData<Int>()
        val mapLiveData = LiveDataTransformations.map<Int, Int>(sourceLiveData) { it?.plus(1) }
        val observer = mapLiveData.test()

        val maxElements = 100
        repeat(0.until(maxElements).count()) { element -> sourceLiveData.value = element }

        // For now we allow the initial null, for backward compat.
        // this may change in the future.
        observer.assertValueHistory(null, *(1.until(maxElements + 1).toList().toTypedArray()))
    }

    @Test
    fun `map allows null return values from mapper`() {
        val sourceLiveData = MutableLiveData<Int>()
        val mapLiveData = LiveDataTransformations.map<Int, Int>(sourceLiveData) { null }
        val observer = mapLiveData.test()

        val maxElements = 100
        repeat(0.until(maxElements).count()) { element -> sourceLiveData.value = element }

        // For now we allow the initial null, for backward compat.
        // this may change in the future.
        observer.assertValueHistory(*arrayOfNulls(maxElements + 1))
    }

    @Test
    fun `mapNonNull applies transformation to every element emitted by the source live data`() {
        val sourceLiveData = MutableLiveData<Int>()
        val mapLiveData =
            LiveDataTransformations.mapNonNull<Int, Int>(sourceLiveData, 0) { it + 1 }
        val observer = mapLiveData.test()

        val maxElements = 100
        repeat(0.until(maxElements).count()) { element -> sourceLiveData.value = element }

        observer.assertValueHistory(0, *(1.until(maxElements + 1).toList().toTypedArray()))
    }

    @Test
    fun `mapNonNull emits default value before mapping input stream`() {
        val defaultValue = -100
        val sourceLiveData = MutableLiveData<Int>()
        val mapLiveData =
            LiveDataTransformations.mapNonNull<Int, Int>(
                sourceLiveData,
                defaultValue = defaultValue
            ) { it + 1 }
        val observer = mapLiveData.test()

        val maxElements = 100
        repeat(0.until(maxElements).count()) { element -> sourceLiveData.value = element }

        observer.assertValueHistory(
            defaultValue,
            *(1.until(maxElements + 1).toList().toTypedArray())
        )
    }

    @Test
    fun `combineLatest is triggered each time lhs and rhs emit new values`() {
        val lhs = MutableLiveData<Int>()
        val rhs = MutableLiveData<Boolean>()

        val combined = combineLatest(lhs, rhs) { left, right ->
            if (left == null) null
            else left * 2 + if (right == true) 1 else 0
        }
        val observer = combined.test()

        val maxIndex = 10
        repeat(0.rangeTo(maxIndex).count()) { element -> lhs.value = element }
        observer.assertValueHistory(
            *(0.rangeTo(maxIndex).map { it * 2 }.toList().toTypedArray())
        )

        rhs.value = false
        observer.assertValue(maxIndex * 2)

        rhs.value = true
        observer.assertValue(maxIndex * 2 + 1)

        lhs.value = maxIndex - 1
        observer.assertValue((maxIndex - 1) * 2 + 1)
    }

    @Test
    fun `distinctUntilChanged emits only distinct values`() {
        val sourceLiveData = MutableLiveData<Int>()
        val distinctLiveData = sourceLiveData.distinctUntilChanged()
        val observer = distinctLiveData.test()

        val maxElements = 100
        repeat(0.until(maxElements).count()) { element ->
            repeat(0.until(3).count()) { sourceLiveData.value = element }
        }

        observer.assertValueHistory(*(0.until(maxElements).toList().toTypedArray()))
    }

    @Test
    fun `map with distinctUntilChanged emits only distinct values from mapped live data`() {
        val sourceLiveData = MutableLiveData<Int>()
        val mapLiveData = LiveDataTransformations.map(sourceLiveData) { it }
        val distinctLiveData = mapLiveData.distinctUntilChanged()
        val observer = distinctLiveData.test()

        val maxElements = 100
        repeat(0.until(maxElements).count()) { element ->
            repeat(0.until(3).count()) { sourceLiveData.value = element }
        }

        // For now we allow the initial null, for backward compat.
        // this may change in the future.
        observer.assertValueHistory(null, *(0.until(maxElements).toList().toTypedArray()))
    }

    @Test
    fun `mapNonNull with distinctUntilChanged emits only distinct values from mapped live data`() {
        val sourceLiveData = MutableLiveData<Int>()
        val mapLiveData = LiveDataTransformations.mapNonNull(sourceLiveData, 0) { it }
        val distinctLiveData = mapLiveData.distinctUntilChanged()
        val observer = distinctLiveData.test()

        val maxElements = 100
        repeat(0.until(maxElements).count()) { element ->
            repeat(0.until(3).count()) { sourceLiveData.value = element }
        }

        observer.assertValueHistory(*(0.until(maxElements).toList().toTypedArray()))
    }

    @Test
    fun `map with distinctUntilChanged emits only distinct null values`() {
        val sourceLiveData = MutableLiveData<Int>()
        val mapLiveData = LiveDataTransformations.map<Int, Int>(sourceLiveData) { null }
        val observer = mapLiveData.distinctUntilChanged().test()

        val maxElements = 100
        repeat(0.until(maxElements).count()) { element -> sourceLiveData.value = element }

        observer.assertValueHistory(null)
    }
}
