/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import androidx.annotation.Keep
import dagger.Binds
import dagger.Module
import io.reactivex.Completable
import javax.inject.Inject

/** BrushingModeSetting allows the client to read and set brushing mode by profile Id */
@Keep
interface BrushingModeSetting {
    fun getBrushingMode(profileId: Long): ProfileBrushingMode?

    fun setBrushingMode(profileId: Long, brushingMode: BrushingMode): Completable
}

internal class BrushingModeSettingImpl @Inject constructor(
    private val confirmBrushingModeUseCase: ConfirmBrushingModeUseCase,
    private val brushingModeRepository: BrushingModeRepository
) : BrushingModeSetting {

    override fun getBrushingMode(profileId: Long): ProfileBrushingMode? {
            return brushingModeRepository.getForProfile(profileId)
        }

    override fun setBrushingMode(profileId: Long, brushingMode: BrushingMode): Completable {
        return confirmBrushingModeUseCase.confirmBrushingModeCompletable(profileId, brushingMode)
    }
}

@Module(includes = [ConfirmBrushingModeModule::class])
abstract class BrushingModeSettingModule {

    @Binds
    internal abstract fun bindConfirmBrushingModeUseCase(
        impl: BrushingModeSettingImpl
    ): BrushingModeSetting
}
