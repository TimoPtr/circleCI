/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushDetailsClickableItemBindingModel

internal interface ToothbrushSettingsInteraction : BrushHeaderInteraction,
    IdentifyInteraction,
    BrushHeadConditionInteraction,
    BrushDetailsClickableInteraction,
    BottomButtonsInteraction

internal interface BrushHeaderInteraction {
    fun onOTAClick()
    fun onConnectNewBrushClick()
    fun onNotConnectingClick()
}

internal interface IdentifyInteraction {
    fun onIdentifyClick()
}

internal interface BrushHeadConditionInteraction {
    fun onResetCounterClick()
    fun onBuyNewClick()
}

internal interface BrushDetailsClickableInteraction {
    fun onDetailItemClick(item: BrushDetailsClickableItemBindingModel)
}

internal interface BottomButtonsInteraction {
    fun onHelpCenterClick()
    fun onForgetToothbrushClick()
}
