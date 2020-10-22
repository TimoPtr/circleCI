/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.sdkws.account

import androidx.annotation.Keep
import com.kolibree.android.network.toParsedResponseSingle
import com.kolibree.sdkws.uploadByteArray
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@Keep
interface Magik6P0Manager {
    fun get6POUrl(accountId: Long): Single<SignedUrl6POResponse>

    fun get6POUrl(): Single<SignedUrl6POResponse>

    fun upload6PO(url: String, blob: ByteArray): Completable
}

internal class Magik6P0ManagerImpl
@Inject constructor(private val accountApi: AccountApi) : Magik6P0Manager {
    override fun upload6PO(url: String, blob: ByteArray): Completable = uploadByteArray(url, blob)

    override fun get6POUrl(accountId: Long) = accountApi.get6POUrl(accountId).toParsedResponseSingle()

    override fun get6POUrl() = accountApi.get6POUrl().toParsedResponseSingle()
}
