/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.sdk

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.core.AlwaysScanBeforeReconnectStrategy
import com.kolibree.android.sdk.core.ScanBeforeReconnectStrategy
import com.kolibree.android.sdk.dagger.DaggerSdkComponent
import com.kolibree.android.sdk.dagger.SdkComponent
import com.kolibree.android.translationssupport.Translations
import com.kolibree.android.translationssupport.TranslationsProvider
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android initializer for Kolibree's Android SDK
 *
 *
 * Created by miguelaragues on 29/9/17.
 */
@Keep
@SuppressLint("LogNotTimber")
class KolibreeAndroidSdk private constructor() {

    companion object {

        private const val OLD_DATABASE_NAME = "com.kolibree.android.sdk.db"

        private val initialized = AtomicBoolean()

        @VisibleForTesting
        @Volatile
        private var sdkComponent: SdkComponent? = null

        @JvmOverloads
        @JvmStatic
        @Keep
        fun init(
            context: Context,
            translationsProvider: TranslationsProvider? = null,
            scanBeforeReconnectStrategy: ScanBeforeReconnectStrategy = AlwaysScanBeforeReconnectStrategy
        ): SdkComponent {
            if (initialized.getAndSet(true)) {
                return sdkComponent
                    ?: throw IllegalStateException("SdkComponent should be null at this point")
            }

            val localSdkComponent = DaggerSdkComponent.builder()
                .context(context.applicationContext)
                .scanBeforeReconnectStrategy(scanBeforeReconnectStrategy)
                .build()

            sdkComponent = localSdkComponent

            Translations.init(context, translationsProvider)

            removeKnownToothbrushDatabase(context)

            return localSdkComponent
        }

        private fun removeKnownToothbrushDatabase(context: Context) {
            context.deleteDatabase(OLD_DATABASE_NAME)
        }

        /** Do NOT invoke from production code  */
        @VisibleForTesting
        @JvmStatic
        fun setSdkComponent(newSdkComponent: SdkComponent) {
            sdkComponent = newSdkComponent

            initialized.set(sdkComponent != null)
        }

        /**
         * Returns a SdkComponent that will give access to the dependencies provided by the Sdk graph
         *
         * @return an application context aware SdkComponent
         */
        @JvmStatic
        fun getSdkComponent(): SdkComponent {
            check(initialized.get()) { "Make sure you initialized the SDK in onCreate" }

            return checkNotNull(sdkComponent)
        }
    }
}
