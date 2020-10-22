/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.support.product

import com.kolibree.android.app.ui.card.DynamicCardInteraction

internal interface ProductSupportCardInteraction :
    DynamicCardInteraction {

    fun onProductSupportClick()
}
