/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.fakeImmediateHandler
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.ProfileWrapper
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import timber.log.Timber

class BrushingCreatorTest : BaseUnitTest() {

    private lateinit var brushingCreatorImpl: BrushingCreatorImpl

    private val connector = mock<IKolibreeConnector>()
    private val appVersions = mock<KolibreeAppVersions>()
    private val currentProfileProvider: CurrentProfileProvider = mock()

    private val currentProfileSingleSubject = SingleSubject.create<Profile>()

    override fun setup() {
        super.setup()
        brushingCreatorImpl =
            spy(
                BrushingCreatorImpl(
                    connector = connector,
                    appVersions = appVersions,
                    currentProfileProvider = currentProfileProvider,
                    mainThreadHandler = fakeImmediateHandler()
                )
            )
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(currentProfileSingleSubject)
    }

    @Test
    fun `onDestroyInternal dispose`() {
        val disposable = Flowable.never<Boolean>()
            .subscribe({}, Timber::e)
        brushingCreatorImpl.disposables += disposable

        brushingCreatorImpl.onDestroyInternal()

        assertTrue(disposable.isDisposed)
    }

    /*
    onBrushingCompleted
     */

    @Test
    fun `onBrushingCompleted ends up with error when missing connection for non-manual brushing`() {
        val brushingData = mock<CreateBrushingData>()
        val listener = mock<BrushingCreator.Listener>()
        brushingCreatorImpl.addListener(listener)

        brushingCreatorImpl.onBrushingCompleted(false, null, brushingData)

        verify(listener).somethingWrong(any<IllegalStateException>())
        verify(brushingCreatorImpl, never()).doSendData(any(), any())
    }

    @Test
    fun `onBrushingCompleted invokes addSupportData and invokes doSendData when isManual false and connection is not null`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withSerialNumber("hello")
            .withMac("mac")
            .build()

        val brushingData = mock<CreateBrushingData>()

        whenever(appVersions.appVersion).thenReturn("app")
        whenever(appVersions.buildVersion).thenReturn("build")

        brushingCreatorImpl.onBrushingCompleted(false, connection, brushingData)

        verify(brushingData).addSupportData(eq("hello"), eq("mac"), eq("app"), eq("build"))

        verify(brushingCreatorImpl).doSendData(any(), any())
    }

    @Test
    fun `onBrushingCompleted invokes addSupportData and invokes doSendData with connection null even if given when isManual true`() {
        val brushingData = mock<CreateBrushingData>()
        doNothing().whenever(brushingData).addSupportData(anyOrNull(), anyOrNull(), any(), any())
        doNothing().whenever(brushingCreatorImpl).doSendData(anyOrNull(), any())

        whenever(appVersions.appVersion).thenReturn("app")
        whenever(appVersions.buildVersion).thenReturn("build")

        brushingCreatorImpl.onBrushingCompleted(true, mock(), brushingData)

        verify(brushingData).addSupportData(eq(null), eq(null), eq("app"), eq("build"))
        verify(brushingCreatorImpl).doSendData(eq(null), eq(brushingData))
    }

    /*
    onBrushingCompletedCompletable
     */

    @Test
    fun `onBrushingCompletedCompletable ends up with error when missing connection for non-manual brushing`() {
        val brushingData = mock<CreateBrushingData>()
        val listener = mock<BrushingCreator.Listener>()
        brushingCreatorImpl.addListener(listener)

        brushingCreatorImpl.onBrushingCompletedCompletable(false, null, brushingData).test()

        verify(listener).somethingWrong(any<IllegalStateException>())
        verify(brushingCreatorImpl, never()).doSendData(any(), any())
    }

    @Test
    fun `onBrushingCompletedCompletable creates brushing when isManual false and connection is not null`() {
        val (profile, profileWrapper, brushingData) = mockCreateBrushingSingle()

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withSerialNumber("hello")
            .withMac("mac")
            .build()

        whenever(appVersions.appVersion).thenReturn("app")
        whenever(appVersions.buildVersion).thenReturn("build")

        val listener = mock<BrushingCreator.Listener>()
        brushingCreatorImpl.addListener(listener)

        brushingCreatorImpl.onBrushingCompletedCompletable(false, connection, brushingData).test()

        currentProfileSingleSubject.onSuccess(profile)

        verify(brushingData).addSupportData(eq("hello"), eq("mac"), eq("app"), eq("build"))

        verify(profileWrapper).createBrushingSingle(brushingData)
        verify(brushingCreatorImpl).forceOfflineBrushingStop(connection)
        verify(listener).onSuccessfullySentData()
    }

    @Test
    fun `onBrushingCompletedCompletable invokes addSupportData and creates brushing with connection null even if given when isManual true`() {
        val (profile, profileWrapper, brushingData) = mockCreateBrushingSingle()

        whenever(appVersions.appVersion).thenReturn("app")
        whenever(appVersions.buildVersion).thenReturn("build")

        val listener = mock<BrushingCreator.Listener>()
        brushingCreatorImpl.addListener(listener)

        brushingCreatorImpl.onBrushingCompletedCompletable(true, mock(), brushingData).test()

        currentProfileSingleSubject.onSuccess(profile)

        verify(brushingData).addSupportData(eq(null), eq(null), eq("app"), eq("build"))

        verify(profileWrapper).createBrushingSingle(brushingData)
        verify(brushingCreatorImpl, never()).forceOfflineBrushingStop(any())
        verify(listener).onSuccessfullySentData()
    }

    /*
    forceOfflineBrushingStop
     */

    @Test
    fun `forceOfflineBrushingStop on null connection complete`() {
        brushingCreatorImpl.forceOfflineBrushingStop(null).test().assertComplete()
    }

    @Test
    fun `forceOfflineBrushingStop invokes vibratorOffAndStopRecording`() {

        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        whenever(connection.vibrator().offAndStopRecording()).thenReturn(Completable.complete())

        val returnedCompletable = brushingCreatorImpl.forceOfflineBrushingStop(connection)

        returnedCompletable.test().assertComplete()

        verify(connection.vibrator()).offAndStopRecording()
    }

    /*
    doSendData
     */

    @Test
    fun `doSendData subscribe to source invokes connector withProfileId and createBrushingSingle and forceOfflineBrushingStop and notify listener onSuccessfullySentData`() {
        val (profile, profileWrapper, brushingData) = mockCreateBrushingSingle()

        val connection = mock<KLTBConnection>()

        val listener = mock<BrushingCreator.Listener>()
        brushingCreatorImpl.addListener(listener)

        assertEquals(0, brushingCreatorImpl.disposables.size())

        brushingCreatorImpl.doSendData(connection, brushingData)

        assertEquals(1, brushingCreatorImpl.disposables.size())

        currentProfileSingleSubject.onSuccess(profile)

        verify(profileWrapper).createBrushingSingle(brushingData)
        verify(brushingCreatorImpl).forceOfflineBrushingStop(connection)
        verify(listener).onSuccessfullySentData()
    }

    @Test
    fun `doSendData notify listener with somethingWrong when an error occur`() {
        val listener = mock<BrushingCreator.Listener>()
        val expectedError = TestForcedException()
        brushingCreatorImpl.addListener(listener)
        brushingCreatorImpl.doSendData(null, mock())

        verify(listener, never()).somethingWrong(eq(expectedError))

        currentProfileSingleSubject.onError(expectedError)

        verify(listener).somethingWrong(eq(expectedError))
    }

    /*
    Utils
     */

    private fun mockCreateBrushingSingle(): Triple<Profile, ProfileWrapper, CreateBrushingData> {
        val profile = mock<Profile>()
        val profileWrapper = mock<ProfileWrapper>()
        val brushingData = mock<CreateBrushingData>()
        val brushing = mock<Brushing>()

        whenever(profile.id).thenReturn(10)
        whenever(connector.withProfileId(profile.id)).thenReturn(profileWrapper)
        doReturn(Single.just(brushing)).whenever(profileWrapper).createBrushingSingle(brushingData)
        return Triple(profile, profileWrapper, brushingData)
    }
}
