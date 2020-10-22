/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.app.ui.settings.notifications.NotificationsBindingSharedModule
import com.kolibree.android.app.ui.settings.notifications.SystemNotificationsEnabledUseCase
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        NotificationsBindingSharedModule::class,
        EspressoSystemNotificationsEnabledModule::class
    ]
)
abstract class EspressoNotificationsBindingModule

@Module
internal object EspressoSystemNotificationsEnabledModule {

    val systemNotificationsEnabledMock: SystemNotificationsEnabledUseCase = mock()

    @Provides
    internal fun providesSystemNotificationsEnabledUseCase(): SystemNotificationsEnabledUseCase =
        systemNotificationsEnabledMock
}
