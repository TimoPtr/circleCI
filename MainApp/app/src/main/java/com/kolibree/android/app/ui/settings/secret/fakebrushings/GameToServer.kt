/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.fakebrushings

import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.game.Game

internal enum class GameToServer(private val displayName: String, val serverName: String) {
    RABBIDS(serverName = GameApiConstants.GAME_RABBIDS, displayName = Game.RABBIDS.toString()),
    GO_PIRATE(
        serverName = GameApiConstants.GAME_GO_PIRATE,
        displayName = Game.GO_PIRATE.toString()
    ),
    COACH(serverName = GameApiConstants.GAME_COACH, displayName = Game.COACH.toString()),
    COACH_MANUAL(
        serverName = GameApiConstants.GAME_COACH_MANUAL,
        displayName = "MANUAL COACH"
    ),
    COACH_PLUS(
        serverName = GameApiConstants.GAME_COACH_PLUS,
        displayName = Game.COACH_PLUS.toString()
    ),
    OFFLINE(serverName = GameApiConstants.GAME_OFFLINE, displayName = Game.OFFLINE.toString()),
    SBA(serverName = GameApiConstants.GAME_SBA, displayName = Game.TEST_BRUSHING.toString());

    fun toDisplayString(): String {
        return "$displayName ($serverName)"
    }
}
