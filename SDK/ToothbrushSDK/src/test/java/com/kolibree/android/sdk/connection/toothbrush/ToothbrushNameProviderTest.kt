package com.kolibree.android.sdk.connection.toothbrush

import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sdk.R
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Test

internal class ToothbrushNameProviderTest : BaseUnitTest() {

    private val context = mock<Context>()

    private lateinit var provider: ToothbrushNameProvider

    override fun setup() {
        super.setup()

        provider = ToothbrushNameProvider(context)
    }

    /*
    provide
     */

    @Test
    fun `provide invoke getString on context object`() {
        val name = "Name-B1"
        whenever(context.getString(R.string.displayable_name_b1)).thenReturn(name)

        assertEquals(name, provider.provide(CONNECT_B1))
        verify(context).getString(R.string.displayable_name_b1)
    }

    /*
    provideResource
     */

    @Test
    fun `provideResource for ARA returns resource displayable_name_ara`() {
        assertEquals(R.string.displayable_name_ara, provider.provideResource(ARA))
    }

    @Test
    fun `provideResource for CONNECT_E1 returns resource displayable_name_e1`() {
        assertEquals(R.string.displayable_name_e1, provider.provideResource(CONNECT_E1))
    }

    @Test
    fun `provideResource for CONNECT_E2 returns resource displayable_name_e2`() {
        assertEquals(R.string.displayable_name_e2, provider.provideResource(CONNECT_E2))
    }

    @Test
    fun `provideResource for CONNECT_M1 returns resource displayable_name_m1`() {
        assertEquals(R.string.displayable_name_m1, provider.provideResource(CONNECT_M1))
    }

    @Test
    fun `provideResource for CONNECT_B1 returns resource displayable_name_b1`() {
        assertEquals(R.string.displayable_name_b1, provider.provideResource(CONNECT_B1))
    }

    @Test
    fun `provideResource for PLAQLESS returns resource displayable_name_pql`() {
        assertEquals(R.string.displayable_name_pql, provider.provideResource(PLAQLESS))
    }

    @Test
    fun `provideResource for HILINK returns resource displayable_name_hilink`() {
        assertEquals(R.string.displayable_name_hilink, provider.provideResource(HILINK))
    }

    @Test
    fun `provideResource for GLINT returns resource displayable_name_hilink`() {
        assertEquals(R.string.displayable_name_glint, provider.provideResource(GLINT))
    }
}
