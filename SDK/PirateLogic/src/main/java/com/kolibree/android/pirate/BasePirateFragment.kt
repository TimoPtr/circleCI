/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate

import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.fragment.BaseUnityGameFragment
import com.kolibree.android.app.unity.UnityGame
import com.kolibree.android.app.unity.UnityGameResult
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.GameViewState
import com.kolibree.android.pirate.controller.PirateControllerFactory
import com.kolibree.android.pirate.controller.WorldController
import com.kolibree.android.pirate.tuto.TutoRepository
import com.kolibree.android.pirate.utils.PirateCallbackReloader
import com.kolibree.android.pirate_logic.BuildConfig
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.listener.RawDetectorListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.kolibree.sdkws.data.model.gopirate.GoPirateData
import com.kolibree.sdkws.data.model.gopirate.UpdateGoPirateData
import com.kolibree.sdkws.utils.ProfileUtils
import com.unity3d.player.UnityPlayer
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

const val POPUP_GAME_NOT_ALLOWED = 8
const val TAG_UNITY = "PirateFragment|Unity"

/**
 * Pirate game fragment.
 *
 * Update @lookash / 02/03/19
 * Logic was transferred from PirateActivity and cleaned up a bit. This fragment still contains legacy code from
 * the previous implementation.
 */
@Suppress("TooManyFunctions", "LargeClass")
@VisibleForApp
abstract class BasePirateFragment : BaseUnityGameFragment(),
    HasAndroidInjector, TrackableScreen {

    override fun game(): UnityGame = UnityGame.Pirate

    var result = UnityGameResult<Void>(game = game())

    private val pirateFragmentCallback = PirateFragmentCallback()

    private val mainHandler = Handler(getMainLooper())

    internal var mUpdateGoPirateData: UpdateGoPirateData? = null // TODO

    internal var gameController: WorldController? = null

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Any>

    @VisibleForTesting
    internal lateinit var viewModel: BasePirateFragmentViewModel

    @Inject
    internal lateinit var viewModelFactory: BasePirateFragmentViewModelFactory

    @Inject
    lateinit var profileUtils: ProfileUtils

    @Inject
    protected lateinit var connector: IKolibreeConnector

    @Inject
    internal lateinit var tutoRepository: TutoRepository

    @Inject
    internal lateinit var checkupCalculator: CheckupCalculator

    @Inject
    internal lateinit var pirateControllerFactory: PirateControllerFactory

    internal val toothbrushModel: ToothbrushModel by lazy {
        arguments?.getSerializable(BUNDLE_TOOTHBRUSH_MODEL) as? ToothbrushModel
            ?: throw IllegalArgumentException("You should provide a toothbrushModel")
    }

    internal val macAddress: String by lazy {
        arguments?.getString(BUNDLE_TOOTHBRUSH_MAC_ADDRESS, null)
            ?: throw IllegalArgumentException("You should provide a macAddress")
    }

    private var finalWorldId = 0
    private val isRightHand = true
    private var gameControllerIsRunning = false

    private val rawDataDetector = RawDetectorListener { _, data ->
        gameController?.onRawData(data)
    }

    private var rank: Int = 0 // 9
    private var currentGold: Int = 0
    private var brushingCount: Int = 0 // 50
    private var lastWorldReached: Int = 0 // 2
    private var lastLevelReached: Int = 0 // 3
    private var lastLevelBrush: Int = 0 // 2
    private var lastShipBought: Int = 0 // 2
    private var avatarColor: Int = 0
    private var treasure: ArrayList<Int>? = null
    private var targetBrushingTime: Int = 0
    private var data: CreateBrushingData? = null
    private var commitWriteTransactionUpdate = true
    private var level: Int = 0

    override fun androidInjector() = fragmentInjector

    override fun onCreateInternal(savedInstanceState: Bundle?) {
        super.onCreateInternal(savedInstanceState)
        setUpGameState()
        initViewModel()
        PirateCallbackReloader.getInstance().setCurrentCallback(pirateFragmentCallback)
    }

    override fun onDestroyInternal() {
        PirateCallbackReloader.getInstance().setCurrentCallback(null)
        super.onDestroyInternal()
    }

    override fun setChosenConnection(chosenConnection: KLTBConnection) {
        // no-op
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(PirateFragmentViewModel::class.java)

        addToDisposables(
            viewModel
                .viewStateObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::render, Timber::e)
        )
    }

    protected abstract fun maybeShowProfileNotAllowedToBrushPopup()

    private fun initGameController(finalWorldId: Int) {
        connector.currentProfile?.let {
            targetBrushingTime = it.brushingGoalTime
            gameController = pirateControllerFactory.getWorldController(
                isRightHand, finalWorldId
            )?.also { gameController ->
                gameController.init(targetBrushingTime)
                gameControllerIsRunning = false
            } ?: throw IllegalStateException("Impossible to get WorldController")
        }
    }

    private fun render(viewState: PirateFragmentViewState) {
        when (viewState.actionId) {
            GameViewState.ACTION_ON_DATA_SAVED -> {
                Timber.v("Data saved")
            }
            GameViewState.ACTION_ERROR_SOMETHING_WENT_WRONG -> onSomethingWentWrong()
            GameViewState.ACTION_NONE -> Timber.v("Action ACTION_NONE received")
        }
    }

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        Timber.v("onConnectionStateChanged(newState = $newState)")
        when (newState) {
            KLTBConnectionState.ACTIVE -> maybeStartBrushing(connection)
            else -> maybeStopBrushing(connection)
        }
    }

    override fun onVibratorOn(connection: KLTBConnection) {
        Timber.v("onVibratorOn")
        maybeStartBrushing(connection)
    }

    override fun onVibratorOff(connection: KLTBConnection) {
        Timber.v("onVibratorOff")
        maybeStopBrushing(connection)
    }

    private fun maybeStartBrushing(connection: KLTBConnection) {
        Timber.v("maybeStartBrushing")
        when {
            connection.state().current != KLTBConnectionState.ACTIVE -> {
                Timber.w("Connection state ${connection.state().current} != ACTIVE, cannot start the game")
            }
            !connection.vibrator().isOn -> Timber.w("Vibration is not on, cannot start the game")
            gameControllerIsRunning -> Timber.v("Game is already running")
            else -> startBrushingInternal(connection)
        }
    }

    /*
     * Do not call this method directly, it can be called only from within maybeStartBrushing()
     */
    private fun startBrushingInternal(connection: KLTBConnection) {
        gameController?.let {
            Timber.v("Starting game")
            it.run()

            enableDetectors(connection)

            startBrushingUnity()

            addToDisposables(
                connection
                    .brushing()
                    .monitorCurrent()
                    .retry(2)
                    .subscribe({ }, Timber::e)
            )
            gameControllerIsRunning = true
        } ?: run {
            Timber.v("Init controller before starting game")
            initGameController(finalWorldId)
            startBrushingInternal(connection) // Sometimes the game controller is not initialized at startup
        }
    }

    private fun enableDetectors(connection: KLTBConnection) =
        // https://kolibree.atlassian.net/browse/KLTB002-9984

        if (connection.state().current != KLTBConnectionState.ACTIVE) {
            onSomethingWentWrong()
        } else {
            connection.detectors().enableRawDataNotifications()
            connection.detectors().rawData().register(rawDataDetector)
        }

    private fun maybeStopBrushing(connection: KLTBConnection) {
        Timber.v("maybeStopBrushing")
        if (gameControllerIsRunning) {
            Timber.v("Stopping brushing")
            gameController?.let {
                it.pause()
                stopBrushingUnity()
                disableDetectors(connection)
            }
            gameControllerIsRunning = false
        } else {
            Timber.v("Brushing is already stopped")
        }
    }

    private fun disableDetectors(connection: KLTBConnection) {
        connection.detectors().rawData().unregister(rawDataDetector)
        if (connection.state().current == KLTBConnectionState.ACTIVE) {
            connection.detectors().disableRawDataNotifications()
        }
    }

    override fun getScreenName(): AnalyticsEvent = PirateAnalytics.main()

    @CallSuper
    protected open fun onSomethingWentWrong() {
        result.success = false
        finishGame(result)
    }

    private fun startBrushingUnity() {
        UnityPlayer.UnitySendMessage("BrushController", "Run", "")
    }

    private fun stopBrushingUnity() {
        UnityPlayer.UnitySendMessage("BrushController", "Stop", "")
    }

    private fun setUpGameState() {
        if (!connector.hasConnectedAccount()) {
            onNewPreviousGoPirateData()
        } else {
            try {
                val goPirateData = readActiveProfilePirateData()
                    .subscribeOn(Schedulers.io())
                    .blockingGet()

                rank = goPirateData.rank
                currentGold = goPirateData.gold
                brushingCount = goPirateData.brushingNumber
                lastWorldReached = goPirateData.lastWorldReached
                lastLevelReached = goPirateData.lastLevelReached
                lastLevelBrush = goPirateData.lastLevelBrush
                lastShipBought = goPirateData.lastShipBought
                avatarColor = goPirateData.avatarColor
                treasure = goPirateData.treasures
            } catch (ignore: Exception) {
                onNewPreviousGoPirateData()
            }
        }
    }

    private fun onNewPreviousGoPirateData() {
        treasure = ArrayList()
        lastShipBought = -1
    }

    private fun readActiveProfilePirateData(): Single<GoPirateData> {
        return connector.withCurrentProfile()?.goPirateData
            ?: Single.error(RuntimeException("Current profile is null or fetched PirateData is null!"))
    }

    @Keep
    @Suppress("TooManyFunctions")
    inner class PirateFragmentCallback : PirateCallback.Proxy<BasePirateFragment>(this) {

        override fun goPirate_pirateExitCurrentLevel() {
            Timber.tag(TAG_UNITY).v("goPirate_pirateExitCurrentLevel")
        }

        override fun goPirate_getRightHanded(): Boolean {
            Timber.tag(TAG_UNITY).v("goPirate_getRightHanded")
            return connector.currentProfile?.isRightHanded() ?: false
        }

        override fun goPirate_getPlayerName(): String? {
            Timber.tag(TAG_UNITY).v("goPirate_getPlayerName")
            return connector.currentProfile?.firstName
        }

        override fun goPirate_getBrushingTime(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getBrushingTime")
            targetBrushingTime = connector.currentProfile?.brushingGoalTime ?: 0
            return targetBrushingTime
        }

        override fun goPirate_getGender(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getGender")
            return if (connector.currentProfile!!.isMale()) 0 else 1
        }

        override fun goPirate_getRank(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getRank")
            return rank
        }

        override fun goPirate_setRank(rank: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_setRank")
            mUpdateGoPirateData?.rank = rank
        }

        override fun goPirate_getCurrentGold(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getCurrentGold")
            return currentGold
        }

        override fun goPirate_getBrushingsCount(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getBrushingsCount")
            return brushingCount
        }

        override fun goPirate_getLastWorldReached(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getLastWorldReached")
            return lastWorldReached
        }

        override fun goPirate_setLastWorldReached(world: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_setLastWorldReached")
            mUpdateGoPirateData?.lastWorldReached = world
        }

        override fun goPirate_getLastLevelReached(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getLastLevelReached")
            return lastLevelReached
        }

        override fun goPirate_setLastLevelReached(level: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_setLastLevelReached")
            mUpdateGoPirateData?.lastLevelReached = level
        }

        override fun goPirate_getLastLevelBrush(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getLastLevelBrush")
            return lastLevelBrush
        }

        override fun goPirate_setLastLevelBrush(level: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_setLastLevelBrush")
            mUpdateGoPirateData?.lastLevelBrush = level
        }

        override fun goPirate_getLastShipBought(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getLastShipBought")
            return lastShipBought
        }

        override fun goPirate_setLastShipBought(ship: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_setLastShipBought")
            mUpdateGoPirateData?.lastShipBought = ship
        }

        override fun goPirate_getAvatarColor(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getAvatarColor")
            return avatarColor
        }

        override fun goPirate_setAvatarColor(color: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_setAvatarColor")
            mUpdateGoPirateData?.avatarColor = color
        }

        override fun goPirate_getTreasures(): String {
            Timber.tag(TAG_UNITY).v("goPirate_getTreasures")
            return treasure?.joinToString(separator = ":") { elem -> elem.toString() } ?: ""
        }

        override fun goPirate_newTreasureFound(treasure: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_newTreasureFound")
            mUpdateGoPirateData?.onNewTreasure(treasure)
        }

        override fun goPirate_tutorialEnabled(): Boolean {
            Timber.tag(TAG_UNITY).v("goPirate_tutorialEnabled")
            return connector.currentProfile?.let { tutoRepository.hasSeenPirateTuto(it.id) }
                ?: false
        }

        override fun goPirate_setTutorialEnabled(enabled: Boolean) {
            Timber.tag(TAG_UNITY).v("goPirate_setTutorialEnabled")
            connector.currentProfile?.let { tutoRepository.setHasSeenPirateTuto(it.id) }
        }

        override fun goPirate_hasSeenTrailer(): Boolean {
            Timber.tag(TAG_UNITY).v("goPirate_hasSeenTrailer")
            return connector.currentProfile?.let { tutoRepository.hasSeenPirateTrailer(it.id) }
                ?: false
        }

        override fun goPirate_setHasSeenTrailer(seen: Boolean) {
            Timber.tag(TAG_UNITY).v("goPirate_setHasSeenTrailer")
            connector.currentProfile?.let { tutoRepository.setHasSeenPirateTrailer(it.id) }
        }

        // ------------------------------------------------------------------------------------------- //
        // -------------- CALL FROM UNITY TO ANDROID : CALL AT THE END OF THE GAME ------------------- //
        // ------------------------------------------------------------------------------------------- //

        override fun goPirate_hasSeenGameCompleteTrailer(): Boolean {
            Timber.tag(TAG_UNITY).v("goPirate_hasSeenGameCompleteTrailer")
            return connector.currentProfile?.let { tutoRepository.hasSeenPirateCompleteTrailer(it.id) }
                ?: false
        }

        override fun goPirate_setHasSeenGameCompleteTrailer(seen: Boolean) {
            Timber.tag(TAG_UNITY).v("goPirate_setHasSeenGameCompleteTrailer")
            connector.currentProfile?.let { tutoRepository.setHasSeenPirateCompleteTrailer(it.id) }
        }

        // ------------------------------------------------------------------------------------------- //
        // ------------------- CALL FROM UNITY TO ANDROID : DATA WRITING ----------------------------- //
        // ------------------------------------------------------------------------------------------- //

        override fun goPirate_newGoldEarned(gold: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_newGoldEarned")

            mUpdateGoPirateData?.onGoldEarned(gold)
            if (gold > 0) {
                gameController?.let {
                    it.addGoldEarned(gold)
                    data = it.getBrushingData(targetBrushingTime)
                }
            }
        }

        override fun goPirate_brushingComplete(time: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_brushingComplete")

            mUpdateGoPirateData?.onBrushing()
            gameController?.setCompleteTime(time)

            // Next goPirate_commitWriteTransaction will be a create brushing and not an update
            commitWriteTransactionUpdate = false

            result.success = true
        }

        override fun goPirate_beginWriteTransaction() {
            Timber.tag(TAG_UNITY).v("goPirate_beginWriteTransaction")
            mUpdateGoPirateData = UpdateGoPirateData()
        }

        // ------------------------------------------------------------------------------------------- //
        // ------------------- CALL FROM UNITY TO ANDROID : GAME EVENT ------------------------------- //
        // ------------------------------------------------------------------------------------------- //

        override fun goPirate_commitWriteTransaction() {
            Timber.tag(TAG_UNITY).v("goPirate_commitWriteTransaction")
            if (commitWriteTransactionUpdate) {
                // Call for update
                viewModel.updatePirateData(mUpdateGoPirateData)
            } else {
                activity?.let {
                    val appVersions = KolibreeAppVersions(it)
                    data?.let { data ->
                        data.addSupportData(
                            connection?.toothbrush()?.serialNumber,
                            connection?.toothbrush()?.mac,
                            appVersions.appVersion,
                            appVersions.buildVersion
                        )

                        // Call for create brushing
                        commitWriteTransactionUpdate = true
                        viewModel.onBrushingCompleted(data, mUpdateGoPirateData)
                    }
                }
            }
        }

        private fun isAllowedToBrush(): Boolean {
            return BuildConfig.DEBUG || profileUtils.isAllowedToBrush
        }

        override fun goPirate_levelShouldStart(worldId: Int): Boolean {
            Timber.tag(TAG_UNITY).v("goPirate_levelShouldStart")
            if (isAllowedToBrush()) {
                goPirate_sendEnterLevelNotification(worldId)
                while (gameController == null) {
                    Thread.sleep(100)
                }
                return true
            } else if (!profileUtils.isAllowedToBrush) {
                maybeShowProfileNotAllowedToBrushPopup()
                return false
            }
            return false
        }

        override fun goPirate_levelWillRestart(worldId: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_levelWillRestart")
            goPirate_sendEnterLevelNotification(worldId)
        }

        @Suppress("MagicNumber")
        override fun goPirate_sendEnterLevelNotification(worldId: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_sendEnterLevelNotification")
            this@BasePirateFragment.finalWorldId = worldId
            initGameController(finalWorldId)

            // TODO remove this direct manipulation, rely on something more solid (world-bounded values for ex.)
            level = when (worldId) {
                0 -> lastLevelReached + 1
                1 -> lastLevelReached + 4
                2 -> lastLevelReached + 9
                3 -> lastLevelReached + 16
                else -> -1
            }
        }

        @Suppress("MagicNumber")
        override fun goPirate_prescribedZoneDidChange(worldId: Int, zoneId: Int) {
            Timber.tag(TAG_UNITY).v("goPirate_prescribedZoneDidChange")
            // TODO remove this direct manipulation, rely on something more solid (world-bounded values for ex.)
            var nextZoneId = zoneId + 1
            if (worldId == 1) {
                nextZoneId -= 8
            } else if (worldId >= 2) {
                nextZoneId -= 20
            }
            gameController?.setPrescribedZoneId(nextZoneId)
        }

        override fun goPirate_shouldChangeLane() {
            Timber.tag(TAG_UNITY).v("goPirate_shouldChangeLane")
            mainHandler.post {
                gameController?.shouldChangeLane()
            }
        }

        override fun goPirate_pirateDidCrossFinishLine() {
            Timber.tag(TAG_UNITY).v("goPirate_pirateDidCrossFinishLine")
            gameController?.stop()
            connection?.let {
                addToDisposables(
                    it.vibrator()
                        .off()
                        // TODO for M1 we get FailEarly kick, as it returns .complete(), find a better way
                        .delay(100, TimeUnit.MILLISECONDS, Schedulers.computation())
                        .subscribe({ }, Timber::e)
                )
            }
        }

        @Suppress("MagicNumber")
        override fun goPirate_getToothbrushModel(): Int {
            Timber.tag(TAG_UNITY).v("goPirate_getToothbrushModel")
            return connection?.let {
                when (it.toothbrush().model) {
                    ToothbrushModel.ARA -> 2
                    ToothbrushModel.CONNECT_E1 -> 3
                    ToothbrushModel.CONNECT_M1 -> 4
                    else -> 0
                }
            } ?: 0
        }

        override fun goPirate_gameDidFinish() {
            Timber.tag(TAG_UNITY).v("goPirate_gameDidFinish")
            mainHandler.post { finishGame(result) }
        }
    }
}

@Keep
fun addArguments(
    fragment: BasePirateFragment,
    toothbrushModel: ToothbrushModel,
    macAddress: String
) {
    fragment.arguments = Bundle().apply {
        putSerializable(BUNDLE_TOOTHBRUSH_MODEL, toothbrushModel)
        putString(BUNDLE_TOOTHBRUSH_MAC_ADDRESS, macAddress)
    }
}

private const val BUNDLE_TOOTHBRUSH_MODEL = "pirate_toothbrush_model"
private const val BUNDLE_TOOTHBRUSH_MAC_ADDRESS = "pirate_mac_address"
