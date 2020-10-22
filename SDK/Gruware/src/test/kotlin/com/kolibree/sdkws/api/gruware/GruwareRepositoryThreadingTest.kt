/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.api.gruware

import com.google.gson.Gson
import com.kolibree.android.app.test.CommonBaseTest
import com.kolibree.android.network.utils.FileDownloader
import com.kolibree.sdkws.api.gruware.GruwareRepositoryImplTest.FW
import com.kolibree.sdkws.api.gruware.GruwareRepositoryImplTest.HW
import com.kolibree.sdkws.api.gruware.GruwareRepositoryImplTest.MODEL
import com.kolibree.sdkws.api.gruware.GruwareRepositoryImplTest.SERIAL
import com.kolibree.sdkws.api.response.GruwareResponse
import com.kolibree.sdkws.api.response.GruwareResponseTest.GRUWARE_RESPONSE
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.SingleSubject
import java.io.IOException
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class GruwareRepositoryThreadingTest : CommonBaseTest() {

    @Mock
    private lateinit var gruWareManager: GruwareManager

    @Mock
    private lateinit var fileDownloader: FileDownloader

    @Mock
    private lateinit var gruwareRepository: GruwareRepositoryImpl

    @Before
    override fun setup() {
        super.setup()

        gruwareRepository = spy(
            GruwareRepositoryImpl(
                gruWareManager,
                fileDownloader
            )
        )
    }

    @Test
    @Throws(IOException::class)
    fun getGruwareInfo_getGruwareInfosReturnsResponse_observerIsDisposed_createGruwareDataThrowsException_dowsNotThrowUndeliverableException() {
        val gruwareResponse =
            Gson().fromJson<GruwareResponse>(GRUWARE_RESPONSE, GruwareResponse::class.java)
        val responseSubject = SingleSubject.create<GruwareResponse>()
        whenever(gruWareManager.getGruwareInfos(MODEL, HW, SERIAL, FW))
            .thenReturn(responseSubject)

        val disposable = gruwareRepository.getGruwareInfo(MODEL, HW, SERIAL, FW)
            .subscribeOn(Schedulers.io())
            .subscribe(
                { },
                { it.printStackTrace() }
            )

        doAnswer {
            disposable.dispose()

            throw IOException("Test forced error")
        }.whenever(gruwareRepository)
            .createGruwareData(gruwareResponse)

        var capturedError: Throwable? = null
        RxJavaPlugins.setErrorHandler { capturedError = it }

        responseSubject.onSuccess(gruwareResponse)

        assertNull("Error captured, should have been swallowed", capturedError)
    }
}
