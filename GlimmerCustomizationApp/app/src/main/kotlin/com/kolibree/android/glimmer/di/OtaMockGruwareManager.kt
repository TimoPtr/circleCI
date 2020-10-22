/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.di

import android.content.Context
import com.google.gson.Gson
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.utils.assets.AssetLoader
import com.kolibree.sdkws.api.gruware.GruwareManager
import com.kolibree.sdkws.api.response.GruwareResponse
import io.reactivex.Single
import javax.inject.Inject

@Suppress("NestedBlockDepth")
class OtaMockGruwareManager @Inject constructor(
    private val context: Context
) : GruwareManager {

    @Suppress("TooGenericExceptionCaught")
    override fun getGruwareInfos(
        model: String?,
        hw: String?,
        serial: String?,
        firmwareVersion: String?
    ): Single<GruwareResponse> = Single.create { emitter ->
        try {
            val responseString = loadAssetForToothbrush(model, hw)
            val gruwareData = Gson().fromJson(responseString, GruwareResponse::class.java)
            if (!emitter.isDisposed) emitter.onSuccess(gruwareData)
        } catch (e: Throwable) {
            emitter.tryOnError(e)
        }
    }

    private fun loadAssetForToothbrush(model: String?, hw: String?): String {
        val jsonPath = jsonPathForToothbrush(model, hw)

        return AssetLoader(context).loadString("ota/$jsonPath/response_gruware.json")
    }

    private fun jsonPathForToothbrush(model: String?, hw: String?): String? {
        model?.let {
            val toothbrushModel = ToothbrushModel.getModelByInternalName(model) ?: return@let model

            when {
                toothbrushModel.isConnectE1 or toothbrushModel.isAra -> hw?.let {
                    val hwVersion = HardwareVersion(hw)

                    if (!hwVersion.isNewerOrSame(firstHwSupportingEncrypted)) {
                        return KLTB002_UNENCRYPTED_PATH
                    }
                }
                else -> {
                }
            }
        }

        return model
    }
}

@Suppress("MagicNumber")
private val firstHwSupportingEncrypted = HardwareVersion(2, 5)
private const val KLTB002_UNENCRYPTED_PATH = "kltb002_2.4"
