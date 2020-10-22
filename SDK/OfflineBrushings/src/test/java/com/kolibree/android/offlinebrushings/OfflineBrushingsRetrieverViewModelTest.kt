/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ActiveConnectionUseCase
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.ProfileBuilder.DEFAULT_ID
import com.kolibree.android.test.utils.ReflectionUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class OfflineBrushingsRetrieverViewModelTest : BaseUnitTest() {
    private val extractOfflineBrushingsUseCase: ExtractOfflineBrushingsUseCase = mock()
    private val activeConnectionsStateObservable: ActiveConnectionUseCase = mock()
    private val currentProfileProvider: CurrentProfileProvider = mock()

    private lateinit var viewModel: OfflineBrushingsRetrieverViewModel

    private val currentProfileProcessor =
        BehaviorProcessor.createDefault(ProfileBuilder.create().build())

    private val connectionActiveProcessor = BehaviorProcessor.create<KLTBConnection>()

    override fun setup() {
        super.setup()

        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(currentProfileProcessor)

        whenever(activeConnectionsStateObservable.onConnectionsUpdatedStream())
            .thenReturn(connectionActiveProcessor)
    }

    /*
    onCleared
     */
    @Test
    fun `onCleared disposes CompositeDisposable`() {
        initViewModel()

        assertFalse(viewModel.disposables.isDisposed)

        ReflectionUtils.invokeProtectedVoidMethod(viewModel, "onCleared")

        assertTrue(viewModel.disposables.isDisposed)
    }

    /*
    init
     */
    @Test
    fun `init reads and stores current profileId`() {
        initViewModel()

        assertEquals(DEFAULT_ID, viewModel.currentProfileId)
    }

    @Test
    fun `init updates current profileId`() {
        initViewModel()

        val expectedId = DEFAULT_ID + 65
        currentProfileProcessor.onNext(
            ProfileBuilder.create()
                .withId(expectedId)
                .build()
        )

        assertEquals(expectedId, viewModel.currentProfileId)
    }

    /*
    viewStateObservable
     */
    @Test
    fun `viewStateObservable emits empty ViewState`() {
        setupExtractOfflineBrushings(observable = PublishSubject.create())

        initViewModel()

        viewModel.viewStateObservable.test()
            .assertValueCount(1)
            .assertValue(OfflineBrushingsRetrieverViewState.empty())
    }

    @Test
    fun `viewStateObservable subscribes to extractOfflineBrushingsOnce for each emission of onConnectionsUpdatedStream`() {
        val subject = PublishSubject.create<ExtractionProgress>()
            setupExtractOfflineBrushings(observable = subject)

        initViewModel()

        viewModel.viewStateObservable.test()

        assertFalse(subject.hasObservers())

        connectionActiveProcessor.onNext(KLTBConnectionBuilder.createAndroidLess().withMac("blabla").build())

        assertTrue(subject.hasObservers())
    }

    @Test
    fun `viewStateObservable emits if there syncedBrushing for currentBrushing and brushings read is over zero`() {
        val expectedBrushings = 453
        setupExtractOfflineBrushings(brushingsRead = expectedBrushings)

        initViewModel()

        forceExtractBrushings()

        viewModel.viewStateObservable.test()
            .assertValueCount(2)
            .assertLastValue(
                OfflineBrushingsRetrieverViewState.withRecordsRetrieved(
                    expectedBrushings
                )
            )
            .assertNotComplete()
    }

    @Test
    fun `viewStateObservable emits if ProfileSynchedOfflineBrushings id is SHARED_MODE_PROFILE_ID and brushings read is over zero`() {
        val expectedBrushings = 453
        setupExtractOfflineBrushings(
            profileId = SHARED_MODE_PROFILE_ID,
            brushingsRead = expectedBrushings
        )

        initViewModel()

        forceExtractBrushings()

        viewModel.viewStateObservable.test()
            .assertValueCount(2)
            .assertLastValue(
                OfflineBrushingsRetrieverViewState.withRecordsRetrieved(
                    expectedBrushings
                )
            )
            .assertNotComplete()
    }

    @Test
    fun `viewStateObservable does not emit if current profile Id is different than the ProfileSynchedOfflineBrushings emitted`() {
        setupExtractOfflineBrushings()

        initViewModel()

        currentProfileProcessor.onNext(
            ProfileBuilder.create()
                .withId(DEFAULT_ID + 65)
                .build()
        )

        forceExtractBrushings()

        viewModel.viewStateObservable.test()
            .assertValueCount(1)
            .assertNotComplete()
    }

    @Test
    fun `viewStateObservable does not emit if brushingsRead = 0`() {
        setupExtractOfflineBrushings(brushingsRead = 0)

        initViewModel()

        forceExtractBrushings()

        viewModel.viewStateObservable.test()
            .assertValueCount(1)
            .assertNotComplete()
    }

    @Test
    fun `viewStateObservable does not complete after extractOfflineBrushingsOnce`() {
        setupExtractOfflineBrushings(brushingsRead = 1)

        initViewModel()

        forceExtractBrushings()

        viewModel.viewStateObservable.test()
            .assertValueCount(2)
            .assertNotComplete()
    }

    /*
    utils
     */

    private fun initViewModel() {
        viewModel = OfflineBrushingsRetrieverViewModel(
            extractOfflineBrushingsUseCase,
            activeConnectionsStateObservable,
            currentProfileProvider
        )
    }

    private fun setupExtractOfflineBrushings(
        profileId: Long = DEFAULT_ID,
        brushingsRead: Int = 0,
        observable: Observable<ExtractionProgress> = Observable.just(createExtractionProgress(
            (0 until brushingsRead).map { createOfflineBrushingSyncedResult(profileId = profileId) },
            brushingsRead
        ))
    ): Observable<ExtractionProgress> {
        whenever(extractOfflineBrushingsUseCase.extractOfflineBrushings()).thenReturn(observable)

        return observable
    }

    private fun forceExtractBrushings() {
        connectionActiveProcessor.onNext(KLTBConnectionBuilder.createAndroidLess().build())
    }
}
