/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup

import android.content.Context
import com.kolibree.android.app.ui.home.tab.home.card.lastbrushing.BrushingType
import com.kolibree.android.extensions.toCurrentTimeZone
import com.kolibree.android.game.Game
import com.kolibree.android.homeui.hum.R
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

/** Common checkup formatting utilities */
internal object CheckupUtils {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    @JvmStatic
    fun formatBrushingDate(
        context: Context,
        brushingDate: OffsetDateTime,
        type: BrushingType
    ): String {
        val brushingType = brushingTypeDescription(context, type)
        val dateInCurrentTZ = brushingDate.toCurrentTimeZone()
        val date = dateFormatter.format(dateInCurrentTZ)
        val time = timeFormatter.format(dateInCurrentTZ)
        return context.getString(
            R.string.last_brushing_card_brushing_description,
            brushingType,
            date,
            time
        )
    }

    fun brushingType(game: String?) = when {
        game == null -> BrushingType.None
        Game.lookup(game) == Game.OFFLINE -> BrushingType.OfflineBrushing
        Game.lookup(game) == Game.TEST_BRUSHING -> BrushingType.TestBrushing
        else -> BrushingType.GuidedBrushing
    }

    private fun brushingTypeDescription(context: Context, type: BrushingType) = when (type) {
        BrushingType.OfflineBrushing ->
            context.getString(R.string.last_brushing_card_offline_brushing)
        BrushingType.GuidedBrushing ->
            context.getString(R.string.last_brushing_card_guided_brushing)
        BrushingType.TestBrushing ->
            context.getString(R.string.last_brushing_card_test_brushing)
        else -> ""
    }
}
