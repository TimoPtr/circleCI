/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.test

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.synchronizator.BundleConsumableBuilder
import com.kolibree.android.synchronizator.QueueOperationExecutor
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntityDao
import com.kolibree.android.synchronizator.data.database.SynchronizatorDatabase
import com.kolibree.android.synchronizator.network.SynchronizeAccountApi
import com.kolibree.android.synchronizator.operations.CreateOrEditOperationIntegrationTest
import com.kolibree.android.synchronizator.operations.QueueOperation
import com.kolibree.android.synchronizator.operations.SynchronizeQueueOperationIntegrationTest
import com.nhaarman.mockitokotlin2.mock
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.threeten.bp.Duration

@Component(modules = [SynchronizatorTestModule::class, SynchronizatorTestRoomModule::class])
@AppScope
internal interface SynchronizatorTestComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context
        ): SynchronizatorTestComponent
    }

    fun inject(test: SynchronizeQueueOperationIntegrationTest)
    fun inject(test: CreateOrEditOperationIntegrationTest)
}

@Module
internal object SynchronizatorTestModule {

    @Provides
    @AppScope
    fun providesBundleConsumableBuilder(): BundleConsumableBuilder = mock()

    @Provides
    @AppScope
    fun providesQueueOperationExecutor(): QueueOperationExecutor = TestQueueOperationExecutor()

    @Provides
    @AppScope
    internal fun providesAccountApiService(): SynchronizeAccountApi = mock()
}

@Module
internal object SynchronizatorTestRoomModule {

    @Provides
    @AppScope
    internal fun providesDatabase(context: Context): SynchronizatorDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            SynchronizatorDatabase::class.java
        )
            .build()
    }

    @Provides
    internal fun providesSynchronizatorEntityDao(database: SynchronizatorDatabase): SynchronizableTrackingEntityDao {
        return database.synchronizatorEntityDao()
    }
}

internal open class TestQueueOperationExecutor : QueueOperationExecutor {
    private var dryRun = false

    private val _operations = mutableListOf<QueueOperation>()

    fun operations(): List<QueueOperation> = _operations.toList()

    fun enableDryRun() {
        dryRun = true
    }

    override fun enqueue(queueOperation: QueueOperation, initialDelay: Duration) {
        _operations.add(queueOperation)

        if (!dryRun)
            queueOperation.run()
    }

    override fun cancelOperations() {
        TODO("Not yet implemented")
    }
}
