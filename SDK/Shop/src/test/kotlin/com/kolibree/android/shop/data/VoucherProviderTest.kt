/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.shop.data.api.VoucherApi
import com.kolibree.android.shop.data.api.model.VoucherResponse
import com.kolibree.android.shop.domain.model.Voucher
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Test
import retrofit2.Response

internal class VoucherProviderTest : BaseUnitTest() {

    private lateinit var voucherProvider: VoucherProvider

    private val accountDatastore = mock<AccountDatastore>()
    private val api = mock<VoucherApi>()
    private val accountId = 1L

    override fun setup() {
        super.setup()
        val account = mock<AccountInternal>()
        whenever(account.id).thenReturn(accountId)
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(account))
        voucherProvider = VoucherProviderImpl(accountDatastore, api)
    }

    @Test
    fun `getVoucher returns a valid voucher`() {
        val voucherResponse = mock<VoucherResponse>()
        val response = mock<Response<VoucherResponse>>()
        val expectedVoucherCode = "hello"

        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(voucherResponse)
        whenever(voucherResponse.voucherCode).thenReturn(expectedVoucherCode)
        whenever(api.getVoucher(eq(accountId), any())).thenReturn(Single.just(response))

        voucherProvider.getVoucher().test().assertComplete().assertNoErrors().assertValue(Voucher(expectedVoucherCode))
    }

    @Test
    fun `getVoucher exception emits error`() {
        val response = mock<Response<VoucherResponse>>()

        whenever(response.isSuccessful).thenReturn(false)
        whenever(api.getVoucher(eq(accountId), any())).thenReturn(Single.just(response))

        voucherProvider.getVoucher().test().assertError {
            it is ApiError
        }
    }
}
