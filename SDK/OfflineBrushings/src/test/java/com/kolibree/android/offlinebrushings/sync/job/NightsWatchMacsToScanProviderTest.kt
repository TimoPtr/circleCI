/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class NightsWatchMacsToScanProviderTest : BaseUnitTest() {
    private val toothbrushRepository: ToothbrushRepository = mock()

    private val macsProvider = NightsWatchMacsToScanProvider(toothbrushRepository)

    @Test
    fun `provide returns empty list if listAll returns empty list`() {
        mockListAll()

        assertTrue(macsProvider.provide().isEmpty())
    }

    /*
    Utils
     */
    private fun mockListAll(vararg toothbrushes: AccountToothbrush) {
        whenever(toothbrushRepository.listAll()).thenReturn(Single.just(toothbrushes.toList()))
    }
}
