/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data

import android.annotation.SuppressLint
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.network.toParsedResponseSingle
import com.kolibree.android.shop.data.api.VoucherApi
import com.kolibree.android.shop.data.api.model.VoucherRequest
import com.kolibree.android.shop.domain.model.Voucher
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.Locale
import javax.inject.Inject

@Keep
interface VoucherProvider {
    fun getVoucher(): Single<Voucher>
}

internal class VoucherProviderImpl @Inject constructor(
    private val accountDatastore: AccountDatastore,
    private val api: VoucherApi
) : VoucherProvider {

    override fun getVoucher(): Single<Voucher> {
        // We always discard the previous one
        return accountDatastore.getAccountMaybe()
            .flatMapSingle {
                api.getVoucher(it.id, VoucherRequest(getCountryCode(), cancel = true))
            }
            .subscribeOn(Schedulers.io())
            .toParsedResponseSingle().map { Voucher(it.voucherCode) }
    }

    @SuppressLint("DefaultLocale")
    @VisibleForTesting
    fun getCountryCode(): String = Locale.getDefault().country.toLowerCase()
}
