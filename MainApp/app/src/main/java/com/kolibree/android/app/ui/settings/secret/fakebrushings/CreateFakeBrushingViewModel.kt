/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.fakebrushings

import android.text.format.DateUtils.FORMAT_ABBREV_ALL
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_TIME
import android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
import android.text.format.DateUtils.formatDateTime
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kolibree.BR
import com.kolibree.BuildConfig
import com.kolibree.R
import com.kolibree.account.AccountFacade
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.toEpochMilli
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.game.BrushingCreator
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.databinding.livedata.LiveDataTransformations
import com.kolibree.sdkws.data.model.CreateBrushingData
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import me.tatarka.bindingcollectionadapter2.ItemBinding
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

/*
Ugly & untested
 */
internal class CreateFakeBrushingViewModel(
    initialViewState: CreateFakeBrushingViewState,
    private val context: ApplicationContext,
    private val brushingCreator: BrushingCreator,
    private val serviceProvider: ServiceProvider,
    private val accountFacade: AccountFacade,
    private val intervalScheduler: Scheduler
) : BaseViewModel<CreateFakeBrushingViewState, CreateFakeBrushingActions>(initialViewState) {
    val games: List<GameToServer> = GameToServer.values().toList()

    private val secondsFormatter = DateTimeFormatter.ofPattern("ss")

    private val processedDataBuilder = FakeProcessedDataBuilder()

    val gameBinding: ItemBinding<GameToServer> = ItemBinding.of(BR.item, R.layout.item_spinner_game)

    val gamePosition: MediatorLiveData<Int> = LiveDataTransformations.twoWayMap(
        viewStateLiveData,
        mapper = { state -> games.indexOf(state?.selectedGame) },
        updateHandler = {
            it?.let {
                val game = games[it]
                onSelectedGame(game)
            }
        }
    )

    private val defaultConnectionList = listOf(NO_TOOTHBRUSH)
    val connectionBinding: ItemBinding<String> =
        ItemBinding.of(BR.item, R.layout.item_spinner_connection)

    private var connections: List<KLTBConnection> = emptyList()
    var connectionNames: MutableLiveData<List<String>> =
        MutableLiveData<List<String>>().apply { value = defaultConnectionList }

    val connectionsPosition: MediatorLiveData<Int> = LiveDataTransformations.twoWayMap(
        viewStateLiveData,
        mapper = { state -> connectionNames.value?.indexOf(state?.selectedToothbrushName) },
        updateHandler = {
            it?.let { selectedIndex ->
                if (selectedIndex != -1) {
                    connectionNames.value?.let { toothbrushNames ->
                        val selectedToothbrushName = toothbrushNames[selectedIndex]
                        onSelecteToothbrush(selectedToothbrushName)
                    }
                }
            }
        }
    )

    private fun onSelecteToothbrush(toothbrushName: String) {
        updateViewState { copy(selectedToothbrushName = toothbrushName) }
    }

    val selectedDateTime: LiveData<String> =
        LiveDataTransformations.map(viewStateLiveData) { viewState ->
            viewState?.createBrushingAt?.let { localDateTime ->
                val dateTime = formatDateTime(
                    context,
                    localDateTime.toEpochMilli(),
                    FORMAT_SHOW_TIME or FORMAT_SHOW_DATE or FORMAT_ABBREV_ALL or FORMAT_SHOW_WEEKDAY
                )
                val seconds = secondsFormatter.format(localDateTime)

                "$dateTime:$seconds"
            } ?: ""
        }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        autoIncreaseTime()
        readConnectionsOnce()
    }

    /**
     * Auto increment current date
     *
     * This is useful to support creating multiple consecutive brushings. Our DB uses timestamp as
     * primary key, thus we don't support brushings with exact same timestamp. DB has seconds precision
     */
    private fun autoIncreaseTime() {
        disposeOnPause {
            Observable.interval(1, TimeUnit.SECONDS, intervalScheduler)
                .subscribe(
                    { updateViewState { copy(createBrushingAt = createBrushingAt.plusSeconds(1)) } },
                    Timber::e
                )
        }
    }

    private fun readConnectionsOnce() {
        disposeOnPause {
            serviceProvider.connectOnce()
                .map { it.knownConnections }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { knownConnections ->
                        connections = knownConnections
                        connectionNames.value = defaultConnectionList +
                            knownConnections.map {
                                it.toothbrush().getName()
                            }
                    },
                    Timber::e
                )
        }
    }

    private fun onSelectedGame(game: GameToServer) {
        updateViewState { copy(selectedGame = game) }
    }

    fun createFakeBrushing() {
        if (accountAllowedToCreateFakeBrushing()) {
            getViewState()?.also { viewState ->
                val brushingData = createBrushingData(viewState)
                val connection = selectedConnection(viewState)

                brushingCreator.onBrushingCompleted(
                    isManual = connection == null,
                    connection = connection,
                    brushingData = brushingData
                )
            } ?: FailEarly.fail("ViewState is null. Unexpected")
        } else {
            pushAction(CreateFakeBrushingActions.InvalidAccount)
        }
    }

    /**
     * On release, only domains in [supportedDomains] can create fake brushings, as requested by
     * Maxime
     */
    private fun accountAllowedToCreateFakeBrushing(): Boolean =
        accountFacade.email.let { email ->
            if (BuildConfig.DEBUG) return true

            if (email.isNullOrBlank()) return false

            return email.domainIsAllowed()
        }

    private fun String.domainIsAllowed(): Boolean {
        return supportedDomains.firstOrNull { domain -> endsWith(domain) } != null
    }

    private fun selectedConnection(viewState: CreateFakeBrushingViewState): KLTBConnection? {
        return connections.firstOrNull {
            it.toothbrush().getName() == viewState.selectedToothbrushName
        }
    }

    private fun createBrushingData(viewState: CreateFakeBrushingViewState): CreateBrushingData {
        val goalDuration = Duration.ofSeconds(GOAL_TIME.toLong())

        return CreateBrushingData(
            game = viewState.selectedGame.serverName, // we filter by non-null games on [games]
            duration = goalDuration,
            goalDuration = GOAL_TIME,
            date = viewState.createBrushingAt.atOffset(TrustedClock.systemZoneOffset),
            coins = 0,
            isFakeBrushing = true
        ).apply {
            setProcessedData(
                processedDataBuilder.build(
                    dateTime = viewState.createBrushingAt,
                    goalDuration = goalDuration,
                    realDuration = goalDuration
                )
            )
        }
    }

    fun onUserClickOnSelectDate() {
        pushAction(CreateFakeBrushingActions.DateClick)
    }

    fun onUserSelectedBrushingDate(localDateTime: LocalDateTime) {
        val truncatedDateTime = localDateTime.truncatedTo(ChronoUnit.MINUTES)
        if (truncatedDateTime.isAfter(TrustedClock.getNowLocalDateTime())) {
            pushAction(CreateFakeBrushingActions.FutureDateSelected)
        } else {
            updateViewState { withDateTime(truncatedDateTime) }
        }
    }

    class Factory @Inject constructor(
        private val context: ApplicationContext,
        private val brushingCreator: BrushingCreator,
        private val serviceProvider: ServiceProvider,
        private val accountFacade: AccountFacade,
        @SingleThreadScheduler private val scheduler: Scheduler
    ) :
        BaseViewModel.Factory<CreateFakeBrushingViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CreateFakeBrushingViewModel(
                initialViewState = viewState ?: CreateFakeBrushingViewState.initial(),
                context = context,
                brushingCreator = brushingCreator,
                serviceProvider = serviceProvider,
                accountFacade = accountFacade,
                intervalScheduler = scheduler
            ) as T
    }
}

private const val GOAL_TIME = 120
private const val NO_TOOTHBRUSH = "No toothbrush"

internal val supportedDomains = listOf("@kolibree.com", "@colpal.com")
