/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.app.ui.di.PulsingDotToggleModule
import com.kolibree.android.app.ui.home.mindyourspeed.startscreen.di.MindYourSpeedToggleModule
import com.kolibree.android.app.ui.home.tab.activities.card.games.GameCardToggleModule
import com.kolibree.android.app.ui.ota.feature.AlwaysOfferOtaUpdateModule
import com.kolibree.android.guidedbrushing.di.GuidedBrushingTipsToggleModule
import com.kolibree.android.headspace.di.HeadspaceFeatureToggleModule
import com.kolibree.android.network.NetworkLogToggleModule
import com.kolibree.android.offlinebrushings.sync.job.NightsWatchOfflineBrushingsCheckerToggleModule
import com.kolibree.android.rewards.morewaystoearnpoints.di.MoreWaysToEarnPointsFeatureModule
import com.kolibree.android.sdk.dagger.ToothbrushSdkFeatureToggles
import com.kolibree.android.shop.di.GooglePayToggleModule
import com.kolibree.android.shop.di.ShopFeatureToggleModule
import dagger.Module

/*
Every time we add a new ToggleModule, we need to update DaggerFeatureToggleTest
 */
@Module(
    includes = [
        NightsWatchOfflineBrushingsCheckerToggleModule::class,
        NetworkLogToggleModule::class,
        AlwaysOfferOtaUpdateModule::class,
        ShopFeatureToggleModule::class,
        ToothbrushSdkFeatureToggles::class,
        PulsingDotToggleModule::class,
        GooglePayToggleModule::class,
        MindYourSpeedToggleModule::class,
        MoreWaysToEarnPointsFeatureModule::class,
        HeadspaceFeatureToggleModule::class,
        GuidedBrushingTipsToggleModule::class,
        GameCardToggleModule::class
    ]
)
internal object FeatureToggleModule
