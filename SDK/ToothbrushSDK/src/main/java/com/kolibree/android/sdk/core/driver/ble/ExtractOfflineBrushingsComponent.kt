/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import android.content.Context
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.core.driver.ble.fileservice.OfflineBrushingExtractorModule
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.di.KLTBConnectionScope
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileType
import dagger.BindsInstance
import dagger.Component

@Component(modules = [OfflineBrushingExtractorModule::class])
@KLTBConnectionScope
internal interface ExtractOfflineBrushingsComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context,
            @BindsInstance klNordicBleManager: KLNordicBleManager,
            @BindsInstance fileType: FileType,
            @BindsInstance toothbrushModel: ToothbrushModel
        ): ExtractOfflineBrushingsComponent
    }

    fun inject(connection: KolibreeBleDriver)
}
