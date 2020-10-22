/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrushupdate

import dagger.Binds
import dagger.Module

@Module
abstract class CheckOtaUpdatePrerequisitesModule {

    @Binds
    internal abstract fun bindsCheckOtaUpdatePrerequisitesUseCase(
        useCase: CheckOtaUpdatePrerequisitesUseCaseImpl
    ): CheckOtaUpdatePrerequisitesUseCase
}
