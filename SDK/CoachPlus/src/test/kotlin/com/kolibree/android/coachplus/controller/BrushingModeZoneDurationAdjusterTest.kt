package com.kolibree.android.coachplus.controller

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.BrushingModeRepository
import com.kolibree.android.sdk.connection.brushingmode.ProfileBrushingMode
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Duration

class BrushingModeZoneDurationAdjusterTest {
    private val goalDuration = Duration.ofSeconds(120L)
    private val connector: IKolibreeConnector = mock()
    private val brushingModeRepository: BrushingModeRepository = mock()
    private var profile = ProfileBuilder.create().build()

    @Before
    fun setUp() {
        doReturn(profile).whenever(connector).currentProfile
    }

    @Test
    fun `Slow Mode keeps original duration of all zones`() {
        val adjuster = createAdjuster(BrushingMode.Slow)
        MouthZone16.values().forEach { zone ->
            assertEquals(getOriginalPerZoneDuration(), adjuster.getAdjustedDuration(zone))
        }
    }

    @Test
    fun ` RegularMode adjusts duration of expected zones`() {
        val adjuster = createAdjuster(BrushingMode.Regular)
        zonesForRegularMode.forEach { zone ->
            assertEquals(getExpectedAdjustedDuration(), adjuster.getAdjustedDuration(zone))
        }
    }

    @Test
    fun ` RegularMode keeps duration of expected zones`() {
        val adjuster = createAdjuster(BrushingMode.Regular)
        MouthZone16.values()
            .filterNot { zonesForRegularMode.contains(it) }
            .forEach { zone ->
                assertEquals(getOriginalPerZoneDuration(), adjuster.getAdjustedDuration(zone))
            }
    }

    @Test
    fun `StrongMode adjusts duration of expected zones`() {
        val adjuster = createAdjuster(BrushingMode.Strong)
        zonesForStrongMode.forEach { zone ->
            assertEquals(getExpectedAdjustedDuration(), adjuster.getAdjustedDuration(zone))
        }
    }

    @Test
    fun `StrongMode keeps duration of expected zones`() {
        val adjuster = createAdjuster(BrushingMode.Strong)
        MouthZone16.values()
            .filterNot { zonesForStrongMode.contains(it) }
            .forEach { zone ->
                assertEquals(getOriginalPerZoneDuration(), adjuster.getAdjustedDuration(zone))
            }
    }

    @Test
    fun `Polishing Mode keeps original duration of all zones`() {
        val adjuster = createAdjuster(BrushingMode.Polishing)
        MouthZone16.values().forEach { zone ->
            assertEquals(getOriginalPerZoneDuration(), adjuster.getAdjustedDuration(zone))
        }
    }

    @Test
    fun `UserDefined Mode keeps original duration of all zones`() {
        val adjuster = createAdjuster(BrushingMode.UserDefined)
        MouthZone16.values().forEach { zone ->
            assertEquals(getOriginalPerZoneDuration(), adjuster.getAdjustedDuration(zone))
        }
    }

    private fun createAdjuster(brushingMode: BrushingMode): BrushingModeZoneDurationAdjuster {
        doReturn(ProfileBrushingMode(profile.id, brushingMode, TrustedClock.getNowOffsetDateTime()))
            .whenever(brushingModeRepository).getForProfile(profile.id)
        doReturn(ProfileBrushingMode(profile.id, brushingMode, TrustedClock.getNowOffsetDateTime()))
            .whenever(brushingModeRepository).getDefaultModeIfNull(profile.id)
        return BrushingModeZoneDurationAdjuster(goalDuration, connector, brushingModeRepository)
    }

    private fun getOriginalPerZoneDuration(): Long {
        return goalDuration.toMillis() / MouthZone16.values().size.toLong()
    }

    private fun getExpectedAdjustedDuration(): Long {
        return getOriginalPerZoneDuration().times(BrushingModeZoneDurationAdjuster.FACTOR).toLong()
    }

    companion object {
        val zonesForRegularMode = arrayOf(
            MouthZone16.UpMolLeOcc, MouthZone16.LoMolLeOcc,
            MouthZone16.UpMolRiOcc, MouthZone16.LoMolRiOcc
        )
        val zonesForStrongMode = arrayOf(
            MouthZone16.UpIncExt, MouthZone16.LoIncExt
        )
    }
}
