/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger;

import com.kolibree.account.di.AccountModule;
import com.kolibree.android.app.AppInitializerModule;
import com.kolibree.android.app.job.di.WorkerModule;
import com.kolibree.android.app.migration.di.AppMigrationModule;
import com.kolibree.android.app.test.EspressoJawsModule;
import com.kolibree.android.app.ui.kolibree_pro.EspressoKolibreeProRemindersModule;
import com.kolibree.android.app.ui.notification.NotificationPresenterModule;
import com.kolibree.android.brushingquiz.di.BrushingProgramModule;
import com.kolibree.android.brushreminder.di.BrushReminderModule;
import com.kolibree.android.guidedbrushing.di.GuidedBrushingTipsModule;
import com.kolibree.android.offlinebrushings.persistence.EspressoOfflineBrushingsModule;
import com.kolibree.android.sba.testbrushing.EspressoAccountInternalModule;
import com.kolibree.android.test.dagger.EspressoAndroidConfigModule;
import com.kolibree.android.test.dagger.EspressoAnimationInfoProviderModule;
import com.kolibree.android.test.dagger.EspressoAssetBundleModule;
import com.kolibree.android.test.dagger.EspressoCalendarLogicModule;
import com.kolibree.android.test.dagger.EspressoEventTrackerModule;
import com.kolibree.android.test.dagger.EspressoGameModule;
import com.kolibree.android.test.dagger.EspressoKolibreeModule;
import com.kolibree.android.test.dagger.EspressoModelsAvailableModule;
import com.kolibree.android.test.dagger.EspressoPushNotificationModule;
import com.kolibree.android.test.dagger.EspressoQuestionOfTheDayModule;
import com.kolibree.android.test.dagger.EspressoRewardsModule;
import com.kolibree.android.test.dagger.EspressoSdkComponent;
import com.kolibree.android.test.dagger.EspressoSecurityKeeperModule;
import com.kolibree.android.test.dagger.EspressoStatsModule;
import com.kolibree.android.test.dagger.EspressoStatsOfflineModule;
import com.kolibree.android.test.dagger.EspressoSynchronizatorModule;
import com.kolibree.android.test.dagger.EspressoUnityGameModule;
import com.kolibree.android.test.dagger.EspressoUtilsModule;
import com.kolibree.android.test.dagger.EspressoWebServicesSdkModule;
import com.kolibree.pairing.persistence.module.EspressoPairingModule;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

/** Created by miguelaragues on 5/2/18. */
@AppScope
@Component(
    dependencies = {EspressoSdkComponent.class},
    modules = {
      AndroidSupportInjectionModule.class,
      EspressoFeatureToggleModule.class,
      EspressoUiModule.class,
      EspressoBindingModule.class,
      EspressoAppModule.class,
      EspressoForceLogoutIntentModule.class,
      EspressoKolibreeModule.class,
      EspressoRepositoriesModule.class,
      EspressoOfflineBrushingsModule.class,
      EspressoWebServicesSdkModule.class,
      EspressoEventTrackerModule.class,
      EspressoModelsAvailableModule.class,
      EspressoJawsModule.class,
      EspressoUtilsModule.class,
      EspressoStatsModule.class,
      EspressoPairingModule.class,
      EspressoGuidedBrushingModule.class,
      EspressoKolibreeProRemindersModule.class,
      EspressoProcessedBrushingsModule.class,
      EspressoRewardsModule.class,
      EspressoSynchronizatorModule.class,
      EspressoAccountInternalModule.class,
      EspressoCommonsAndroidModule.class,
      EspressoAssetBundleModule.class,
      EspressoUnityGameModule.class,
      EspressoAnimationInfoProviderModule.class,
      EspressoAndroidConfigModule.class,
      EspressoStatsOfflineModule.class,
      AccountModule.class,
      EspressoSecurityKeeperModule.class,
      BrushingProgramModule.class,
      EspressoPushNotificationModule.class,
      EspressoDataBindingModule.class,
      EspressoShopDataModule.class,
      EspressoGameModule.class,
      EspressoCalendarLogicModule.class,
      EspressoHumChallengeModule.class,
      EspressoQuestionOfTheDayModule.class,
      WorkerModule.class,
      EspressoSingleThreadSchedulerModule.class,
      AppInitializerModule.class,
      FlipperModule.class,
      EspressoBrushSyncReminderModule.class,
      NotificationPresenterModule.class,
      EspressoAmazonDashModule.class,
      EspressoBatteryLevelModule.class,
      BrushReminderModule.class,
      EspressoPartnershipModule.class,
      AppMigrationModule.class,
      GuidedBrushingTipsModule.class,
      EspressoHeadspaceMindfulMomentModule.class
    })
public interface EspressoAppComponent extends BaseEspressoAppComponent {

  @Component.Builder
  interface Builder extends BaseBuilder<Builder> {

    EspressoAppComponent build();
  }
}
