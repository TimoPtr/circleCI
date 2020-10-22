/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.retrofit

import com.kolibree.android.commons.models.StrippedMac
import java.lang.reflect.Type
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * Converter for @Path Types in retrofit interfaces
 */
internal class CustomRetrofitConverterFactory private constructor() : Converter.Factory() {
    /*
    If you update this class, add changes to PactBaseTest.CustomRetrofitConverterFactory
     */
    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return when (type) {
            StrippedMac::class.java -> Converter<Any, String> { (it as StrippedMac).value }
            else -> super.stringConverter(type, annotations, retrofit)
        }
    }

    companion object {
        @JvmStatic
        fun create(): CustomRetrofitConverterFactory = CustomRetrofitConverterFactory()
    }
}
