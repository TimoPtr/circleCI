package com.kolibree.android.synchronizator

import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.synchronization.SynchronizationStateUseCase
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntityDao
import com.kolibree.android.synchronizator.data.database.SynchronizatorRoomModule
import com.kolibree.android.synchronizator.models.BundleCreatorSet
import com.kolibree.android.synchronizator.network.SynchronizatorNetworkModule
import com.kolibree.android.synchronizator.usecases.BackendSynchronizationStateUseCase
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds

@Module(includes = [SynchronizatorNetworkModule::class, SynchronizatorRoomModule::class])
abstract class SynchronizatorModule {
    @Binds
    internal abstract fun bindsSynchronizator(impl: SynchronizatorOrchestrator): Synchronizator

    @Binds
    internal abstract fun bindsBundleConsumableVisitor(visitor: BundleConsumableVisitorImpl): BundleConsumableVisitor

    @Binds
    internal abstract fun bindsBundleConsumableBuilder(visitor: BundleConsumableBuilderImpl): BundleConsumableBuilder

    @Binds
    internal abstract fun bindsQueueOperationExecutor(impl: FifoQueueOperationExecutor): QueueOperationExecutor

    @Binds
    internal abstract fun bindsOngoingSynchronizationUseCase(impl: BackendSynchronizationStateUseCase): SynchronizationStateUseCase

    @Binds
    internal abstract fun bindsSynchronizeOnNetworkAvailableUseCase(impl: SynchronizeOnNetworkAvailableUseCaseImpl):
        SynchronizeOnNetworkAvailableUseCase

    @Binds
    @IntoSet
    internal abstract fun bindsSynchronizatorTruncable(truncable: SynchronizableTrackingEntityDao): Truncable

    @ContributesAndroidInjector
    internal abstract fun bindRunSynchronizeOperationJobService(): RunSynchronizeOperationJobService

    /*
    Support for apps without BundleCreators
    */
    @Multibinds
    abstract fun bundleCreatorSet(): BundleCreatorSet
}
