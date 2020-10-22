/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.di

import androidx.annotation.VisibleForTesting
import com.kolibree.android.offlinebrushings.ExtractOfflineBrushingsUseCase
import com.kolibree.android.offlinebrushings.ExtractOfflineBrushingsUseCaseImpl
import com.kolibree.android.offlinebrushings.OfflineBrushingsRetrieverUseCase
import com.kolibree.android.offlinebrushings.OfflineBrushingsRetrieverUseCaseImpl
import com.kolibree.android.offlinebrushings.retriever.OfflineBrushingRetrieverMonitor
import com.kolibree.android.offlinebrushings.retriever.OfflineExtractionProgressConsumer
import com.kolibree.android.offlinebrushings.retriever.OfflineExtractionProgressPublisher
import com.kolibree.android.offlinebrushings.retriever.OfflineRetrieveStatusPublisherImpl
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleObserver
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
@VisibleForTesting
abstract class ExtractOfflineBrushingsModule {
    @Binds
    @IntoSet
    internal abstract fun bindsOfflineBrushingRetriever(
        impl: OfflineBrushingRetrieverMonitor
    ): ApplicationLifecycleObserver

    @Binds
    internal abstract fun bindsOfflineExtractionProgressPublisher(
        impl: OfflineRetrieveStatusPublisherImpl
    ): OfflineExtractionProgressPublisher

    @Binds
    internal abstract fun bindsOfflineExtractionProgressConsumer(
        impl: OfflineRetrieveStatusPublisherImpl
    ): OfflineExtractionProgressConsumer

    @Binds
    internal abstract fun bindsExtractOfflineBrushingsUseCase(impl: ExtractOfflineBrushingsUseCaseImpl):
        ExtractOfflineBrushingsUseCase

    @Binds
    internal abstract fun bindsOfflineBrushingsRetrieverUseCase(impl: OfflineBrushingsRetrieverUseCaseImpl):
        OfflineBrushingsRetrieverUseCase
}
