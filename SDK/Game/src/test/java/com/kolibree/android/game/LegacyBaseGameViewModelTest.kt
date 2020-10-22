/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mock

/** Created by miguelaragues on 16/10/17.  */
class LegacyBaseGameViewModelTest : BaseUnitTest() {

    @Mock
    internal lateinit var connector: IKolibreeConnector

    @Mock
    internal lateinit var connectionProvider: KLTBConnectionProvider

    @Mock
    internal lateinit var brushingCreator: BrushingCreator

    internal lateinit var viewModel: StubGameViewModel

    private val appVersions = KolibreeAppVersions("1.0", "2")

    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    @Test
    fun onBrushingCompleted_subscribesToBrushingCreator() {
        createNonManualViewModel()
        val expectedData: CreateBrushingData = mock()

        val brushingCreatorSubject = CompletableSubject.create()
        whenever(brushingCreator.onBrushingCompletedCompletable(false, null, expectedData))
            .thenReturn(brushingCreatorSubject)

        viewModel.onBrushingCompleted(expectedData, 0)

        assertTrue(brushingCreatorSubject.hasObservers())
    }

    @Test
    fun onBrushingCompleted_subscribesToBeforeSendDataCompletable_afterBrushingCreatorCompletes() {
        createNonManualViewModel()
        val expectedData: CreateBrushingData = mock()

        whenever(brushingCreator.onBrushingCompletedCompletable(false, null, expectedData))
            .thenReturn(Completable.complete())

        viewModel.onBrushingCompleted(expectedData, 0)

        assertTrue(viewModel.beforeSendDataCompletableSubject.hasObservers())
    }

    @Test
    fun onBrushingCompleted_emitsACTION_ON_DATA_SAVEDAfterAllCompletablesComplete() {
        createNonManualViewModel()
        val expectedData: CreateBrushingData = mock()

        whenever(brushingCreator.onBrushingCompletedCompletable(false, null, expectedData))
            .thenReturn(Completable.complete())

        viewModel.onBrushingCompleted(expectedData, 0)

        val observer = viewModel.viewStateObservable().test()

        viewModel.beforeSendDataCompletableSubject.onComplete()

        observer.assertLastValue(StubGameViewState(GameViewState.ACTION_ON_DATA_SAVED))
    }

    /*
  EMIT SOMETHING WENT WRONG
   */
    @Test
    fun emitSomethingWentWrong_sendsViewStateWithActionError() {
        createNonManualViewModel()

        val expectedConnection = mock<KLTBConnection>()
        whenever(connectionProvider.existingActiveConnection(eq(DEFAULT_MAC)))
            .thenReturn(Single.just(expectedConnection))

        val observer = viewModel.viewStateObservable().test()

        observer.assertValue {
            StubGameViewState(GameViewState.ACTION_NONE) == it
        }

        viewModel.emitSomethingWentWrong()

        observer.assertValueAt(
            1
        ) { StubGameViewState(GameViewState.ACTION_ERROR_SOMETHING_WENT_WRONG) == it }
    }

    /*
  INIT
   */
    @Test
    fun init_manual_doesNothing() {
        createManualViewModel()

        viewModel.init()
    }

    @Test
    fun init_nonManual_storesConnection() {
        createNonManualViewModel()

        val expectedConnection = mock<KLTBConnection>()
        whenever(connectionProvider.existingActiveConnection(eq(DEFAULT_MAC)))
            .thenReturn(Single.just(expectedConnection))

        assertNull(viewModel.connection)

        viewModel.init()

        assertEquals(expectedConnection, viewModel.connection)
    }

    @Test
    fun init_nonManual_macSingleThrowsError_emitsViewStateSomethingWentWrong() {
        viewModel = spy(
            StubGameViewModel(
                connector,
                connectionProvider,
                Single.error(InterruptedException("Test forced error")),
                brushingCreator,
                KolibreeAppVersions("1.0", "2")
            )
        )

        val observer = viewModel.viewStateObservable().test()

        viewModel.init()

        observer.assertValueAt(
            1
        ) { StubGameViewState(GameViewState.ACTION_ERROR_SOMETHING_WENT_WRONG) == it }
    }

    @Test
    fun init_nonManual_macSingle_emitsViewStateSomethingWentWrong() {
        viewModel = spy(
            StubGameViewModel(
                connector,
                connectionProvider,
                Single.just(DEFAULT_MAC),
                brushingCreator,
                KolibreeAppVersions("1.0", "2")
            )
        )

        val expectedConnection = mock<KLTBConnection>()
        whenever(connectionProvider.existingActiveConnection(eq(DEFAULT_MAC)))
            .thenReturn(Single.just(expectedConnection))

        assertNull(viewModel.connection)

        viewModel.init()

        assertEquals(expectedConnection, viewModel.connection)
    }

    /*
  IS MANUAL
   */

    @Test
    fun isManual_nullSingle_returnsTrue() {
        viewModel = StubGameViewModel(
            connector,
            connectionProvider,
            null,
            brushingCreator,
            KolibreeAppVersions("1.0", "2")
        )

        assertTrue(viewModel.isManual)
    }

    @Test
    fun isManual_validToothbrushMacSingle_returnsFalse() {
        viewModel = StubGameViewModel(
            connector,
            connectionProvider,
            Single.just(DEFAULT_MAC),
            brushingCreator,
            KolibreeAppVersions("1.0", "2")
        )

        assertFalse(viewModel.isManual)
    }

    /*
  UTILS
   */

    private fun createNonManualViewModel() {
        viewModel = StubGameViewModel(
            connector,
            connectionProvider,
            Single.just(DEFAULT_MAC),
            brushingCreator,
            appVersions
        )
    }

    private fun createManualViewModel() {
        viewModel = StubGameViewModel(
            connector,
            connectionProvider,
            null,
            brushingCreator,
            appVersions
        )
    }

    internal class StubGameViewModel(
        connector: IKolibreeConnector,
        provider: KLTBConnectionProvider,
        toothbrushMacSingle: Single<String>?,
        brushingCreator: BrushingCreator,
        appVersions: KolibreeAppVersions
    ) : LegacyBaseGameViewModel<StubGameViewState>(
        connector,
        provider,
        toothbrushMacSingle,
        appVersions,
        brushingCreator
    ) {
        val beforeSendDataCompletableSubject = CompletableSubject.create()

        override fun initialViewState(): StubGameViewState {
            return StubGameViewState(GameViewState.ACTION_NONE)
        }

        override fun beforeSendDataSavedCompletable(): Completable =
            beforeSendDataCompletableSubject
    }

    internal data class StubGameViewState(@GameViewState.ActionId override val actionId: Int) :
        GameViewState<StubGameViewState> {

        override fun withActionId(actionId: Int): StubGameViewState {
            return StubGameViewState(actionId)
        }
    }

    companion object {

        private const val DEFAULT_MAC = "AA:56"
    }
}
