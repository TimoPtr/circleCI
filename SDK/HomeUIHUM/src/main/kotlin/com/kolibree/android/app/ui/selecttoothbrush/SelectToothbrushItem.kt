/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selecttoothbrush

import androidx.annotation.DrawableRes
import com.kolibree.android.sdk.connection.KLTBConnection

internal data class SelectToothbrushItem(
    val connection: KLTBConnection,
    @DrawableRes val iconRes: Int,
    val isSelected: Boolean = false
) {

    val name: String = connection.toothbrush().getName()
}
