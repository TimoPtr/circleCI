/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

internal interface ScanToothbrushInteraction {
    fun onItemClick(item: ScanToothbrushItemBindingModel)
    fun onBlinkClick(item: ScanToothbrushItemBindingModel)
}
