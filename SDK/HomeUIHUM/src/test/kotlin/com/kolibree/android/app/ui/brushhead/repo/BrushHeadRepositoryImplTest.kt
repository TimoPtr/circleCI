/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.repo

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.api.BrushHeadInformationApi
import com.kolibree.android.app.ui.brushhead.api.model.request.BrushHeadInformationResponse
import com.kolibree.android.app.ui.brushhead.api.model.request.data.BrushHeadData
import com.kolibree.android.app.ui.brushhead.sync.brushHeadInfo
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.assertHasObserversAndComplete
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import retrofit2.Response

internal class BrushHeadRepositoryImplTest : BaseUnitTest() {

    private lateinit var repository: BrushHeadRepositoryImpl

    private val replaceDateReader: BrushHeadInformationReader = mock()

    private val replaceDateWriter: BrushHeadReplacedDateWriter = mock()

    private val brushHeadInformationApi: BrushHeadInformationApi = mock()

    private val accountDatastore: AccountDatastore = mock()

    private val brushHeadDateSendApiProvider: BrushHeadDateSendApiProvider = mock()

    override fun setup() {
        super.setup()

        repository = BrushHeadRepositoryImpl(
            replaceDateReader,
            replaceDateWriter,
            brushHeadInformationApi,
            accountDatastore,
            brushHeadDateSendApiProvider
        )
    }

    @Test
    fun `when brush head replace date is known return it`() {
        val mac = "12::33"
        val expectedBrushHeadInformation = brushHeadInfo(
            mac = mac,
            resetDate = TrustedClock.getNowOffsetDateTime()
        )
        whenever(replaceDateReader.read(mac)).thenReturn(Maybe.just(expectedBrushHeadInformation))

        val wrongBrushHeadInfo = brushHeadInfo(
            mac = "trololol",
            resetDate = TrustedClock.getNowOffsetDateTime().minusYears(4)
        )
        whenever(replaceDateWriter.writeReplacedDateNow(mac))
            .thenReturn(Single.just(wrongBrushHeadInfo))

        repository.brushHeadInformationOnce(mac).test()
            .assertValue(expectedBrushHeadInformation)
    }

    @Test
    fun `when brush head replace date is unknown create it and return`() {
        val mac = "12::33"
        val expectedBrushHeadInfo = brushHeadInfo(
            mac = mac,
            resetDate = TrustedClock.getNowOffsetDateTime()
        )
        whenever(replaceDateReader.read(mac)).thenReturn(Maybe.empty())
        whenever(replaceDateWriter.writeReplacedDateNow(mac))
            .thenReturn(Single.just(expectedBrushHeadInfo))

        repository.brushHeadInformationOnce(mac).test()
            .assertValue(expectedBrushHeadInfo)
    }

    @Test
    fun `newBrushHead resets replaced date to now`() {
        val mac = "22::44"
        whenever(replaceDateWriter.writeReplacedDateNow(mac))
            .thenReturn(Single.just(brushHeadInfo()))

        repository.newBrushHeadCompletable(mac).test()

        verify(replaceDateWriter).writeReplacedDateNow(mac)
    }

    @Test
    fun `sendReplacedDateToApiCompletable call the replace brush head API and complete if it is a success`() {
        val mac = "22::44"
        val serial = "serial123"
        val accountId: Long = 123
        val profileId: Long = 456
        val replacedDate = OffsetDateTime.MAX

        val accountInternal = AccountInternal().apply {
            id = accountId
            ownerProfileId = profileId
        }

        val response = Response.success(
            BrushHeadInformationResponse(TrustedClock.getNowOffsetDateTime(), 0, 1, 2, 3)
        )

        whenever(brushHeadDateSendApiProvider.setLastReplacedDateSentCompletable(mac, replacedDate))
            .thenReturn(Completable.complete())
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(accountInternal))
        whenever(
            brushHeadInformationApi.updateBrushHead(
                accountId = accountId,
                profileId = profileId,
                serialNumber = serial,
                macAddress = StrippedMac.fromMac(mac),
                body = BrushHeadData(replacedDate)
            )
        ).thenReturn(Single.just(response))

        val completable =
            repository.sendReplacedDateToApiCompletable(mac, serial, replacedDate).test()

        completable.assertComplete()
        verify(brushHeadDateSendApiProvider).setLastReplacedDateSentCompletable(mac, replacedDate)
    }

    @Test
    fun `sendReplacedDateToApiCompletable call the replace brush head API and should propagate the error`() {
        val mac = "22::44"
        val serial = "serial123"
        val accountId: Long = 123
        val profileId: Long = 456
        val replacedDate = OffsetDateTime.MAX
        val exception = Exception()
        val accountInternal = AccountInternal().apply {
            id = accountId
            ownerProfileId = profileId
        }

        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(accountInternal))
        whenever(
            brushHeadInformationApi.updateBrushHead(
                accountId = accountId,
                profileId = profileId,
                serialNumber = serial,
                macAddress = StrippedMac.fromMac(mac),
                body = BrushHeadData(replacedDate)
            )
        ).thenReturn(Single.error(exception))

        val sendReplacedDateCompletable =
            repository.sendReplacedDateToApiCompletable(mac, serial, replacedDate).test()

        sendReplacedDateCompletable.assertError(exception)
        verify(brushHeadDateSendApiProvider, never()).setLastReplacedDateSentCompletable(
            mac,
            replacedDate
        )
    }

    @Test
    fun `sendReplacedDateToApiCompletable should throw an exception if serial is empty`() {
        val mac = "22::44"
        val emptySerial = ""

        repository.sendReplacedDateToApiCompletable(mac, emptySerial, OffsetDateTime.MAX).test()
            .assertError(IllegalArgumentException::class.java)

        verifyZeroInteractions(brushHeadDateSendApiProvider, brushHeadInformationApi)
    }

    @Test
    fun `sendReplacedDateToApiCompletable should throw an exception if mac is empty`() {
        val emptyMac = ""
        val serial = "serial"

        repository.sendReplacedDateToApiCompletable(emptyMac, serial, OffsetDateTime.MAX).test()
            .assertError(IllegalArgumentException::class.java)

        verifyZeroInteractions(brushHeadDateSendApiProvider, brushHeadInformationApi)
    }

    @Test
    fun `getBrushHeadInformationFromApi should throw an exception if serial is empty`() {
        val mac = "22::44"
        val emptySerial = ""

        repository.getBrushHeadInformationFromApi(mac, emptySerial).test()
            .assertError(IllegalArgumentException::class.java)

        verifyZeroInteractions(brushHeadDateSendApiProvider, brushHeadInformationApi)
    }

    @Test
    fun `getBrushHeadInformationFromApi should throw an exception if mac is empty`() {
        val emptyMac = ""
        val serial = "serial"

        repository.getBrushHeadInformationFromApi(emptyMac, serial).test()
            .assertError(IllegalArgumentException::class.java)

        verifyZeroInteractions(brushHeadDateSendApiProvider, brushHeadInformationApi)
    }

    @Test
    fun `hasBrushHeadReplacedDate should return true if there is a brush head replaced date saved`() {
        val mac = "22::44"

        whenever(replaceDateReader.read(mac)).thenReturn(Maybe.just(brushHeadInfo(resetDate = OffsetDateTime.MIN)))

        repository.hasBrushHeadReplacedDateOnce(mac).test()
            .assertValue(true)
    }

    @Test
    fun `hasBrushHeadReplacedDate should return false if there is no brush head replaced date`() {
        val mac = "22::44"

        whenever(replaceDateReader.read(mac)).thenReturn(Maybe.empty())

        repository.hasBrushHeadReplacedDateOnce(mac).test()
            .assertValue(false)
    }

    @Test
    fun `hasBrushHeadReplacedDate should return error if read emits error`() {
        val mac = "22::44"

        whenever(replaceDateReader.read(mac)).thenReturn(Maybe.error(TestForcedException()))

        repository.hasBrushHeadReplacedDateOnce(mac).test().assertError(TestForcedException::class.java)
    }

    @Test
    fun `setLastApiReplacedDateSent call the provider with the right params`() {
        val mac = "mac"
        val replacedDate = TrustedClock.getNowOffsetDateTime()

        whenever(brushHeadDateSendApiProvider.setLastReplacedDateSentCompletable(mac, replacedDate))
            .thenReturn(Completable.complete())

        val testObserver = repository.setLastDateSentToApiCompletable(mac, replacedDate).test()

        verify(brushHeadDateSendApiProvider).setLastReplacedDateSentCompletable(mac, replacedDate)
        testObserver.assertComplete()
    }

    @Test
    fun `getLastApiReplacedDateSent returns the value emitted by the provider`() {
        val mac = "mac"
        val expectedDate = TrustedClock.getNowOffsetDateTime()

        whenever(brushHeadDateSendApiProvider.getLastReplacedDateSentMaybe(mac))
            .thenReturn(Maybe.just(expectedDate))

        repository.getLastDateSentToApiMaybe(mac).test()
            .assertValue(expectedDate)
            .assertComplete()
    }

    @Test
    fun `writeReplacedDate call the dateWriter method`() {
        val mac = "mac"
        val expectedDate = TrustedClock.getNowOffsetDateTime()

        whenever(brushHeadDateSendApiProvider.getLastReplacedDateSentMaybe(mac))
            .thenReturn(Maybe.just(expectedDate))

        val brushHeadInfo = brushHeadInfo(mac, expectedDate)

        val replaceDateSubject = CompletableSubject.create()
        whenever(replaceDateWriter.writeBrushHeadInformation(brushHeadInfo))
            .thenReturn(replaceDateSubject)

        val observer = repository.writeBrushHeadInfo(brushHeadInfo).test()
            .assertNotComplete()

        replaceDateSubject.assertHasObserversAndComplete()

        observer.assertComplete()
    }

    @Test
    fun `getBrushHeadInformationFromApi query the back-end`() {
        val accountId = 123L
        val profileId = 456L
        val serial = "serial"
        val mac = "mac"
        val strippedMac = StrippedMac.fromMac(mac)

        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.just(AccountInternal(id = accountId, ownerProfileId = profileId)))

        whenever(brushHeadInformationApi.getBrushHeadInformation(any(), any(), any(), any()))
            .thenReturn(
                Single.just(
                    Response.success(
                        BrushHeadInformationResponse(
                            TrustedClock.getNowOffsetDateTime(),
                            0,
                            0,
                            0,
                            0
                        )
                    )
                )
            )

        repository.getBrushHeadInformationFromApi(mac, serial).test()
            .assertComplete()

        verify(brushHeadInformationApi)
            .getBrushHeadInformation(accountId, profileId, serial, strippedMac)
    }
}
