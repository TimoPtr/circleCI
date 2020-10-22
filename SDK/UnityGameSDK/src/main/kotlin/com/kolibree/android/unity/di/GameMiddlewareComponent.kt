/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.di

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.dagger.SingleThreadSchedulerModule
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.gameprogress.domain.logic.GameProgressRepository
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.unity.GameMiddlewareFragment
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.core.avro.AvroFileUploader
import dagger.BindsInstance
import dagger.Component

@FragmentScope
@GameScope
@Component(modules = [
    GameMiddlewareModule::class,
    SingleThreadSchedulerModule::class
])
internal interface GameMiddlewareComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context,
            @BindsInstance kltbConnection: KLTBConnection,
            @BindsInstance kolibreeConnector: InternalKolibreeConnector,
            @BindsInstance gameProgressRepository: GameProgressRepository,
            @BindsInstance lifecycle: Lifecycle,
            @BindsInstance avroFileUploader: AvroFileUploader
        ): GameMiddlewareComponent
    }

    fun inject(dependencyProvider: GameMiddlewareFragment.DependencyProvider)
}
