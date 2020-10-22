package com.kolibree.android.sdk.util

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.HUM_BATTERY
import com.kolibree.android.commons.ToothbrushModel.HUM_ELECTRIC
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sdk.R
import com.kolibree.crypto.KolibreeGuard
import com.kolibree.kml.ShortVector

@Keep
interface RnnWeightProvider {
    fun getRnnWeight(): ShortVector
}

internal class RnnWeightProviderImpl constructor(
    private val context: Context,
    private val kolibreeGuard: KolibreeGuard,
    private val toothbrushModel: ToothbrushModel
) : RnnWeightProvider {

    override fun getRnnWeight(): ShortVector {
        val key = kolibreeGuard.revealFromString(context, R.string.rnn_key, R.string.rnn_iv)
        val (encryptedFileRes, ivFileRes) = getEncryptedBinaryRes(toothbrushModel)

        return ShortVector(
            kolibreeGuard.revealFromRaw(context, encryptedFileRes, ivFileRes, key)
                .map { it.toShort() })
    }

    /**
     * Pair<EncryptedFile, IvFile>
     */
    @VisibleForTesting
    fun getEncryptedBinaryRes(model: ToothbrushModel): Pair<Int, Int> = when (model) {
        // gru_data_ce1_cluster_3.0.3
        CONNECT_E1,
        ARA -> Pair(
            R.raw.gru_data_ce1_cluster_3_0_3_bin_enc,
            R.raw.gru_data_ce1_cluster_3_0_3_bin_iv
        )
        // gru_data_cm1_cluster_3_0_3
        CONNECT_M1 -> Pair(
            R.raw.gru_data_cm1_3_0_3_bin_enc,
            R.raw.gru_data_cm1_3_0_3_bin_iv
        )
        // gru_data_cb1_cluster_3_0_4
        CONNECT_B1 -> Pair(
            R.raw.gru_data_cb1_cluster_3_0_4_bin_enc,
            R.raw.gru_data_cb1_cluster_3_0_4_bin_iv

        )
        // gru_data_ce2_cluster_3_0_4
        CONNECT_E2,
        HILINK,
        HUM_ELECTRIC,
        HUM_BATTERY, //  https://kolibree.atlassian.net/browse/KLTB002-9032
        PLAQLESS //  https://kolibree.atlassian.net/browse/KLTB002-9033
        -> Pair(
            R.raw.gru_data_ce2_cluster_3_0_4_bin_enc,
            R.raw.gru_data_ce2_cluster_3_0_4_bin_iv
        )
        // gru_data_glint_cluster_3_0_4
        GLINT -> Pair(
            R.raw.gru_data_glint_cluster_3_0_4_bin_enc,
            R.raw.gru_data_glint_cluster_3_0_4_bin_iv
        )
    }
}

internal class RnnWeightNotAvailableException : Exception()
