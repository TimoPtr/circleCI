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
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.sdk.connection.KLTBConnection
import javax.inject.Inject

internal interface SelectToothbrushIconProvider {

    @DrawableRes
    fun getIconFor(connection: KLTBConnection): Int
}

internal class SelectToothbrushIconProviderImpl @Inject constructor() :
    SelectToothbrushIconProvider {

    override fun getIconFor(connection: KLTBConnection): Int {
        return when (connection.toothbrush().model) {
            CONNECT_E1, ARA -> R.drawable.ic_toothbrush_e1_ara
            CONNECT_M1 -> R.drawable.ic_toothbrush_m1
            PLAQLESS -> R.drawable.ic_toothbrush_plq
            else -> R.drawable.ic_toothbrush_e1_ara
        }
    }
}
