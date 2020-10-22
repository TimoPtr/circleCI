/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushbetter

import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterItem.ADJUST_BRUSHING_ANGLE
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterItem.GUIDED_BRUSHING
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterItem.MIND_YOUR_SPEED
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterItem.TEST_BRUSHING
import com.kolibree.android.homeui.hum.R
import javax.inject.Inject

internal interface BrushBetterResourceProvider {
    fun createItemBinding(item: BrushBetterItem): BrushBetterItemBinding
}

internal class BrushBetterResourceProviderImpl @Inject constructor() : BrushBetterResourceProvider {

    override fun createItemBinding(item: BrushBetterItem): BrushBetterItemBinding {
        return when (item) {
            GUIDED_BRUSHING -> item.guidedBrushing()
            MIND_YOUR_SPEED -> item.mindYourSpeed()
            ADJUST_BRUSHING_ANGLE -> item.adjustBrushingAngle()
            TEST_BRUSHING -> item.testBrushing()
        }
    }

    private fun BrushBetterItem.guidedBrushing() = BrushBetterItemBinding(
        item = this,
        iconRes = R.drawable.brush_better_guided_icon,
        titleRes = R.string.brush_better_card_item_guided_brushing_title,
        bodyRes = R.string.brush_better_card_item_guided_brushing_body
    )

    private fun BrushBetterItem.mindYourSpeed() = BrushBetterItemBinding(
        item = this,
        iconRes = R.drawable.brush_better_mind_your_speed_icon,
        titleRes = R.string.brush_better_card_item_mind_your_speed_title,
        bodyRes = R.string.brush_better_card_item_mind_your_speed_body
    )

    private fun BrushBetterItem.adjustBrushingAngle() = BrushBetterItemBinding(
        item = this,
        iconRes = R.drawable.brush_better_brushing_angle_icon,
        titleRes = R.string.brush_better_card_item_adjust_brushing_angle_title,
        bodyRes = R.string.brush_better_card_item_adjust_brushing_angle_body
    )

    private fun BrushBetterItem.testBrushing() = BrushBetterItemBinding(
        item = this,
        iconRes = R.drawable.brush_better_test_brushing_icon,
        titleRes = R.string.brush_better_card_item_test_brushing_title,
        bodyRes = R.string.brush_better_card_item_test_brushing_body
    )
}
