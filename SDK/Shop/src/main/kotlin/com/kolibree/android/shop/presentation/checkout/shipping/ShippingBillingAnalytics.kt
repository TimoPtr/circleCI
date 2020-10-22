/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.shipping

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object ShippingBillingAnalytics {
    fun main() = AnalyticsEvent("Billing")

    fun shippingAndBillingAddressDiff(enabled: Boolean) =
        send(main() + "ShoppingAddressDiff" + if (enabled) "On" else "Off")

    fun goToPayment() = send(main() + "GoToPayment")

    fun quit() = send(main() + "quit")
}
