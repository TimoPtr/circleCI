/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity

import android.annotation.SuppressLint
import android.os.Bundle
import com.kolibree.android.app.unity.UnityGame
import com.kolibree.android.failearly.FailEarly

/**
 * Fragment hosting the real unity game
 */
@SuppressLint("ExperimentalClassUse")
internal class UnityGameFragment : GameMiddlewareFragment() {
    override fun game(): UnityGame = middlewareGame().toUnityGame()

    private fun middlewareGame(): MiddlewareUnityGame {
        return (arguments?.getSerializable(EXTRA_GAME)
            ?: FailEarly.fail("Expecting game in $arguments")) as MiddlewareUnityGame
    }

    companion object {
        fun create(game: MiddlewareUnityGame): UnityGameFragment {
            return UnityGameFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_GAME, game)
                }
            }
        }
    }
}

private const val EXTRA_GAME = "extra_game"
