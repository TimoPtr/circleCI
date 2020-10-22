/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.content.ClipboardManager
import android.content.Context
import com.kolibree.android.appversion.di.AppVersionModule
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.error.KolibreeRxErrorHandler
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.location.LocationStatusImpl
import com.kolibree.android.utils.CopyToClipboardUseCase
import com.kolibree.android.utils.CopyToClipboardUseCaseImpl
import com.kolibree.android.utils.PhoneNumberChecker
import com.kolibree.android.utils.PhoneNumberCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.functions.Consumer
import org.threeten.bp.Clock

@Module(includes = [CommonsAndroidCoreModule::class, CommonsAndroidLocationModule::class])
interface CommonsAndroidModule

@Module(includes = [ApplicationLifecycleModule::class, AppVersionModule::class])
abstract class CommonsAndroidCoreModule {

    @Binds
    internal abstract fun bindPhoneNumberChecker(phoneNumberChecker: PhoneNumberCheckerImpl): PhoneNumberChecker

    @Binds
    abstract fun bindKolibreeRxErrorHandler(handler: KolibreeRxErrorHandler): Consumer<Throwable>

    @Binds
    internal abstract fun bindsClipboardUseCase(impl: CopyToClipboardUseCaseImpl): CopyToClipboardUseCase

    internal companion object {

        @Provides
        fun provideUTCClock(): Clock = TrustedClock.utcClock

        @Provides
        fun providesClipboardManager(context: Context): ClipboardManager {
            return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        }
    }
}

@Module
internal abstract class CommonsAndroidLocationModule {
    @Binds
    internal abstract fun bindsLocationStatus(implementation: LocationStatusImpl): LocationStatus
}
