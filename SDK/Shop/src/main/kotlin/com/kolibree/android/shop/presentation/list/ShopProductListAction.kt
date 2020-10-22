/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.list

import com.kolibree.android.app.base.BaseAction

internal sealed class ShopProductListAction : BaseAction {

    class ScrollToPosition(val position: Int) : ShopProductListAction()
}
