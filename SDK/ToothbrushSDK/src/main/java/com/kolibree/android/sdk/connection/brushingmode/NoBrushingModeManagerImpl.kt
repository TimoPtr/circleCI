/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeCustomizer
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettings
import com.kolibree.android.sdk.error.CommandNotSupportedException
import io.reactivex.Completable
import io.reactivex.Single
import org.threeten.bp.OffsetDateTime

/**
 * [BrushingModeManager] implementation for toothbrushes that don't have the feature:
 * Kolibree, Ara, E1, M1 and Plaqless
 */
internal class NoBrushingModeManagerImpl : BrushingModeManager {

    private val customizer = NoBrushingModeCustomizerImpl()

    override fun isAvailable() = false

    override fun availableBrushingModes(): Single<List<BrushingMode>> =
        Single.error(CommandNotSupportedException())

    override fun lastUpdateDate(): Single<OffsetDateTime> =
        Single.error(CommandNotSupportedException())

    override fun set(mode: BrushingMode) =
        Completable.error(CommandNotSupportedException())

    override fun getCurrent(): Single<BrushingMode> =
        Single.error(CommandNotSupportedException())

    override fun customize() = customizer
}

internal class NoBrushingModeCustomizerImpl : BrushingModeCustomizer {

    override fun setCustomBrushingModeSettings(brushingModeSettings: BrushingModeSettings) =
        Completable.error(CommandNotSupportedException())

    override fun getCustomBrushingModeSettings(): Single<BrushingModeSettings> =
        Single.error(CommandNotSupportedException())
}
