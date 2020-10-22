/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.game

import androidx.annotation.Keep
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.GameApiConstants.GAME_COACH
import com.kolibree.android.commons.GameApiConstants.GAME_COACH_MANUAL
import com.kolibree.android.commons.GameApiConstants.GAME_COACH_PLUS
import com.kolibree.android.commons.GameApiConstants.GAME_GO_PIRATE
import com.kolibree.android.commons.GameApiConstants.GAME_OFFLINE
import com.kolibree.android.commons.GameApiConstants.GAME_RABBIDS
import com.kolibree.android.commons.GameApiConstants.GAME_SBA
import java.util.Locale

@Keep
enum class Game {

    COACH,

    /**
     * Coach+ activity.
     */
    COACH_PLUS,

    /**
     * Go Pirate game.
     */
    GO_PIRATE,

    /**
     * Test brushing activity.
     */
    TEST_BRUSHING,

    /**
     * Rabbids Smart Brush game.
     */
    RABBIDS,

    /**
     * Synchronized offline brushing session.
     */
    OFFLINE,

    TEST_ANGLES,

    SPEED_CONTROL;

    companion object {
        /**
         * Find a game by server identifier.
         *
         * @param serverName non null server name [String]
         * @return Game if found, null otherwise
         */
        @JvmStatic
        fun lookup(serverName: String?): Game? =
            if (serverName == null) null
            else when (serverName.toLowerCase(Locale.getDefault())) {
                GAME_GO_PIRATE -> GO_PIRATE
                GAME_COACH -> COACH
                GAME_COACH_MANUAL -> COACH
                GAME_SBA -> TEST_BRUSHING
                GAME_OFFLINE -> OFFLINE
                GAME_COACH_PLUS -> COACH_PLUS
                GAME_RABBIDS -> RABBIDS
                else -> null
            }

        /**
         * Convert a game to server identifier.
         *
         * TEST_ANGLES and SPEED_CONTROL don't create a brushing on backend, thus there's no
         * constant for them
         *
         * @return GameApiConstants if found, null otherwise
         */
        @VisibleForApp
        fun Game.toServerName(): String? {
            return when (this) {
                GO_PIRATE -> GAME_GO_PIRATE
                COACH -> GAME_COACH
                COACH_PLUS -> GAME_COACH_PLUS
                TEST_BRUSHING -> GAME_SBA
                OFFLINE -> GAME_OFFLINE
                RABBIDS -> GAME_RABBIDS
                TEST_ANGLES -> null
                SPEED_CONTROL -> null
            }
        }
    }
}
