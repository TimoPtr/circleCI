/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import com.kolibree.android.shop.data.googlewallet.requests.IsReadyToPayRequestUseCase
import com.kolibree.android.shop.data.googlewallet.requests.PaymentDataRequestUseCase
import javax.inject.Inject

internal interface GooglePayClientWrapper : IsReadyToPayRequestUseCase, PaymentDataRequestUseCase

/*
open for testing :-(
 */
internal open class GooglePayClientFacade
@Inject constructor(
    private val isReadyToPayRequestUseCase: IsReadyToPayRequestUseCase,
    private val paymentDataRequestUseCase: PaymentDataRequestUseCase
) : GooglePayClientWrapper,
    IsReadyToPayRequestUseCase by isReadyToPayRequestUseCase,
    PaymentDataRequestUseCase by paymentDataRequestUseCase
