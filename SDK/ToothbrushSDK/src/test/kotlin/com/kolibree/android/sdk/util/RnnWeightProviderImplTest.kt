package com.kolibree.android.sdk.util

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.R
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class RnnWeightProviderImplTest : BaseUnitTest() {

    lateinit var rnnWeightProviderImpl: RnnWeightProviderImpl

    override fun setup() {
        super.setup()
        rnnWeightProviderImpl = RnnWeightProviderImpl(mock(), mock(), mock())
    }

    @Test
    fun `get weight and iv for ARA returns gru_data_ce1_cluster_3_0_3_bin_enc and gru_data_ce1_cluster_3_0_3_bin_iv`() {
        assertEquals(
            Pair(
                R.raw.gru_data_ce1_cluster_3_0_3_bin_enc,
                R.raw.gru_data_ce1_cluster_3_0_3_bin_iv
            ), rnnWeightProviderImpl.getEncryptedBinaryRes(ToothbrushModel.ARA)
        )
    }

    @Test
    fun `get weight and iv for E1 returns gru_data_ce1_cluster_3_0_3_bin_enc and gru_data_ce1_cluster_3_0_3_bin_iv`() {
        assertEquals(
            Pair(
                R.raw.gru_data_ce1_cluster_3_0_3_bin_enc,
                R.raw.gru_data_ce1_cluster_3_0_3_bin_iv
            ), rnnWeightProviderImpl.getEncryptedBinaryRes(ToothbrushModel.CONNECT_E1)
        )
    }

    @Test
    fun `get weight and iv for M1 returns gru_data_cm1_3_0_3_bin_enc and gru_data_cm1_3_0_3_bin_iv`() {
        assertEquals(
            Pair(
                R.raw.gru_data_cm1_3_0_3_bin_enc,
                R.raw.gru_data_cm1_3_0_3_bin_iv
            ), rnnWeightProviderImpl.getEncryptedBinaryRes(ToothbrushModel.CONNECT_M1)
        )
    }

    @Test
    fun `get weight and iv for E2 returns gru_data_ce2_cluster_3_0_4_bin_enc and gru_data_ce2_cluster_3_0_4_bin_iv`() {
        assertEquals(
            Pair(
                R.raw.gru_data_ce2_cluster_3_0_4_bin_enc,
                R.raw.gru_data_ce2_cluster_3_0_4_bin_iv
            ), rnnWeightProviderImpl.getEncryptedBinaryRes(ToothbrushModel.CONNECT_E2)
        )
    }

    @Test
    fun `get weight and iv for HILINK returns gru_data_ce2_cluster_3_0_4_bin_enc and gru_data_ce2_cluster_3_0_4_bin_iv`() {
        assertEquals(
            Pair(
                R.raw.gru_data_ce2_cluster_3_0_4_bin_enc,
                R.raw.gru_data_ce2_cluster_3_0_4_bin_iv
            ), rnnWeightProviderImpl.getEncryptedBinaryRes(ToothbrushModel.HILINK)
        )
    }

    @Test
    fun `get weight and iv for HUM_ELECTRIC returns gru_data_ce2_cluster_3_0_4_bin_enc and gru_data_ce2_cluster_3_0_4_bin_iv`() {
        assertEquals(
            Pair(
                R.raw.gru_data_ce2_cluster_3_0_4_bin_enc,
                R.raw.gru_data_ce2_cluster_3_0_4_bin_iv
            ), rnnWeightProviderImpl.getEncryptedBinaryRes(ToothbrushModel.HUM_ELECTRIC)
        )
    }

    @Test
    fun `get weight and iv for HUM_BATTERY returns gru_data_ce2_cluster_3_0_4_bin_enc and gru_data_ce2_cluster_3_0_4_bin_iv`() {
        assertEquals(
            Pair(
                R.raw.gru_data_ce2_cluster_3_0_4_bin_enc,
                R.raw.gru_data_ce2_cluster_3_0_4_bin_iv
            ), rnnWeightProviderImpl.getEncryptedBinaryRes(ToothbrushModel.HUM_BATTERY)
        )
    }

    @Test
    fun `get weight and iv for PQL returns gru_data_ce2_cluster_3_0_4_bin_enc and gru_data_ce2_cluster_3_0_4_bin_iv`() {
        assertEquals(
            Pair(
                R.raw.gru_data_ce2_cluster_3_0_4_bin_enc,
                R.raw.gru_data_ce2_cluster_3_0_4_bin_iv
            ), rnnWeightProviderImpl.getEncryptedBinaryRes(ToothbrushModel.PLAQLESS)
        )
    }

    @Test
    fun `get weight and iv for B1 returns gru_data_cb1_cluster_3_0_4_bin_enc and gru_data_cb1_cluster_3_0_4_bin_iv`() {
        assertEquals(
            Pair(
                R.raw.gru_data_cb1_cluster_3_0_4_bin_enc,
                R.raw.gru_data_cb1_cluster_3_0_4_bin_iv
            ), rnnWeightProviderImpl.getEncryptedBinaryRes(ToothbrushModel.CONNECT_B1)
        )
    }

    @Test
    fun `get weight and iv for GLINT returns gru_data_glint_cluster_3_0_4_bin_enc and gru_data_glint_cluster_3_0_4_bin_iv`() {
        assertEquals(
            Pair(
                R.raw.gru_data_glint_cluster_3_0_4_bin_enc,
                R.raw.gru_data_glint_cluster_3_0_4_bin_iv
            ), rnnWeightProviderImpl.getEncryptedBinaryRes(ToothbrushModel.GLINT)
        )
    }
}
