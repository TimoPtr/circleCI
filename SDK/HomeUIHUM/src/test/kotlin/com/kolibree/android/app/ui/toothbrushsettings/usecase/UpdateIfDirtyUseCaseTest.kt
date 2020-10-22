/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.usecase

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.createAccountToothbrush
import com.kolibree.pairing.usecases.UpdateToothbrushUseCase
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import junit.framework.TestCase.assertFalse
import org.junit.Test

class UpdateIfDirtyUseCaseTest : BaseUnitTest() {
    private val toothbrushRepository: ToothbrushRepository = mock()
    private val updateToothbrushUseCase: UpdateToothbrushUseCase = mock()

    private val useCase = UpdateIfDirtyUseCase(toothbrushRepository, updateToothbrushUseCase)

    @Test
    fun `maybeUpdate does nothing if there's no toothbrush associated to mac`() {
        whenever(toothbrushRepository.getAccountToothbrush(any()))
            .thenReturn(Maybe.empty())

        useCase.maybeUpdate(createConnection()).test().assertComplete().assertNoErrors()

        verifyNoMoreInteractions(updateToothbrushUseCase)
    }

    @Test
    fun `maybeUpdate does nothing if accountToothbrush is not dirty`() {
        val accountToothbrush = createAccountToothbrush()
        whenever(toothbrushRepository.getAccountToothbrush(any()))
            .thenReturn(Maybe.just(accountToothbrush))

        assertFalse(accountToothbrush.dirty)

        useCase.maybeUpdate(createConnection()).test().assertComplete().assertNoErrors()

        verifyNoMoreInteractions(updateToothbrushUseCase)
    }

    @Test
    fun `maybeUpdate updates remote repository if accountToothbrush is dirty, and then it cleans dirty state`() {
        val accountToothbrush = createAccountToothbrush()
        whenever(toothbrushRepository.getAccountToothbrush(any()))
            .thenReturn(Maybe.just(accountToothbrush))

        assertFalse(accountToothbrush.dirty)

        useCase.maybeUpdate(createConnection()).test().assertComplete().assertNoErrors()

        verifyNoMoreInteractions(updateToothbrushUseCase)
    }

    private fun createConnection() = KLTBConnectionBuilder.createAndroidLess().build()
}
