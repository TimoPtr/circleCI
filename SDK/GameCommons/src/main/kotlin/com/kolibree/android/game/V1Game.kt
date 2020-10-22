/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.game

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.game.commons.R

/**
 * Embedded games and activities enumeration.
 *
 * Some data is missing here, and there are activities that are not present in the app, but we
 * have to define them to be compatible with the iOS and Ubisoft created brushing sessions
 */
@Keep
@Deprecated("will be redone in the new Game API")
enum class V1Game(
    @StringRes val gameName: Int,
    @StringRes val description: Int,
    @ColorRes val mainColor: Int,
    @DrawableRes val thumbnail: Int,
    @ColorRes val textColor: Int
) {
    /**
     * Coach activity.
     */
    COACH(
        R.string.coach_game_name,
        R.string.coach_game_description,
        R.color.white,
        R.drawable.coach_launch,
        R.color.coachThumbnailText
    ),

    /**
     * Coach+ activity.
     */
    COACH_PLUS(
        R.string.coach_plus_game_name,
        R.string.coach_pluse_game_description,
        R.color.white,
        R.drawable.activity_cp,
        R.color.coachThumbnailText
    ),

    /**
     * Go Pirate game.
     */
    GO_PIRATE(
        R.string.pirate_game_name,
        R.string.pirate_game_description,
        R.color.green_pirate,
        R.drawable.pirate_launch,
        R.color.grey_dark
    ),

    /**
     * Smart brushing analyzer activity.
     */
    TEST_BRUSHING(
        R.string.test_brushing_screen_title,
        R.string.sba_game_description,
        R.color.sba_thumbnail_background,
        R.drawable.test_brushing_launch,
        R.color.coachThumbnailText
    ),

    /**
     * Rabbids Smart Brush game.
     */
    RABBIDS(R.string.rabbids_game_name, 0, 0, 0, 0),

    /**
     * Synchronized offline brushing session.
     */
    OFFLINE(R.string.offline_game_name, 0, 0, 0, 0),

    TEST_ANGLES(
        R.string.test_angles_activity_name,
        R.string.test_angles_activity_name,
        R.color.white,
        R.drawable.activity_test_angles,
        R.color.coachThumbnailText
    ),

    SPEED_CONTROL(
        R.string.speed_control_activity_name,
        R.string.speed_control_activity_name,
        R.color.white,
        R.drawable.activity_speed_control,
        R.color.coachThumbnailText
    );

    override fun toString(): String {
        return ("V1Game{" +
            "name=" +
            gameName +
            ", description=" +
            description +
            ", mainColor=" +
            mainColor +
            ", thumbnail=" +
            thumbnail +
            ", textColor=" +
            textColor +
            '}')
    }

    companion object {
        /**
         * Find a game by game enum
         *
         * @param game domain game enum
         * @return Game if found, null otherwise
         */
        @JvmStatic
        fun lookup(game: Game?): V1Game? {
            return when (game) {
                Game.GO_PIRATE -> GO_PIRATE
                Game.COACH -> COACH
                Game.TEST_BRUSHING -> TEST_BRUSHING
                Game.OFFLINE -> OFFLINE
                Game.COACH_PLUS -> COACH_PLUS
                Game.RABBIDS -> RABBIDS
                Game.SPEED_CONTROL -> SPEED_CONTROL
                Game.TEST_ANGLES -> TEST_ANGLES
                else -> null
            }
        }

        @JvmStatic
        fun lookup(serverName: String?): V1Game? = lookup(Game.lookup(serverName))
    }
}
