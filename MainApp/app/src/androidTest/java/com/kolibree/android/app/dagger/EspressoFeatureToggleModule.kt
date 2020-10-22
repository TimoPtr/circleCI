/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.content.Context
import com.kolibree.android.feature.AlwaysOfferOtaUpdateFeature
import com.kolibree.android.feature.AmazonDashFeature
import com.kolibree.android.feature.ConvertB1ToHumBatteryFeature
import com.kolibree.android.feature.ConvertCe2ToHumElectricFeature
import com.kolibree.android.feature.ConvertCe2ToPlaqlessFeature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.GooglePayFeature
import com.kolibree.android.feature.GooglePayProductionEnvironmentFeature
import com.kolibree.android.feature.GuidedBrushingTipsFeature
import com.kolibree.android.feature.HeadspaceFeature
import com.kolibree.android.feature.MindYourSpeedHideDotFeature
import com.kolibree.android.feature.MindYourSpeedSnapDotFeature
import com.kolibree.android.feature.PulsingDotFeature
import com.kolibree.android.feature.ShowAllMoreWaysCardsFeature
import com.kolibree.android.feature.ShowGamesCardFeature
import com.kolibree.android.feature.ShowMindYourSpeedFeature
import com.kolibree.android.feature.ShowOrphanBrushingsInCheckupFeature
import com.kolibree.android.feature.ShowPlaqlessVersionOfViewsFeature
import com.kolibree.android.feature.ShowShopTabsFeature
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.android.network.NetworkLogFeatureToggle
import com.kolibree.android.offlinebrushings.sync.job.NightsWatchOfflineBrushingsCheckerToggleModule
import com.kolibree.crypto.SecurityKeeper
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet

// TODO make values settable via Component.Builder and replace TransientFeatureToggle with ConstantFeatureToggle
@Module(
    includes = [
        NightsWatchOfflineBrushingsCheckerToggleModule::class
    ]
)
class EspressoFeatureToggleModule {

    @Provides
    @ElementsIntoSet
    fun provideFeatureToggles(
        context: Context,
        securityKeeper: SecurityKeeper
    ): Set<FeatureToggle<*>> {
        return setOf(
            // TODO make use of non-persistent toggles
            PersistentFeatureToggle(context, ConvertCe2ToPlaqlessFeature),
            PersistentFeatureToggle(context, ShowOrphanBrushingsInCheckupFeature),
            PersistentFeatureToggle(context, AlwaysOfferOtaUpdateFeature),
            NetworkLogFeatureToggle.newInstance(context, securityKeeper),
            PersistentFeatureToggle(context, ShowPlaqlessVersionOfViewsFeature),
            ConstantFeatureToggle(GooglePayProductionEnvironmentFeature, initialValue = false),
            PersistentFeatureToggle(context, ConvertCe2ToHumElectricFeature),
            PersistentFeatureToggle(context, ConvertB1ToHumBatteryFeature),
            PersistentFeatureToggle(context, PulsingDotFeature),
            PersistentFeatureToggle(context, GooglePayFeature),
            PersistentFeatureToggle(context, ShowMindYourSpeedFeature),
            PersistentFeatureToggle(context, MindYourSpeedHideDotFeature),
            PersistentFeatureToggle(context, MindYourSpeedSnapDotFeature),
            PersistentFeatureToggle(context, ShowAllMoreWaysCardsFeature),
            PersistentFeatureToggle(context, AmazonDashFeature),
            PersistentFeatureToggle(context, ShowShopTabsFeature),
            PersistentFeatureToggle(context, ShowGamesCardFeature),
            ConstantFeatureToggle(HeadspaceFeature, initialValue = true),
            ConstantFeatureToggle(GuidedBrushingTipsFeature)
        )
    }
}
