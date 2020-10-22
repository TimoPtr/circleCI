package com.kolibree.android.offlinebrushings.sync

import com.kolibree.android.app.dagger.AppScope
import dagger.Binds
import dagger.Module

@Module
internal abstract class LastSyncModule {

    @Binds
    abstract fun providesLastSyncObservable(impl: LastSyncObservableInternal): LastSyncObservable

    @Binds
    internal abstract fun bindsLastSyncDateFormatter(impl: LastSyncDateFormatterImpl): LastSyncDateFormatter

    @Binds
    internal abstract fun bindsLastSyncProvider(impl: LastSyncProviderImpl): LastSyncProvider

    @Binds
    @AppScope
    internal abstract fun bindsLastSyncObservableInternal(impl: LastSyncObservableImpl): LastSyncObservableInternal
}
