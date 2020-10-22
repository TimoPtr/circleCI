/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.activity

import android.content.ComponentCallbacks2
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleRegistry
import com.kolibree.android.app.lifecycle.LifecycleMonitor
import com.kolibree.android.app.loader.GameService
import com.kolibree.android.app.loader.GameServiceLoaderCallback
import com.kolibree.android.app.ui.fragment.BaseUnityGameFragment
import com.kolibree.android.app.unity.KolibreeUnityPlayer
import com.kolibree.android.app.unity.UnityGame
import com.kolibree.android.app.unity.UnityGameModule
import com.kolibree.android.app.unity.UnityGameResult
import com.kolibree.android.extensions.hasFlags
import com.kolibree.android.lifecycle.InternalLifecycle
import com.kolibree.android.sdk.core.BackgroundJobManager
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.game.R
import com.unity3d.player.UnityPlayer
import javax.inject.Inject
import javax.inject.Named
import timber.log.Timber

/**
 * Base class for activities responsible for launching Unity-based games.
 *
 * It hosts [UnityPlayer] instance and manages its lifecycle. Also provides methods to start and finish the game.
 *
 * Because of Unity platform limitations, [UnityPlayer] object has to be added to Activity's layout.
 * To do that please use [UnityPlayerLifecycleActivity.gameFragmentContainer] method.
 *
 * TODO this may be also good place to execute other, non-Unity games, based on fragments.
 * TODO: this became quite a large piece, try to find a way to decrease its size
 *
 * @author Kornel, lookashc
 */
@Suppress("TooManyFunctions", "LargeClass")
@Deprecated("Use MVIUnityPlayerLifecycleActivity")
abstract class UnityPlayerLifecycleActivity(
    /**
     * If true - activity will be serving as a one-time host for a single game (like with old Pirate)
     * If false - activity will be working as a reusable host (similar to Main activity)
     */
    private val oneTimeUse: Boolean = false
) : KolibreeServiceActivity(), InternalLifecycle {

    private val lifecycleMonitor = LifecycleMonitor()

    private val transitionAnimator = TransitionAnimator()

    @JvmField
    @field:[Inject Named(UnityGameModule.ATTACH_UNITY_PLAYER_TO_VIEW)]
    var attachUnityPlayerToView: Boolean = true

    /*
     * IMPORTANT: don't change the name of this variable; referenced from native code
     */
    @Keep
    private var mUnityPlayer: KolibreeUnityPlayer? = null

    val player: KolibreeUnityPlayer?
        get() = mUnityPlayer

    @Inject
    lateinit var gameService: GameService

    @Inject
    lateinit var backgroundJobManager: Set<@JvmSuppressWildcards BackgroundJobManager>

    /**
     * Returns reference to layout which will host Unity game fragments.
     * This view will be animated while showing/hiding Unity game.
     */
    abstract fun gameFragmentContainer(): ViewGroup

    /**
     * Returns reference to all views that are not related to hosting Unity game fragments.
     * Those views will be animated while showing/hiding Unity game.
     */
    abstract fun nonGameViews(): List<View>

    // If set to true, Unity player will tear down itself during the next onPause().
    // It will also kill the host process - as there is no other way to prevent bugs
    // in Unity player when new activity with the player is launched.
    private var tearDownPlayerInOnPause = false

    /**
     * Delegate of the [onCreate] method
     * @param savedInstanceState previous state
     */
    @CallSuper
    override fun onCreateInternal(savedInstanceState: Bundle?) {
        Timber.tag(Companion.TAG_LIFECYCLE).v("%s - onCreateInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.CREATED)
    }

    /**
     * Delegate of the [onStart] method
     */
    @CallSuper
    override fun onStartInternal() {
        Timber.tag(Companion.TAG_LIFECYCLE).v("%s - onStartInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.STARTED)
    }

    /**
     * Delegate of the [onResume] method
     */
    @CallSuper
    override fun onResumeInternal() {
        Timber.tag(Companion.TAG_LIFECYCLE).v("%s - onResumeInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.RESUMED)
    }

    /**
     * Delegate of the [onPause] method
     */
    @CallSuper
    override fun onPauseInternal() {
        Timber.tag(Companion.TAG_LIFECYCLE).v("%s - onPauseInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.STARTED)
    }

    /**
     * Delegate of the [onStop] method
     */
    @CallSuper
    override fun onStopInternal() {
        Timber.tag(Companion.TAG_LIFECYCLE).v("%s - onStopInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.CREATED)
    }

    /**
     * Delegate of the [onDestroy] method
     */
    @CallSuper
    override fun onDestroyInternal() {
        Timber.tag(Companion.TAG_LIFECYCLE).v("%s - onDestroyInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.DESTROYED)
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // note: ContextWrapper is needed to prevent UnityPlayer
        // of applying immersive mode (fullscreen)
        if (attachUnityPlayerToView) {
            KolibreeUnityPlayer.loaderCallback = GameServiceLoaderCallback(gameService)
            mUnityPlayer = createNewPlayerInstance(ContextWrapper(this))
            UnityPlayer.currentActivity = this
        }
        onCreateInternal(savedInstanceState)
    }

    final override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mUnityPlayer?.let {
            gameFragmentContainer().addView(it)
            it.requestFocus()
        }
    }

    final override fun onStart() {
        super.onStart()
        ifUnityGameIsOn(
            execute = {
                val gameRunsInLandscape =
                    (supportFragmentManager.fragments.firstOrNull { fragment ->
                        fragment is BaseUnityGameFragment
                    } as? BaseUnityGameFragment)?.game()?.runsInLandscape == true
                requestLandscapeOrientation(gameRunsInLandscape)
            },
            otherwise = { onStartInternal() }
        )
    }

    final override fun onResume() {
        super.onResume()
        resumePlayer()
        ifUnityGameIsOn(
            execute = { /* reserved */ },
            otherwise = { onResumeInternal() }
        )
    }

    final override fun onPause() {
        ifUnityGameIsOn(
            execute = { /* reserved */ },
            otherwise = { onPauseInternal() }
        )
        super.onPause()
        pausePlayer()
        if (tearDownPlayerInOnPause) {
            destroyPlayer()
        }
    }

    final override fun onStop() {
        ifUnityGameIsOn(
            execute = { /* reserved */ },
            otherwise = { onStopInternal() }
        )
        super.onStop()
    }

    final override fun onDestroy() {
        onDestroyInternal()
        super.onDestroy()
        UnityPlayer.currentActivity = null
        destroyPlayer()
    }

    // Low Memory Unity
    final override fun onLowMemory() {
        super.onLowMemory()
        mUnityPlayer?.lowMemory()
    }

    // Trim Memory Unity
    final override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            mUnityPlayer?.lowMemory()
        }
    }

    // This ensures the layout will be correct.
    final override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mUnityPlayer?.configurationChanged(newConfig)
    }

    // Notify Unity of the focus change.
    final override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mUnityPlayer?.windowFocusChanged(hasFocus)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent)
    }

    final override fun finish() {
        tearDownUnityPlayer()
        super.finish()
    }

    override fun finishAndRemoveTask() {
        tearDownUnityPlayer()
        super.finishAndRemoveTask()
    }

    final override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        tearDownUnityPlayerIfNeeded(intent)
    }

    final override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        tearDownUnityPlayerIfNeeded(intent)
    }

    final override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        tearDownUnityPlayerIfNeeded(intent)
    }

    final override fun checkCallingOrSelfPermission(permission: String): Int {
        /*
        UGLY HACK!!

            Always report to Unity that we have the permission. For now we don't seem to use it so it's not
            a problem, but if some edge case does use it, the app will crash.

            Or if Unity changes something in their side, we can also have problems.

            This was requested here https://jira.kolibree.com/browse/KLTB002-2131

            See https://answers.unity.com/answers/1472450/view.html for explanation
            */
        return PackageManager.PERMISSION_GRANTED
    }

    final override fun dispatchKeyEvent(event: KeyEvent) =
        if (event.action == KeyEvent.ACTION_MULTIPLE)
            ifUnityGameIsOn(
                execute = { player -> player.dispatchKeyEvent(event) },
                otherwise = { super.dispatchKeyEvent(event) }
            )
        else super.dispatchKeyEvent(event)

    final override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KEYCODE_BACK && allowOnBackPressedOverride()) {
            onBackPressed()
            true
        } else {
            ifUnityGameIsOn(
                execute = { player -> player.onKeyUp(keyCode, event) },
                otherwise = { super.onKeyUp(keyCode, event) }
            )
        }
    }

    protected open fun allowOnBackPressedOverride() = false

    final override fun onKeyDown(keyCode: Int, event: KeyEvent) = ifUnityGameIsOn(
        execute = { player -> player.onKeyDown(keyCode, event) },
        otherwise = { super.onKeyDown(keyCode, event) }
    )

    final override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent) =
        ifUnityGameIsOn(
            execute = { player -> player.onKeyMultiple(keyCode, repeatCount, event) },
            otherwise = { super.onKeyMultiple(keyCode, repeatCount, event) }
        )

    final override fun onKeyLongPress(keyCode: Int, event: KeyEvent) = ifUnityGameIsOn(
        execute = { player -> player.onKeyLongPress(keyCode, event) },
        otherwise = { super.onKeyLongPress(keyCode, event) }
    )

    final override fun onTouchEvent(event: MotionEvent) = ifUnityGameIsOn(
        execute = { player -> player.onTouchEvent(event) },
        otherwise = { super.onTouchEvent(event) }
    )

    final override fun onGenericMotionEvent(event: MotionEvent) = ifUnityGameIsOn(
        execute = { player -> player.onGenericMotionEvent(event) },
        otherwise = { super.onGenericMotionEvent(event) }
    )

    protected open fun createNewPlayerInstance(contextWrapper: ContextWrapper): KolibreeUnityPlayer {
        return KolibreeUnityPlayer(contextWrapper)
    }

    protected fun startUnityGame(fragment: BaseUnityGameFragment) {
        ifUnityGameIsOn(
            execute = { Timber.w("Cannot launch Unity game from $fragment, another game is already in progress!") },
            otherwise = {
                if (!UnityGame.isSupportedBySystem()) {
                    Timber.e("Cannot launch Unity game for this system")
                    return@ifUnityGameIsOn
                }

                mUnityPlayer?.let {
                    transitionAnimator.showGame(
                        executeAfterAnimation = {
                            emulateLifecyclePauseAndStop()

                            requestLandscapeOrientation(fragment.game().runsInLandscape)

                            supportFragmentManager
                                .beginTransaction()
                                .add(gameFragmentContainer().id, fragment)
                                .addToBackStack(fragment::class.java.canonicalName)
                                .commit()
                        }
                    )
                }
            }
        )
    }

    fun finishUnityGame(fragment: BaseUnityGameFragment, result: UnityGameResult<*>) {
        ifUnityGameIsOn(
            execute = { finishUnityGameInternal(result) },
            otherwise = { Timber.w("Cannot finish Unity game from $fragment, there is no game in progress!") }
        )
    }

    private fun finishUnityGameInternal(result: UnityGameResult<*>) {
        transitionAnimator.hideGame(
            executeBeforeAnimation = {
                supportFragmentManager.popBackStackImmediate()

                if (result.game.runsInLandscape) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            },
            executeAfterAnimation = {
                emulateLifecycleStartAndResume()
                onUnityGameFinished(result)
            }
        )
    }

    private fun requestLandscapeOrientation(gameRunsInLandscape: Boolean) {
        requestedOrientation = if (gameRunsInLandscape)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * Callback that returns result of finished Unity game.
     * @param result result object from game
     */
    protected abstract fun onUnityGameFinished(result: UnityGameResult<*>)

    @Deprecated(
        message = "Direct access to lifecycle of UnityPlayerLifecycleActivity is strictly forbidden.",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("addLifecycleObserver() or removeLifecycleObserver()")
    )
    override fun getLifecycle(): Lifecycle {
        return super.getLifecycle()
    }

    protected fun addLifecycleObserver(lifecycleObserver: LifecycleObserver) {
        super.getLifecycle().addObserver(lifecycleObserver)
    }

    protected fun removeLifecycleObserver(lifecycleObserver: LifecycleObserver) {
        super.getLifecycle().removeObserver(lifecycleObserver)
    }

    /**
     * When launching Unity game, we need to emulate the state in which host activity goes to stopped state.
     * This way we stay compatible with old, activity-based games.
     */
    private fun emulateLifecyclePauseAndStop() {
        if (!isFinishing && !oneTimeUse) {
            val lifecycleRegistry = super.getLifecycle() as? LifecycleRegistry
            lifecycleRegistry?.currentState = Lifecycle.State.STARTED
            onPauseInternal()
            lifecycleRegistry?.currentState = Lifecycle.State.CREATED
            onStopInternal()
        }
    }

    /**
     * Similarly, when Unity game finishes, we need to emulate activity going back to resumed state.
     * This way we stay compatible with old, activity-based games.
     */
    private fun emulateLifecycleStartAndResume() {
        if (!isFinishing && !oneTimeUse) {
            val lifecycleRegistry = super.getLifecycle() as? LifecycleRegistry
            lifecycleRegistry?.currentState = Lifecycle.State.STARTED
            onStartInternal()
            lifecycleRegistry?.currentState = Lifecycle.State.RESUMED
            onResumeInternal()
        }
    }

    private fun tearDownUnityPlayerIfNeeded(intent: Intent) {
        if (intent.hasFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)) {
            // New activity will tear us down, we have to react.
            // We have to tear down the player, otherwise the app will crash
            tearDownUnityPlayer()
        }
    }

    private fun tearDownUnityPlayer() {
        mUnityPlayer?.let {
            synchronized(it) {
                if (it.tearDownScheduled) {
                    Timber.w("Teardown already scheduled! Skipping")
                    return
                }

                Timber.d("Scheduling UnityPlayer teardown for state: ${lifecycleMonitor.lastLifecycleState}")

                when {
                    // If the activity is resumed, teardown will happen in onPause()
                    lifecycleMonitor.lastLifecycleState.isAtLeast(Lifecycle.State.RESUMED) ->
                        tearDownPlayerInOnPause = true
                    // If we are started - we will finish gracefully in the normal lifecycle.
                    lifecycleMonitor.lastLifecycleState.isAtLeast(Lifecycle.State.STARTED) -> {
                        // no-op
                    }
                    // If we are created and we are about to close, we need to perform full teardown now.
                    // Otherwise player will crash the app. This is usually the case when task is being
                    // cleared and activity didn't have time to reactivate itself.
                    lifecycleMonitor.lastLifecycleState.isAtLeast(Lifecycle.State.CREATED) ->
                        performImmediateTearDown()
                }

                it.tearDownScheduled = true
            }
        }
    }

    private fun resumePlayer() {
        mUnityPlayer?.let {
            UnityPlayer.currentActivity = this
            it.resume()
        }
    }

    private fun pausePlayer() {
        mUnityPlayer?.let {
            UnityPlayer.currentActivity = null
            mUnityPlayer?.pause()
        }
    }

    private fun performImmediateTearDown() {
        pausePlayer()

        destroyPlayer()
    }

    private fun onTearingDownProcess() {
        /**
         * Schedule offline brushings periodic background extractor before killing the process
         *
         * See https://github.com/kolibree-git/android-monorepo/pull/295
         */
        backgroundJobManager.forEach { it.scheduleJob(applicationContext) }
    }

    private fun destroyPlayer() {
        mUnityPlayer?.let {
            stopService(Intent(this, KolibreeService::class.java))
            it.performKill = isTaskRoot

            if (it.performKill) {
                onTearingDownProcess()
            }

            it.destroy()
            gameFragmentContainer().removeView(it)
            it.tearDownScheduled = false
        }
        mUnityPlayer = null
    }

    protected fun <T> ifUnityGameIsOn(
        execute: (UnityPlayer) -> T,
        otherwise: () -> T
    ): T = mUnityPlayer?.let {
        return if (supportFragmentManager.fragments.any { fragment -> fragment is BaseUnityGameFragment })
            execute(it)
        else
            otherwise()
    } ?: otherwise()

    /**
     * Responsible for showing/hiding Unity game fragments + management of view flags.
     * This way game fragment doesn't contain any logic that is not strictly related to the game.
     */
    private inner class TransitionAnimator {

        private var preGamePixelFormat: Int = PixelFormat.OPAQUE

        private var preGameSystemUiVisibility: Int = 0

        inline fun showGame(
            crossinline executeBeforeAnimation: () -> Unit = {},
            crossinline executeAfterAnimation: () -> Unit = {}
        ) {
            val executionBlock = {
                prepareWindowForGame()
                nonGameViews().forEach { view -> view.visibility = View.INVISIBLE }
                player?.view?.visibility = View.VISIBLE
                executeAfterAnimation()
            }

            if (oneTimeUse) {
                executeBeforeAnimation()
                executionBlock()
            } else runTransitionAnimation(
                animationId = R.anim.fade_in,
                executeBeforeAnimation = executeBeforeAnimation,
                onAnimationEnd = executionBlock
            )
        }

        inline fun hideGame(
            crossinline executeBeforeAnimation: () -> Unit = {},
            crossinline executeAfterAnimation: () -> Unit = {}
        ) {
            val firstExecutionBlock = {
                cleanupWindowAfterGame()
                nonGameViews().forEach { view -> view.visibility = View.VISIBLE }
            }
            val secondExecutionBlock = {
                gameFragmentContainer().visibility = View.INVISIBLE
                executeAfterAnimation()
            }

            if (oneTimeUse) {
                executeBeforeAnimation()
                secondExecutionBlock()
            } else runTransitionAnimation(
                animationId = R.anim.fade_out,
                executeBeforeAnimation = executeBeforeAnimation,
                onAnimationStart = firstExecutionBlock,
                onAnimationEnd = secondExecutionBlock
            )
        }

        private inline fun runTransitionAnimation(
            @AnimRes animationId: Int,
            crossinline executeBeforeAnimation: () -> Unit = {},
            crossinline onAnimationStart: () -> Unit = {},
            crossinline onAnimationEnd: () -> Unit = {}
        ) {
            val animation =
                AnimationUtils.loadAnimation(this@UnityPlayerLifecycleActivity, animationId)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    /*no-op*/
                }

                override fun onAnimationStart(animation: Animation?) = onAnimationStart()

                override fun onAnimationEnd(animation: Animation?) = onAnimationEnd()
            })

            // UnityPlayer is an overlay, we wouldn't see a thing
            player?.view?.visibility = View.INVISIBLE
            gameFragmentContainer().visibility = View.VISIBLE

            gameFragmentContainer().animation?.cancel()
            gameFragmentContainer().clearAnimation()
            gameFragmentContainer().animation = animation

            executeBeforeAnimation()

            animation.start()
        }

        private fun prepareWindowForGame() {
            player?.view?.keepScreenOn = true

            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

            preGameSystemUiVisibility = window.decorView.systemUiVisibility

            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            window.decorView.systemUiVisibility = flags
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    window.decorView.systemUiVisibility = flags
                }
            }

            preGamePixelFormat = window.attributes.format
            window.setFormat(PixelFormat.RGBX_8888) // <--- This makes xperia play happy
        }

        private fun cleanupWindowAfterGame() {
            player?.view?.keepScreenOn = false
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.decorView.setOnSystemUiVisibilityChangeListener(null)
            window.decorView.systemUiVisibility = preGameSystemUiVisibility
            window.setFormat(preGamePixelFormat)
        }
    }
}
