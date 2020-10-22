/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.usecases

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.synchronization.SynchronizationState.Failure
import com.kolibree.android.synchronization.SynchronizationState.None
import com.kolibree.android.synchronization.SynchronizationState.Ongoing
import com.kolibree.android.synchronization.SynchronizationState.Success
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.kolibree.android.test.extensions.withFixedInstant
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import org.junit.Test

class BackendSynchronizationUseCaseTest : BaseUnitTest() {
    private val useCase = BackendSynchronizationStateUseCase()

    @Test
    fun `subscription starts with state None`() {
        useCase.onceAndStream.test()
            .assertValue(None)
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `onSyncStart emits state Ongoing with current timestamp`() = withFixedInstant {
        val observer = useCase.onceAndStream.test()
            .assertLastValue(None)

        useCase.onSyncStarted()

        observer.assertLastValueWithPredicate {
            it is Ongoing && it.timestamp == this
        }
    }

    @Test
    fun `multiple onSyncStart only emit state once`() = withFixedInstant {
        val observer = useCase.onceAndStream.test()

        useCase.onSyncStarted()
        useCase.onSyncStarted()
        useCase.onSyncStarted()

        observer.assertLastValueWithPredicate {
            it is Ongoing && it.timestamp == this
        }
    }

    @Test
    fun `onSyncSuccess emits state Success with current timestamp if previous value was Ongoing`() =
        withFixedInstant {
            val observer = useCase.onceAndStream.test()
                .assertLastValue(None)

            useCase.onSyncStarted()

            observer.assertLastValueWithPredicate {
                it is Ongoing && it.timestamp == this
            }

            useCase.onSyncSuccess()

            observer.assertLastValueWithPredicate {
                it is Success && it.timestamp == this
            }
        }

    @Test
    fun `onSyncFailed emits state Failure with current timestamp if previous value was Ongoing`() =
        withFixedInstant {
            val observer = useCase.onceAndStream.test()
                .assertLastValue(None)

            useCase.onSyncStarted()

            observer.assertLastValueWithPredicate {
                it is Ongoing && it.timestamp == this
            }

            useCase.onSyncFailed()

            observer.assertLastValueWithPredicate {
                it is Failure && it.timestamp == this
            }
        }

    @Test
    fun `consecutive onSyncSuccess throw FailEarly`() {
        val observer = useCase.onceAndStream.test()

        useCase.onSyncStarted()

        useCase.onSyncSuccess()
        useCase.onSyncSuccess()

        observer.assertError(AssertionError::class.java)
    }

    @Test
    fun `consecutive onSyncFailed throw FailEarly`() {
        val observer = useCase.onceAndStream.test()

        useCase.onSyncStarted()

        useCase.onSyncFailed()
        useCase.onSyncFailed()

        observer.assertError(AssertionError::class.java)
    }

    @Test
    fun `consecutive onSyncStarted throw FailEarly`() {
        val observer = useCase.onceAndStream.test()

        useCase.onSyncStarted()

        useCase.onSyncStarted()
        useCase.onSyncStarted()

        observer.assertError(AssertionError::class.java)
    }

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }
}
