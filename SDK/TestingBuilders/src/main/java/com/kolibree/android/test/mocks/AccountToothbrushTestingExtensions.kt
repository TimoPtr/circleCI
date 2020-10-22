/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import androidx.annotation.Keep
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.persistence.model.AccountToothbrush

@Keep
fun List<KLTBConnection>.toAccountToothbrush(): List<AccountToothbrush> {
    return map { AccountToothbrushBuilder.fromConnection(it) }
}

@Keep
@JvmOverloads
fun createAccountToothbrush(
    mac: String = KLTBConnectionBuilder.DEFAULT_MAC,
    isShared: Boolean = false,
    model: ToothbrushModel? = null
): AccountToothbrush {
    return AccountToothbrushBuilder.builder()
        .withDefaultState()
        .withModel(model ?: ToothbrushModel.values().random())
        .withMac(mac)
        .withIsShared(isShared)
        .build()
}
