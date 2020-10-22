/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.test

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.setFixedDate
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.ZoneOffset
import timber.log.Timber

@Ignore("Sandbox unit test for kotlin")
class KotlinSandboxUnitTest : BaseUnitTest() {
    @Test
    fun `concatWith after error`() {
        Flowable.just(1, 2)
            .concatWith(Single.just(99))
            .subscribe(
                { Timber.d("good stream $it") },
                Timber::e
            )

        Flowable.error<Int>(Exception())
            .concatWith(Single.just(100))
            .subscribe(
                { Timber.d("error stream $it") },
                Timber::e
            )
    }

    @Test
    fun `datetime equality`() {
        TrustedClock.setFixedDate()

        val time1 = TrustedClock.getNowZonedDateTimeUTC()
        val time2 = TrustedClock.getNowZonedDateTime()

        Timber.d("Times are equal! %s", time1 == time2)
        Timber.d("Times are equal! %s", time1.isEqual(time2))
    }

    @Test
    fun `datetime comparisons`() {
        val time1 = TrustedClock.getNowZonedDateTime()
        val time2 = time1.withZoneSameInstant(ZoneOffset.UTC)

        Timber.d("Duration is %s", Duration.between(time1, time2))

        Timber.d("Duration is %s", Duration.between(time1.minusMinutes(5), time2))

        Timber.d("Is after %s", time1 > time2)
        Timber.d("Is after %s", time1.isAfter(time2))
        Timber.d("Is before %s", time1 < time2)
        Timber.d("Is before %s", time1.isBefore(time2))

        assertEquals(time1, time2)
    }

    @Test
    fun `delay with delayError = false should immediately report error`() {
        mapOf<String, String>().toList()
        val exception = Exception()
        Completable.error(exception)
            .delay(5, TimeUnit.SECONDS, TestScheduler())
            .test()
            .assertError(exception)
    }

    @Test
    fun `delay with delayError = true should report error with delay`() {
        val scheduler = TestScheduler()

        val secondsDelay = 5L

        val exception = Exception()
        val observer = Completable.error(exception)
            .delay(secondsDelay, TimeUnit.SECONDS, scheduler, true)
            .test()
            .assertNoErrors()

        scheduler.advanceTimeBy(secondsDelay, TimeUnit.SECONDS)

        observer.assertError(exception)
    }

    @Test
    fun `delay with delayError = false should not wait to report error`() {
        val scheduler = TestScheduler()

        val secondsDelay = 5L

        val exception = Exception()
        val observer = Completable.error(exception)
            .delay(secondsDelay, TimeUnit.SECONDS, scheduler, false)
            .test()
            .assertNoErrors()

        scheduler.advanceTimeBy(secondsDelay, TimeUnit.SECONDS)

        observer.assertError(exception)
    }
}
