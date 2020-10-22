package com.kolibree.android.pirate.controller

import androidx.annotation.VisibleForTesting
import com.kolibree.android.pirate.controller.kml.World1KmlController
import com.kolibree.android.pirate.controller.kml.World2KmlController
import com.kolibree.android.pirate.controller.kml.World3KmlController
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.kml.PirateHelper
import com.kolibree.kml.SupervisedBrushingAppContext12
import com.kolibree.kml.SupervisedBrushingAppContext16
import com.kolibree.kml.SupervisedBrushingAppContext8
import javax.inject.Inject
import javax.inject.Provider

internal class PirateControllerFactory @Inject constructor(
    private val checkupCalculator: CheckupCalculator,
    private val supervisedAppContext8Provider: Provider<SupervisedBrushingAppContext8>,
    private val supervisedAppContext12Provider: Provider<SupervisedBrushingAppContext12>,
    private val supervisedAppContext16Provider: Provider<SupervisedBrushingAppContext16>,
    private val pirateHelperProvider: Provider<PirateHelper>
) {

    fun getWorldController(
        isRightHand: Boolean,
        worldId: Int
    ): WorldController? = when (worldId) {
        WORLD_1_ID -> World1KmlController(
            isRightHand,
            checkupCalculator,
            supervisedAppContext8Provider.get(),
            pirateHelperProvider.get()
        )
        WORLD_2_ID -> World2KmlController(
            isRightHand,
            checkupCalculator,
            supervisedAppContext12Provider.get(),
            pirateHelperProvider.get()
        )
        WORLD_3_ID, WORLD_4_ID -> World3KmlController(
            isRightHand,
            checkupCalculator,
            supervisedAppContext16Provider.get(),
            pirateHelperProvider.get()
        )
        else -> null
    }
}

@VisibleForTesting
internal const val WORLD_1_ID = 0

@VisibleForTesting
internal const val WORLD_2_ID = 1

@VisibleForTesting
internal const val WORLD_3_ID = 2

@VisibleForTesting
internal const val WORLD_4_ID = 3
