/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.payment

import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.shop.domain.model.WebViewCheckout

internal sealed class AnotherPaymentAction : BaseAction {
    data class SetPaymentProcessingResult(
        val checkout: WebViewCheckout
    ) : AnotherPaymentAction()
}
