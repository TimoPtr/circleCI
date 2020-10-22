/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.activity.mvi.MVIUnityPlayerLifecycleActivity
import com.kolibree.android.app.ui.fragment.BaseUnityGameFragment
import com.kolibree.android.app.unity.UnityGameResult
import com.kolibree.android.failearly.FailEarly
import javax.inject.Inject
import timber.log.Timber

/**
 * Host activity for Unity games powered by GameMiddleware. Can also host legacy games, like Pirate.
 *
 * @see [BaseUnityGameFragment]
 * @see [GameMiddlewareFragment]
 *
 * TODO Migrate to proper MVI structure
 */
@VisibleForApp
class GameMiddlewareActivity :
    MVIUnityPlayerLifecycleActivity<
        GameMiddlewareViewState,
        GameMiddlewareViewModel.Factory,
        GameMiddlewareViewModel
        >() {

    @Inject
    internal lateinit var gameSplashResourcesProvider: GameSplashResourcesProvider

    override fun onUnityGameFinished(result: UnityGameResult<*>) {
        Timber.d("Game finished. Result: $result")
    }

    override fun getViewModelClass(): Class<GameMiddlewareViewModel> =
        GameMiddlewareViewModel::class.java

    override fun splashDrawable(): Int = gameSplashResourcesProvider.background(middlewareGame())

    override fun splashText(): Int = gameSplashResourcesProvider.loadingText()

    private fun middlewareGame(): MiddlewareUnityGame {
        return (intent.getSerializableExtra(EXTRA_GAME)
            ?: FailEarly.fail("Expecting a MiddlewareUnityGame in ${intent.extras}")) as MiddlewareUnityGame
    }

    override fun showSomethingWentWrong(actionAfterAccept: () -> Unit) {
        /*
        Should be improved with a proper dialog

        We receive multiple showSomethingWentWrong calls when download is wrong
         */
        Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()

        Handler(Looper.getMainLooper()).postDelayed(
            { actionAfterAccept() },
            DELAY_AFTER_SOMETHING_WENT_WRONG
        )
    }

    override fun onBackPressed() {
        ifUnityGameIsOn(
            execute = { finishOngoingGame() },
            otherwise = { restoreLauncherTask() }
        )
    }

    private fun finishOngoingGame() {
        if (supportFragmentManager.fragments.isNullOrEmpty()) return

        val topFragment = supportFragmentManager.fragments[0]
        (topFragment as? BaseUnityGameFragment)?.let {
            finishUnityGame(it, UnityGameResult<Void>(it.game(), success = false))
        }
    }

    override fun allowOnBackPressedOverride() = true

    override fun unityGameFragment(): BaseUnityGameFragment =
        UnityGameFragment.create(middlewareGame())

    @VisibleForApp
    companion object {

        /**
         * Starts [GameMiddlewareActivity] with [game]
         *
         * @param game the [MiddlewareUnityGame] to launch
         * @param activityAfterGameFinish activity's class to open when finishing the game
         */
        @VisibleForApp
        fun <T : AppCompatActivity> Activity.launchUnityGame(
            game: MiddlewareUnityGame,
            activityAfterGameFinish: Class<T>
        ) {
            val intent = createGameIntent(
                hostActivity = GameMiddlewareActivity::class.java,
                activityAfterGameFinish = activityAfterGameFinish
            ).apply {
                putExtra(EXTRA_GAME, game)
            }

            startActivity(intent)
        }
    }
}

private const val EXTRA_GAME = "extra_game"

private const val DELAY_AFTER_SOMETHING_WENT_WRONG = 2000L
