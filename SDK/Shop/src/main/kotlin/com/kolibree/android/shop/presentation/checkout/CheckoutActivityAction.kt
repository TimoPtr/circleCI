/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout

import com.kolibree.android.app.base.BaseAction

internal sealed class CheckoutActivityAction : BaseAction

internal object ToolbarIconClickAction : CheckoutActivityAction()
