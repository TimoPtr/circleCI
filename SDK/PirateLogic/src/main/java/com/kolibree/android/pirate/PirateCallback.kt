/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate

import androidx.annotation.Keep
import com.kolibree.android.app.unity.UnityCallback

@Keep
@Suppress("all")
interface PirateCallback : UnityCallback {

    abstract class Proxy<T>(impl: T) : UnityCallback.Proxy<T>(impl), PirateCallback

    fun goPirate_pirateExitCurrentLevel()

    fun goPirate_getRightHanded(): Boolean

    fun goPirate_getPlayerName(): String?

    fun goPirate_getBrushingTime(): Int

    fun goPirate_getGender(): Int

    fun goPirate_getRank(): Int

    fun goPirate_setRank(rank: Int)

    fun goPirate_getCurrentGold(): Int

    fun goPirate_getBrushingsCount(): Int

    fun goPirate_getLastWorldReached(): Int

    fun goPirate_setLastWorldReached(world: Int)

    fun goPirate_getLastLevelReached(): Int

    fun goPirate_setLastLevelReached(level: Int)

    fun goPirate_getLastLevelBrush(): Int

    fun goPirate_setLastLevelBrush(level: Int)

    fun goPirate_getLastShipBought(): Int

    fun goPirate_setLastShipBought(ship: Int)

    fun goPirate_getAvatarColor(): Int

    fun goPirate_setAvatarColor(color: Int)

    fun goPirate_getTreasures(): String

    fun goPirate_newTreasureFound(treasure: Int)

    fun goPirate_tutorialEnabled(): Boolean

    fun goPirate_setTutorialEnabled(enabled: Boolean)

    fun goPirate_hasSeenTrailer(): Boolean

    fun goPirate_setHasSeenTrailer(seen: Boolean)

    // ------------------------------------------------------------------------------------------- //
    // -------------- CALL FROM UNITY TO ANDROID : CALL AT THE END OF THE GAME ------------------- //
    // ------------------------------------------------------------------------------------------- //

    fun goPirate_hasSeenGameCompleteTrailer(): Boolean

    fun goPirate_setHasSeenGameCompleteTrailer(seen: Boolean)

    // ------------------------------------------------------------------------------------------- //
    // ------------------- CALL FROM UNITY TO ANDROID : DATA WRITING ----------------------------- //
    // ------------------------------------------------------------------------------------------- //

    fun goPirate_newGoldEarned(gold: Int)

    fun goPirate_brushingComplete(time: Int)

    fun goPirate_beginWriteTransaction()

    // ------------------------------------------------------------------------------------------- //
    // ------------------- CALL FROM UNITY TO ANDROID : GAME EVENT ------------------------------- //
    // ------------------------------------------------------------------------------------------- //

    fun goPirate_commitWriteTransaction()

    fun goPirate_levelShouldStart(worldId: Int): Boolean

    fun goPirate_levelWillRestart(worldId: Int)

    fun goPirate_sendEnterLevelNotification(worldId: Int)

    fun goPirate_prescribedZoneDidChange(worldId: Int, zoneId: Int)

    fun goPirate_shouldChangeLane()

    fun goPirate_pirateDidCrossFinishLine()

    fun goPirate_getToothbrushModel(): Int

    fun goPirate_gameDidFinish()
}
