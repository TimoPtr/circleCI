/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.di

import android.content.Context
import com.kolibree.android.app.dagger.SingleThreadSchedulerModule
import com.kolibree.android.sdk.connection.brushingmode.BrushingProgramModule
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.KLTBConnectionImpl
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.di.KLTBConnectionScope
import dagger.BindsInstance
import dagger.Component

@Component(modules = [BrushingProgramModule::class, SingleThreadSchedulerModule::class])
@KLTBConnectionScope
internal interface ConnectionComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context,
            @BindsInstance connection: InternalKLTBConnection
        ): ConnectionComponent
    }

    fun inject(connection: KLTBConnectionImpl)
}
