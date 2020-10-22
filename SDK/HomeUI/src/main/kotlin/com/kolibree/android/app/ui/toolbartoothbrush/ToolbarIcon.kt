/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toolbartoothbrush

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import com.kolibree.android.homeui.R

@SuppressLint("DeobfuscatedPublicSdkClass")
enum class ToolbarIcon(@DrawableRes val iconResource: Int) {
    NoToothbrush(R.drawable.ic_toolbar_add_brush),
    ToothbrushDisconnected(R.drawable.ic_toolbar_tb_disconnected),
    MultiToothbrushDisconnected(R.drawable.ic_toolbar_tb_disconnected_multi),
    MultiToothbrushConnected(R.drawable.ic_toolbar_tb_connected_multi),
    ToothbrushConnectedOta(R.drawable.ic_toolbar_tb_connected_ota),
    ToothbrushConnected(R.drawable.ic_toolbar_tb_connected),
    MultiToothbrushConnectedOta(R.drawable.ic_toolbar_tb_connected_ota_multi),
}
