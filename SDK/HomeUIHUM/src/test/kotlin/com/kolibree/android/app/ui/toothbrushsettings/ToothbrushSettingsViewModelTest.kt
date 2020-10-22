/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import androidx.lifecycle.Lifecycle
import com.kolibree.account.utils.ToothbrushForgetter
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionData
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionUseCase
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushHeadConditionState
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushNameItemBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.usecase.RenameToothbrushNameUseCase
import com.kolibree.android.app.ui.toothbrushsettings.usecase.UpdateIfDirtyUseCase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.offlinebrushings.sync.LastSyncDate
import com.kolibree.android.offlinebrushings.sync.LastSyncObservable
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.AccountToothbrushBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel
import com.kolibree.android.toothbrush.battery.domain.BatteryLevelUseCase
import com.kolibree.android.toothbrushupdate.OtaChecker
import com.kolibree.android.toothbrushupdate.OtaForConnection
import com.kolibree.android.toothbrushupdate.OtaUpdateType
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.pairing.assistant.PairingAssistant
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ToothbrushSettingsViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: ToothbrushSettingsViewModel

    private val navigator: ToothbrushSettingsNavigator = mock()

    private val toothbrushRepository: ToothbrushRepository = mock()

    private val lastSyncObservable: LastSyncObservable = mock()

    private val serviceProvider: ServiceProvider = mock()

    private val brushHeadConditionUseCase: BrushHeadConditionUseCase = mock()

    private val updateIfDirtyUseCase: UpdateIfDirtyUseCase = mock()

    private val pairingAssistant: PairingAssistant = mock()

    private val timeScheduler = TestScheduler()

    private val renameToothbrushNameUseCase: RenameToothbrushNameUseCase = mock()

    private val toothbrushForgetter: ToothbrushForgetter = mock()

    private val batteryLevelUseCase: BatteryLevelUseCase = mock()

    private val otaChecker: OtaChecker = mock()

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        whenever(toothbrushRepository.readAccountToothbrush(MAC_ADDRESS))
            .thenReturn(Flowable.never())
        whenever(serviceProvider.connectStream())
            .thenReturn(Observable.never())
        whenever(lastSyncObservable.observable())
            .thenReturn(Observable.never())
        whenever(brushHeadConditionUseCase.headCondition(MAC_ADDRESS))
            .thenReturn(Single.never())
        whenever(brushHeadConditionUseCase.resetBrushHead(MAC_ADDRESS, SERIAL))
            .thenReturn(Single.never())

        viewModel = ToothbrushSettingsViewModel(
            initialViewState = ToothbrushSettingsViewState.initial(MAC_ADDRESS),
            navigator = navigator,
            toothbrushRepository = toothbrushRepository,
            lastSyncObservable = lastSyncObservable,
            updateIfDirtyUseCase = updateIfDirtyUseCase,
            toothbrushMac = MAC_ADDRESS,
            serviceProvider = serviceProvider,
            renameToothbrushNameUseCase = renameToothbrushNameUseCase,
            pairingAssistant = pairingAssistant,
            timeScheduler = timeScheduler,
            brushHeadConditionUseCase = brushHeadConditionUseCase,
            toothbrushForgetter = toothbrushForgetter,
            otaChecker = otaChecker,
            batteryUseCase = batteryLevelUseCase
        )
    }

    @Test
    fun `when user clicks on close icon then app closes screen`() {
        viewModel.onCloseClick()

        verify(navigator).finishScreen()
    }

    @Test
    fun `when user clicks on ConnectNewBrush then app sends analytics event`() {
        viewModel.onConnectNewBrushClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_NewBrush"))
    }

    @Test
    fun `when AccountToothbrush is changed then viewState is updated`() {
        val tbName = "Hum_TB_home"
        val model = ToothbrushModel.HUM_ELECTRIC
        val dspVersion = DspVersion(1, 1, 1)
        val accountToothbrush = AccountToothbrushBuilder.builder()
            .withDefaultState()
            .withName(tbName)
            .withMac(MAC_ADDRESS)
            .withModel(model)
            .withDspVersion(dspVersion)
            .build()

        whenever(toothbrushRepository.readAccountToothbrush(MAC_ADDRESS))
            .thenReturn(Flowable.just(accountToothbrush))

        assertEquals(ToothbrushSettingsViewState.initial(MAC_ADDRESS), viewModel.getViewState())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        assertTrue(viewModel.getViewState()?.toothbrushName == tbName)
        assertTrue(viewModel.getViewState()?.model == model.commercialName)
        assertTrue(viewModel.getViewState()?.serial == KLTBConnectionBuilder.DEFAULT_SERIAL)
        assertTrue(viewModel.getViewState()?.firmware == KLTBConnectionBuilder.DEFAULT_FW_VERSION.toString())
        assertTrue(viewModel.getViewState()?.hardware == KLTBConnectionBuilder.DEFAULT_HW_VERSION.toString())
        assertTrue(viewModel.getViewState()?.bootloader == KLTBConnectionBuilder.DEFAULT_BOOTLOADER_VERSION.toString())
        assertTrue(viewModel.getViewState()?.dsp == dspVersion.toString())
        assertTrue(viewModel.getViewState()?.hasDsp == false)
        assertTrue(viewModel.getViewState()?.mac == MAC_ADDRESS)
    }

    @Test
    fun `when AccountToothbrush is changed then viewState is updated with model that has dsp update viewState`() {
        val tbName = "Hum_TB_home"
        val model = ToothbrushModel.PLAQLESS
        val dspVersion = DspVersion(1, 1, 1)
        val accountToothbrush = AccountToothbrushBuilder.builder()
            .withDefaultState()
            .withName(tbName)
            .withMac(MAC_ADDRESS)
            .withModel(model)
            .withDspVersion(dspVersion)
            .build()

        whenever(toothbrushRepository.readAccountToothbrush(MAC_ADDRESS))
            .thenReturn(Flowable.just(accountToothbrush))

        assertEquals(ToothbrushSettingsViewState.initial(MAC_ADDRESS), viewModel.getViewState())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        assertTrue(viewModel.getViewState()?.toothbrushName == tbName)
        assertTrue(viewModel.getViewState()?.model == model.commercialName)
        assertTrue(viewModel.getViewState()?.serial == KLTBConnectionBuilder.DEFAULT_SERIAL)
        assertTrue(viewModel.getViewState()?.firmware == KLTBConnectionBuilder.DEFAULT_FW_VERSION.toString())
        assertTrue(viewModel.getViewState()?.hardware == KLTBConnectionBuilder.DEFAULT_HW_VERSION.toString())
        assertTrue(viewModel.getViewState()?.bootloader == KLTBConnectionBuilder.DEFAULT_BOOTLOADER_VERSION.toString())
        assertTrue(viewModel.getViewState()?.dsp == dspVersion.toString())
        assertTrue(viewModel.getViewState()?.hasDsp == true)
        assertTrue(viewModel.getViewState()?.mac == MAC_ADDRESS)
    }

    @Test
    fun `when brushHeadConditionUseCase is changed then viewState is updated`() {
        val replacementDate = TrustedClock.getNowLocalDate()
        val brushHeadConditionData = BrushHeadConditionData(
            condition = BrushHeadCondition.GOOD,
            lastReplacementDate = replacementDate
        )
        whenever(brushHeadConditionUseCase.headCondition(MAC_ADDRESS))
            .thenReturn(Single.just(brushHeadConditionData))

        assertNull(viewModel.getViewState()?.brushHeadConditionState)
        assertNull(viewModel.getViewState()?.brushHeadReplacementDate)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        assertEquals(
            BrushHeadConditionState.GOOD,
            viewModel.getViewState()?.brushHeadConditionState
        )
        assertEquals(replacementDate, viewModel.getViewState()?.brushHeadReplacementDate)
    }

    @Test
    fun `when LastSyncData is changed then viewStat is updated`() {
        mockEmptyToothbrushRepository()
        whenever(serviceProvider.connectStream())
            .thenReturn(Observable.never())

        val lastSyncDate = TrustedClock.getNowZonedDateTime()
        val lastSyncData = LastSyncDate(MAC_ADDRESS, lastSyncDate)
        whenever(lastSyncObservable.observable())
            .thenReturn(Observable.just(lastSyncData))

        assertFalse(viewModel.getViewState()?.lastSyncDate == lastSyncDate.toLocalDate())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        assertTrue(viewModel.getViewState()?.lastSyncDate == lastSyncDate.toLocalDate())
    }

    @Test
    fun `when connection is ACTIVE battery level is updated from the usecase`() {
        val batteryLevelValue = 77
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBattery(level = batteryLevelValue)
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        mockEmptyOtaForConnections()
        whenever(batteryLevelUseCase.batteryLevel(connection)).thenReturn(
            Single.just(BatteryLevel.LevelPercentage(batteryLevelValue))
        )

        mockServiceWithConnection(connection)

        assertTrue(viewModel.getViewState()?.batteryLevel == BatteryLevelState.UnknownState)

        viewModel.onConnectionStateChanged(connection, KLTBConnectionState.ACTIVE)

        val expectedBatteryLevel = BatteryLevelState.PercentageState(
            text = "$batteryLevelValue%",
            icon = R.drawable.ic_battery_level_100
        )
        verify(batteryLevelUseCase).batteryLevel(connection)
        assertEquals(expectedBatteryLevel, viewModel.getViewState()?.batteryLevel)
    }

    @Test
    fun `when user taps on toothbrush name then app emits ShowEditBrushNameDialog action`() {
        val actionTest = viewModel.actionsObservable.test()
        val currentName = "TB_HUM (home)"
        val brushNameItem = BrushNameItemBindingModel(
            title = R.string.tb_settings_nickname_title,
            isClickable = true,
            value = currentName
        )

        viewModel.onDetailItemClick(brushNameItem)

        actionTest.assertValue(ToothbrushSettingsActions.ShowEditBrushNameDialog(currentName))
    }

    @Test
    fun `when user taps on toothbrush name then app send analytics event`() {
        val brushNameItem = BrushNameItemBindingModel(
            title = R.string.tb_settings_nickname_title,
            isClickable = true,
            value = "HUM_tb"
        )

        viewModel.onDetailItemClick(brushNameItem)

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_EditNickname"))
    }

    @Test
    fun `when new toothbrush name has been provided then toothbrush name is renamed`() {
        val newName = "HUM_150VX"
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .build()
        viewModel.connection = connection

        whenever(renameToothbrushNameUseCase.rename(connection, newName))
            .thenReturn(Completable.complete())

        viewModel.userRenamedToothbrush(newName)

        verify(renameToothbrushNameUseCase).rename(connection, newName)
    }

    @Test
    fun `when toothbrush name has been provided then app sends Analytics event`() {
        val newName = "New NAME TB"
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .build()
        viewModel.connection = connection

        whenever(renameToothbrushNameUseCase.rename(connection, newName))
            .thenReturn(Completable.complete())

        viewModel.userRenamedToothbrush(newName)

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_EditNickname_Save"))
    }

    @Test
    fun `when user canceled toothbrush rename then app sends Analytics event`() {
        viewModel.userCancelRenamedToothbrush()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_EditNickname_Cancel"))
    }

    @Test
    fun `when connection is ACTIVE is set the isActive flag and set it back when not active anymore`() {
        val service = mock<KolibreeService>()
        val serviceProvisionResult = ServiceConnected(service)
        whenever(serviceProvider.connectStream())
            .thenReturn(Observable.just(serviceProvisionResult))
        mockEmptyOtaForConnections()
        val activeConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        val notActiveConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .build()

        whenever(service.getConnection(MAC_ADDRESS)).thenReturn(activeConnection)
        whenever(batteryLevelUseCase.batteryLevel(any())).thenReturn(Single.never())

        viewModel.onConnectionStateChanged(activeConnection, KLTBConnectionState.ACTIVE)

        assertEquals(true, viewModel.getViewState()?.isActive)

        viewModel.onConnectionStateChanged(notActiveConnection, KLTBConnectionState.ESTABLISHING)

        assertEquals(false, viewModel.getViewState()?.isActive)
    }

    @Test
    fun `when connection state change it check for OTA`() {
        val service = mock<KolibreeService>()
        val serviceProvisionResult = ServiceConnected(service)
        whenever(serviceProvider.connectStream())
            .thenReturn(Observable.just(serviceProvisionResult))
        val activeConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()

        mockEmptyOtaForConnections()
        whenever(batteryLevelUseCase.batteryLevel(any())).thenReturn(Single.never())

        viewModel.onConnectionStateChanged(activeConnection, KLTBConnectionState.ACTIVE)

        verify(otaChecker).otaForConnectionsOnce()
    }

    @Test
    fun `when there is an ota available it update the view state with its type`() {
        val service = mock<KolibreeService>()
        val serviceProvisionResult = ServiceConnected(service)
        whenever(serviceProvider.connectStream())
            .thenReturn(Observable.just(serviceProvisionResult))
        val activeConnection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC_ADDRESS)
            .withState(KLTBConnectionState.ACTIVE)
            .build()

        val otaResult = OtaForConnection(activeConnection, OtaUpdateType.STANDARD, mock())

        whenever(otaChecker.otaForConnectionsOnce()).thenReturn(Observable.just(otaResult))
        whenever(batteryLevelUseCase.batteryLevel(any())).thenReturn(Single.never())

        viewModel.onConnectionStateChanged(activeConnection, KLTBConnectionState.ACTIVE)

        verify(otaChecker).otaForConnectionsOnce()

        assertEquals(otaResult.otaUpdateType, viewModel.getViewState()?.otaUpdateType)
    }

    @Test
    fun `when user clicks on Identify Toothbrush it send analytics`() {
        whenever(pairingAssistant.blinkBlue(any())).thenReturn(Single.never())

        viewModel.connection = mock()

        viewModel.onIdentifyClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_BrushBlink"))
    }

    @Test
    fun `when user clicks on IdentifyTB it make the brush blinks`() {
        val connection: KLTBConnection = mock()

        whenever(pairingAssistant.blinkBlue(connection)).thenReturn(Single.just(connection))

        viewModel.connection = connection

        viewModel.onIdentifyClick()

        verify(pairingAssistant).blinkBlue(connection)

        assertEquals(true, viewModel.getViewState()?.isIdentifying)

        timeScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        assertEquals(false, viewModel.getViewState()?.isIdentifying)
    }

    @Test
    fun `when user clicks on IdentifyTB and an error happen it notify the view`() {
        val testObserver = viewModel.actionsObservable.test()
        whenever(pairingAssistant.blinkBlue(any())).thenReturn(Single.error(IllegalAccessError()))

        viewModel.connection = KLTBConnectionBuilder.createAndroidLess().build()
        viewModel.onIdentifyClick()

        timeScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        testObserver.assertValue(ToothbrushSettingsActions.SomethingWrongHappened)
    }

    @Test
    fun `when user clicks on ResetCounter button then app sends analytics event`() {
        whenever(brushHeadConditionUseCase.resetBrushHead(any(), any())).thenReturn(Single.never())

        viewModel.onResetCounterClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_ResetCounter"))
    }

    @Test
    fun `when user clicks on ResetCounter button then brush condition is updated`() {
        val replacementDate = TrustedClock.getNowLocalDate()
        val brushHeadConditionData = BrushHeadConditionData(
            condition = BrushHeadCondition.GOOD,
            lastReplacementDate = replacementDate
        )
        whenever(brushHeadConditionUseCase.resetBrushHead(MAC_ADDRESS, SERIAL))
            .thenReturn(Single.just(brushHeadConditionData))

        assertNull(viewModel.getViewState()?.brushHeadConditionState)
        assertNull(viewModel.getViewState()?.brushHeadReplacementDate)

        viewModel.updateViewState { copy(serial = SERIAL) }
        viewModel.onResetCounterClick()

        assertEquals(
            BrushHeadConditionState.GOOD,
            viewModel.getViewState()?.brushHeadConditionState
        )
        assertEquals(replacementDate, viewModel.getViewState()?.brushHeadReplacementDate)
    }

    @Test
    fun `when user clicks on BuyNew button then app navigates to shop`() {
        viewModel.onBuyNewClick()

        verify(navigator).navigateToShop()
    }

    @Test
    fun `when service is active and connection exists, it subscribes to maybeUpdateDirtyConnection`() {
        mockEmptyToothbrushRepository()
        mockEmptyLastSync()
        val connection = mockServiceWithConnection()!!

        val dirtySubject = CompletableSubject.create()
        whenever(updateIfDirtyUseCase.maybeUpdate(connection))
            .thenReturn(dirtySubject)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        assertTrue(dirtySubject.hasObservers())
    }

    @Test
    fun `when service is active and connection exists, connect doesn't fail if maybeUpdateDirtyConnection emits an error`() {
        mockEmptyToothbrushRepository()
        mockEmptyLastSync()
        mockEmptyOtaForConnections()
        val connection = mockServiceWithConnection()!!

        whenever(batteryLevelUseCase.batteryLevel(connection)).thenReturn(Single.never())

        whenever(updateIfDirtyUseCase.maybeUpdate(connection))
            .thenReturn(Completable.error(TestForcedException()))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        verify(connection.state()).register(viewModel)
    }

    @Test
    fun `when service is active and connection does not exist, it never subscribes to maybeUpdateDirtyConnection`() {
        mockEmptyToothbrushRepository()
        mockEmptyLastSync()
        mockServiceWithConnection(connection = null)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        verifyNoMoreInteractions(updateIfDirtyUseCase)
    }

    @Test
    fun `when user clicks on HelpCenter then app sends analytics event`() {
        viewModel.onHelpCenterClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_Help"))
    }

    @Test
    fun `when user clicks on HelpCenter it navigates to help screen`() {
        viewModel.onHelpCenterClick()

        verify(navigator).showHelp()
    }

    @Test
    fun `when user click on forgetToothbrush it send ShowForgetToothbrushDialog action`() {
        val testObserver = viewModel.actionsObservable.test()

        viewModel.onForgetToothbrushClick()

        testObserver.assertValue(ToothbrushSettingsActions.ShowForgetToothbrushDialog)
    }

    @Test
    fun `when user click on forgetToothbrush it send Analytics event`() {
        viewModel.onForgetToothbrushClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_ForgetBrush"))
    }

    @Test
    fun `when user forget is toothbrush then app sends analytics event`() {
        whenever(toothbrushForgetter.forgetToothbrush(MAC_ADDRESS)).thenReturn(Completable.never())

        viewModel.forgetToothbrush()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_ForgetBrush_Yes"))
    }

    @Test
    fun `when user forget is toothbrush it removed it from the db and close the settings`() {
        whenever(toothbrushForgetter.forgetToothbrush(MAC_ADDRESS)).thenReturn(Completable.complete())

        viewModel.forgetToothbrush()

        verify(toothbrushForgetter).forgetToothbrush(MAC_ADDRESS)
        verify(navigator).finishScreen()
    }

    @Test
    fun `when user forget is toothbrush and an error occur an error is shown`() {
        val testObserver = viewModel.actionsObservable.test()

        whenever(toothbrushForgetter.forgetToothbrush(MAC_ADDRESS)).thenReturn(
            Completable.error(
                IllegalAccessError()
            )
        )

        viewModel.forgetToothbrush()

        verify(toothbrushForgetter).forgetToothbrush(MAC_ADDRESS)
        verify(navigator, never()).finishScreen()
        testObserver.assertValue(ToothbrushSettingsActions.SomethingWrongHappened)
    }

    @Test
    fun `when user want to connect a new brush it removed it from the db and open pairing`() {
        whenever(toothbrushForgetter.forgetToothbrush(MAC_ADDRESS)).thenReturn(Completable.complete())

        viewModel.connectNewBrush()

        verify(toothbrushForgetter).forgetToothbrush(MAC_ADDRESS)
        verify(navigator).navigatesToPairingScreen()
    }

    @Test
    fun `when user want to connect a new brush then app sends analytics event`() {
        whenever(toothbrushForgetter.forgetToothbrush(MAC_ADDRESS)).thenReturn(Completable.complete())

        viewModel.connectNewBrush()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_PopUp_Forget"))
    }

    @Test
    fun `when user click on ota then app sends analytics event`() {
        viewModel.onOTAClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_BannerFirmwareUpdate"))
    }

    @Test
    fun `when user click on ota it navigates to ota screen`() {
        viewModel.connection = KLTBConnectionBuilder.createAndroidLess().build()
        viewModel.onOTAClick()

        verify(navigator).navigateToOta(
            false,
            MAC_ADDRESS,
            viewModel.connection!!.toothbrush().model
        )
    }

    @Test
    fun `when user clicks on NotConnecting then app navigates to HelpCenter`() {
        viewModel.onNotConnectingClick()

        verify(navigator).showNotConnectingHelpCenter()
    }

    @Test
    fun `when user clicks on NotConnecting then app sends analytics event`() {
        viewModel.onNotConnectingClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_NotConnecting"))
    }

    @Test
    fun `onBackPressed finishes screen`() {
        viewModel.onBackPressed()

        verify(navigator).finishScreen()
    }

    @Test
    fun `onBackPressed sends Analytics event`() {
        viewModel.onBackPressed()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_GoBack"))
    }

    /*
    Utils
    */
    private fun mockServiceWithConnection(
        connection: KLTBConnection? = KLTBConnectionBuilder.createAndroidLess().build()
    ): KLTBConnection? {
        val service = mock<KolibreeService>()
        val serviceProvisionResult = ServiceConnected(service)
        whenever(serviceProvider.connectStream())
            .thenReturn(Observable.just(serviceProvisionResult))
        whenever(service.getConnection(MAC_ADDRESS)).thenReturn(connection)

        return connection
    }

    private fun mockEmptyToothbrushRepository() {
        whenever(toothbrushRepository.readAccountToothbrush(MAC_ADDRESS))
            .thenReturn(Flowable.never())
    }

    private fun mockEmptyLastSync() {
        whenever(lastSyncObservable.observable())
            .thenReturn(Observable.empty())
    }

    private fun mockEmptyOtaForConnections() {
        whenever(otaChecker.otaForConnectionsOnce()).thenReturn(Observable.never())
    }
}

private const val MAC_ADDRESS = "CA:FE:BA:BE"
private const val SERIAL = "SERIAL"
