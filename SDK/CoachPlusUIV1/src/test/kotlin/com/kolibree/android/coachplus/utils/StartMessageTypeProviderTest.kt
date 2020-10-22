package com.kolibree.android.coachplus.utils

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.R
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.HUM_BATTERY
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.spy
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test

internal class StartMessageTypeProviderTest : BaseUnitTest() {

    private lateinit var provider: StartMessageTypeProvider

    override fun setup() {
        super.setup()

        provider = spy(StartMessageTypeProvider())
    }

    /*
    provideType
     */

    @Test
    fun `provideType returns NoModel for 0 connection`() {
        val connections = emptyList<KLTBConnection>()
        val type = provider.provideType(connections)
        assertEquals(NoModel, type)
    }

    @Test
    fun `provideType returns MultipleModels for 2 different connections`() {
        val connections = listOf(
            mockConnection(CONNECT_B1),
            mockConnection(CONNECT_E2)
        )
        val type = provider.provideType(connections)
        assertEquals(MultipleModels, type)
    }

    @Test
    fun `provideType returns OneModel for 1 connection`() {
        val connections = listOf(
            mockConnection(CONNECT_M1)
        )
        val type = provider.provideType(connections)
        Assert.assertTrue(type is OneModel)
    }

    @Test
    fun `provideType returns OneModel for 2 same connections`() {
        val connections = listOf(
            mockConnection(ARA),
            mockConnection(ARA)
        )
        val type = provider.provideType(connections)
        Assert.assertTrue(type is OneModel)
    }

    @Test
    fun `ARA returns raw resource anim_step2_ara`() {
        val resource = provider.modelResource(ARA)
        assertEquals(R.raw.anim_step2_ara, resource)
    }

    @Test
    fun `CONNECT_E1 returns raw resource anim_step2_e1`() {
        val resource = provider.modelResource(CONNECT_E1)
        assertEquals(R.raw.anim_step2_e1, resource)
    }

    @Test
    fun `CONNECT_E2 returns raw resource anim_step2_e2`() {
        val resource = provider.modelResource(CONNECT_E2)
        assertEquals(R.raw.anim_step2_e2, resource)
    }

    @Test
    fun `HUM_ELECTRIC returns raw resource anim_step2_e2`() {
        val resource = provider.modelResource(ToothbrushModel.HUM_ELECTRIC)
        assertEquals(R.raw.anim_step2_e2, resource)
    }

    @Test
    fun `modelResource returns raw resource anim_step2_e2 when model is HILINK`() {
        val resource = provider.modelResource(HILINK)
        assertEquals(R.raw.anim_step2_e2, resource)
    }

    @Test
    fun `CONNECT_M1 returns raw resource anim_m1`() {
        val resource = provider.modelResource(CONNECT_M1)
        assertEquals(R.raw.anim_m1, resource)
    }

    @Test
    fun `HUM_BATTERY returns raw resource anim_step2_b1`() {
        val resource = provider.modelResource(HUM_BATTERY)
        assertEquals(R.raw.anim_step2_b1, resource)
    }

    @Test
    fun `CONNECT_B1 returns raw resource anim_step2_b1`() {
        val resource = provider.modelResource(CONNECT_B1)
        assertEquals(R.raw.anim_step2_b1, resource)
    }

    @Test
    fun `GLINT returns raw resource anim_step2_e2`() {
        val resource = provider.modelResource(GLINT)
        assertEquals(R.raw.anim_step2_e2, resource)
    }

    @Test
    fun `CONNECT_E1 returns description coach_start_message_title`() {
        val resource = provider.descriptionResource(CONNECT_E1)
        assertEquals(R.string.coach_start_message_title, resource)
    }

    @Test
    fun `CONNECT_E2 returns description coach_start_message_title`() {
        val resource = provider.descriptionResource(CONNECT_E2)
        assertEquals(R.string.coach_start_message_title, resource)
    }

    @Test
    fun `CONNECT_M1 returns description coach_start_message_title_manual`() {
        val resource = provider.descriptionResource(CONNECT_M1)
        assertEquals(R.string.coach_start_message_title_manual, resource)
    }

    @Test
    fun `CONNECT_B1 returns description coach_start_message_title`() {
        val resource = provider.descriptionResource(CONNECT_B1)
        assertEquals(R.string.coach_start_message_title, resource)
    }

    @Test
    fun `HUM_BATTERY returns description coach_start_message_title`() {
        val resource = provider.descriptionResource(HUM_BATTERY)
        assertEquals(R.string.coach_start_message_title, resource)
    }

    @Test
    fun `GLINT returns description coach_start_message_title`() {
        val resource = provider.descriptionResource(GLINT)
        assertEquals(R.string.coach_start_message_title, resource)
    }

    private fun mockConnection(model: ToothbrushModel): KLTBConnection {
        return KLTBConnectionBuilder.createAndroidLess().withModel(model).build()
    }
}
