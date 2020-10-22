/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbox

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.annotation.AttrRes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.homeui.hum.R
import javax.inject.Inject
import kotlinx.android.parcel.Parcelize

/**
 * Provides information how toolbox should behave.
 * Not specified values won't be displayed.
 */
@VisibleForApp
@Parcelize
data class ToolboxConfiguration(
    val analyticsName: String,
    val iconRes: Int? = null,
    val subTitle: String? = null,
    val title: String? = null,
    val body: String? = null,
    val detailsButton: Button? = null,
    val confirmButton: Button? = null,
    val pulsingDotVisible: Boolean = false,
    @AttrRes val titleTextAppearance: Int = R.attr.textAppearanceHeadline3
) : Parcelable {

    @VisibleForApp
    @Parcelize
    data class Button(
        val text: String,
        val analytics: String,
        val intent: Intent? = null
    ) : Parcelable

    @VisibleForApp
    class Factory @Inject constructor(
        private val context: Context
    ) {
        @SuppressWarnings("LongMethod")
        fun smilePoints() = with(context) {
            ToolboxConfiguration(
                analyticsName = "BlackBox_SmilePoints_Open",
                iconRes = R.drawable.earning_points_icon,
                subTitle = getString(R.string.toolbox_home_smile_points_subtitle),
                title = getString(R.string.toolbox_home_smile_points_title),
                body = getString(R.string.toolbox_home_smile_points_body),
                detailsButton = Button(
                    text = getString(R.string.toolbox_details_button),
                    analytics = "BlackBox_SmilePoints_LearnMore",
                    intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.rewards_terms_url))
                    )
                ),
                confirmButton = Button(
                    text = getString(R.string.toolbox_confirm_button),
                    analytics = "BlackBox_SmilePoints_GotIt"
                )
            )
        }

        fun toolboxExplanation() = with(context) {
            ToolboxConfiguration(
                pulsingDotVisible = true,
                analyticsName = "BlackBox_MainBox_Open",
                title = getString(R.string.toolbox_home_explanation_title),
                titleTextAppearance = R.attr.textAppearanceHeadline4,
                confirmButton = Button(
                    text = getString(R.string.toolbox_confirm_button),
                    analytics = "BlackBox_MainBox"
                )
            )
        }

        fun testBrushing() = with(context) {
            ToolboxConfiguration(
                analyticsName = "BlackBox_BrushingResult_Open",
                iconRes = R.drawable.test_brushing_icon,
                subTitle = getString(R.string.toolbox_home_test_brushing_subtitle),
                title = getString(R.string.toolbox_home_test_brushing_title),
                body = getString(R.string.toolbox_home_test_brushing_body),
                confirmButton = Button(
                    text = getString(R.string.toolbox_confirm_button),
                    analytics = "BlackBox_BrushingResult_GotIt"
                )
            )
        }

        fun brushingActivities() = with(context) {
            ToolboxConfiguration(
                analyticsName = "BlackBox_BrushingActivities_Open",
                iconRes = R.drawable.brushing_activities_icon,
                subTitle = getString(R.string.toolbox_home_brushing_activities_subtitle),
                title = getString(R.string.toolbox_home_brushing_activities_title),
                body = getString(R.string.toolbox_home_brushing_activities_body),
                confirmButton = Button(
                    text = getString(R.string.toolbox_confirm_button),
                    analytics = "BlackBox_BrushingActivities_GotIt"
                )
            )
        }

        fun frequencyChart() = with(context) {
            ToolboxConfiguration(
                analyticsName = "BlackBox_FrequencyChart_Open",
                iconRes = R.drawable.frequency_chart_icon,
                subTitle = getString(R.string.toolbox_home_frequency_chart_subtitle),
                title = getString(R.string.toolbox_home_frequency_chart_title),
                body = getString(R.string.toolbox_home_frequency_chart_body),
                confirmButton = Button(
                    text = getString(R.string.toolbox_confirm_button),
                    analytics = "BlackBox_FrequencyChart_GotIt"
                )
            )
        }
    }
}
