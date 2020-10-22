/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.di

import com.kolibree.android.feature.AlwaysOfferOtaUpdateFeature
import com.kolibree.android.feature.AmazonDashFeature
import com.kolibree.android.feature.CoachPlusPlaqlessSupervisionFeature
import com.kolibree.android.feature.ConvertB1ToHumBatteryFeature
import com.kolibree.android.feature.ConvertCe2ToHumElectricFeature
import com.kolibree.android.feature.ConvertCe2ToPlaqlessFeature
import com.kolibree.android.feature.GooglePayFeature
import com.kolibree.android.feature.GooglePayProductionEnvironmentFeature
import com.kolibree.android.feature.GuidedBrushingTipsFeature
import com.kolibree.android.feature.HeadspaceFeature
import com.kolibree.android.feature.MarkAccountAsBetaFeature
import com.kolibree.android.feature.MindYourSpeedHideDotFeature
import com.kolibree.android.feature.MindYourSpeedSnapDotFeature
import com.kolibree.android.feature.OfflineBrushingsNotificationsFeature
import com.kolibree.android.feature.PulsingDotFeature
import com.kolibree.android.feature.ShowAllMoreWaysCardsFeature
import com.kolibree.android.feature.ShowGamesCardFeature
import com.kolibree.android.feature.ShowMindYourSpeedFeature
import com.kolibree.android.feature.ShowShopTabsFeature
import com.kolibree.android.feature.StatsOfflineFeature
import com.kolibree.android.feature.UseTestShopFeature
import com.kolibree.android.network.NetworkLogFeature
import com.kolibree.android.processedbrushings.CheckupGoalDurationConfigurationFeature

class ColgateDaggerFeatureToggleTest : DaggerFeatureToggleTest() {

    override val expectedPersistentFeatures = setOf(
        OfflineBrushingsNotificationsFeature,
        AlwaysOfferOtaUpdateFeature,
        ConvertCe2ToPlaqlessFeature,
        CheckupGoalDurationConfigurationFeature,
        UseTestShopFeature,
        CoachPlusPlaqlessSupervisionFeature,
        ConvertB1ToHumBatteryFeature,
        ConvertCe2ToHumElectricFeature,
        PulsingDotFeature,
        GooglePayFeature,
        ShowMindYourSpeedFeature,
        MindYourSpeedHideDotFeature,
        MindYourSpeedSnapDotFeature,
        ShowAllMoreWaysCardsFeature,
        AmazonDashFeature,
        ShowShopTabsFeature,
        GuidedBrushingTipsFeature,
        ShowGamesCardFeature
    )

    override val constantFeatures = setOf(
        StatsOfflineFeature,
        HeadspaceFeature
    )

    override val buildTypeDependentFeatures = setOf(
        NetworkLogFeature,
        GooglePayProductionEnvironmentFeature
    )

    override val transientFeatures = setOf(
        MarkAccountAsBetaFeature
    )
}
