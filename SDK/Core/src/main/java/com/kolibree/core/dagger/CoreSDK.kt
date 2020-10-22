/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.core.dagger

import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.core.AlwaysScanBeforeReconnectStrategy
import com.kolibree.android.sdk.dagger.SdkComponent
import com.kolibree.android.translationssupport.TranslationsProvider
import com.kolibree.kml.Kml
import javax.inject.Inject

@Keep
class CoreSDK @Inject constructor() {
    init {
        // place internal SDK initializers here
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun init(
            context: Context,
            translationsProvider: TranslationsProvider? = null
        ): SdkComponent {
            Kml.init()

            return KolibreeAndroidSdk.init(
                context,
                translationsProvider,
                scanBeforeReconnectStrategy = AlwaysScanBeforeReconnectStrategy
            )
        }
    }
}
