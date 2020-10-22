/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings

import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepository
import com.kolibree.android.sdk.e1.ToothbrushShutdownValve
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class CoachSettingsViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: CoachSettingsViewModel

    private val connector: IKolibreeConnector = mock()
    private val coachSettingsRepository: CoachSettingsRepository = mock()
    private val shutdownValve: ToothbrushShutdownValve = mock()

    private val kolibreeServiceInteractor = mock<KolibreeServiceInteractor>()

    override fun setup() {
        super.setup()

        whenever(connector.currentProfile).thenReturn(
            ProfileBuilder.create()
                .withName("FIRST_NAME")
                .withMaleGender()
                .withHandednessRight()
                .withId(0)
                .withPoints(0)
                .build()
        )
        viewModel =
            CoachSettingsViewModel(
                coachSettingsRepository,
                connector,
                kolibreeServiceInteractor,
                shutdownValve
            )
    }

    @Test
    fun `test service interactor LifecycleOwner has been set`() {
        val owner: LifecycleOwner = mock()
        viewModel.onCreate(owner)
        verify(kolibreeServiceInteractor).setLifecycleOwner(owner)
    }
}
