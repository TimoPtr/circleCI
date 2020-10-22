/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.models.ProfileSmiles
import com.kolibree.android.rewards.persistence.ProfileSmilesRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Test

internal class SmilesUseCaseTest : BaseUnitTest() {

    private lateinit var smilesUseCase: SmilesUseCase

    private val repository = mock<ProfileSmilesRepository>()

    override fun setup() {
        super.setup()
        smilesUseCase = SmilesUseCase(repository)
    }

    /*
    smilesAmountStream
     */

    @Test
    fun `smilesAmountStream emits 0 when repository profileProgress emit empty list`() {
        whenever(repository.profileProgress()).thenReturn(Flowable.just(emptyList()))
        smilesUseCase.smilesAmountStream().test().assertValue(0)
    }

    @Test
    fun `smilesAmountStream emits sum of all profileSmile returns by repository profileProgress`() {
        val profileSmile1 = StubProfileSmiles(1, 10)
        val profileSmile2 = StubProfileSmiles(2, 100)
        whenever(repository.profileProgress()).thenReturn(Flowable.just(listOf(profileSmile1, profileSmile2)))
        smilesUseCase.smilesAmountStream().test().assertValue(110)
    }

    private data class StubProfileSmiles(override val profileId: Long, override val smiles: Int) : ProfileSmiles
}
