/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb003

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.plaqless.ToothbrushWithDspUpdater
import dagger.Module
import dagger.Provides

@Module
internal object ToothbrushUpdaterModule {
    @Provides
    fun providesKLTB003ToothbrushUpdater(
        model: ToothbrushModel,
        toothbrushWithDspUpdater: ToothbrushWithDspUpdater,
        toothbrushDfuUpdater: ToothbrushDfuUpdater
    ): KLTB003ToothbrushUpdater {
        return if (model.hasDsp) {
            toothbrushWithDspUpdater
        } else {
            toothbrushDfuUpdater
        }
    }
}
