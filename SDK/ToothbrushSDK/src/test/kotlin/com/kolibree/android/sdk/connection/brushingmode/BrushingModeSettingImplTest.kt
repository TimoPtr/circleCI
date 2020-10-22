package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

class BrushingModeSettingImplTest : BaseUnitTest() {

    private val brushingModeRepository = mock<BrushingModeRepository>()

    private val confirmUserModeUseCase = mock<ConfirmBrushingModeUseCase>()

    private lateinit var brushingModeSetting: BrushingModeSettingImpl

    @Before
    fun setUp() {
        brushingModeSetting =
            spy(BrushingModeSettingImpl(confirmUserModeUseCase, brushingModeRepository))
    }

    @Test
    fun `getBrushingMode invokes getForProfile on BrushingModeRepository`() {
        val profileId = 123L
        val testMode =
            ProfileBrushingMode(profileId, BrushingMode.Slow, TrustedClock.getNowOffsetDateTime())
        whenever(brushingModeRepository.getForProfile(profileId)).thenReturn(testMode)
        brushingModeSetting.getBrushingMode(profileId)
        verify(brushingModeRepository).getForProfile(profileId)
    }

    @Test
    fun `setBrushingMode invokes confirmBrushingModeCompletable on ConfirmBrushingModeUseCase`() {
        val profileId = 123L
        brushingModeSetting.setBrushingMode(profileId, BrushingMode.Strong)
        verify(confirmUserModeUseCase).confirmBrushingModeCompletable(
            profileId,
            BrushingMode.Strong
        )
    }
}
