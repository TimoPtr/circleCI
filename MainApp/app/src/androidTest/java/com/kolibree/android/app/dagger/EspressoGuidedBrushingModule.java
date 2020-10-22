/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger;

import com.kolibree.android.app.dagger.scopes.ActivityScope;
import com.kolibree.android.app.disconnection.LostConnectionModule;
import com.kolibree.android.coachplus.di.CoachPlusActivityLogicModule;
import com.kolibree.android.coachplus.di.CoachPlusControllerInternalModule;
import com.kolibree.android.coachplus.di.CoachPlusLogicModule;
import com.kolibree.android.coachplus.settings.CoachCommonSettingsModule;
import com.kolibree.android.game.BrushingCreatorModule;
import com.kolibree.android.game.GameScope;
import com.kolibree.android.game.bi.AvroCreatorModule;
import com.kolibree.android.guidedbrushing.di.GuidedBrushingActivityInternalModule;
import com.kolibree.android.guidedbrushing.di.GuidedBrushingFactoryModule;
import com.kolibree.android.guidedbrushing.di.PlaqlessSupervisionFeatureToggle;
import com.kolibree.android.guidedbrushing.mvi.GuidedBrushingActivity;
import com.kolibree.android.sdk.connection.brushingmode.ConfirmBrushingModeModule;
import com.kolibree.android.sdk.util.KpiSpeedProviderModule;
import com.kolibree.android.sdk.util.RnnWeightProviderModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(
    includes = {
      CoachCommonSettingsModule.class,
      GuidedBrushingFactoryModule.class,
      PlaqlessSupervisionFeatureToggle.class
    })
public abstract class EspressoGuidedBrushingModule {

  @ActivityScope
  @GameScope
  @ContributesAndroidInjector(
      modules = {
        CoachPlusLogicModule.class,
        EspressoGuidedBrushingActivityModule.class,
        CoachPlusActivityLogicModule.class,
        LostConnectionModule.class
      })
  abstract GuidedBrushingActivity bindGuidedBrushingActivity();
}

@Module(
    includes = {
      GuidedBrushingActivityInternalModule.class,
      ConfirmBrushingModeModule.class,
      CoachPlusControllerInternalModule.class,
      RnnWeightProviderModule.class,
      KpiSpeedProviderModule.class,
      AvroCreatorModule.class,
      BrushingCreatorModule.class
    })
abstract class EspressoGuidedBrushingActivityModule {
  // no-op
}
