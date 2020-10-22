/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.di;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.app.dagger.ApplicationContext;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.commons.ExceptionLogger;
import com.kolibree.android.commons.NoOpExceptionLogger;
import com.kolibree.android.error.KolibreeRxErrorHandler;
import com.kolibree.android.network.environment.Credentials;
import com.kolibree.android.network.environment.DefaultEnvironment;
import com.kolibree.android.network.environment.Environment;
import com.kolibree.android.processedbrushings.ProcessedBrushingsModule;
import com.kolibree.android.synchronizator.SynchronizatorModule;
import com.kolibree.android.unity.core.UnityCredentials;
import com.kolibree.crypto.CryptoUtilsKt;
import com.kolibree.crypto.KolibreeGuard;
import com.kolibree.sdkws.appdata.AppDataModule;
import com.kolibree.sdkws.appdata.AppDataSyncEnabled;
import com.kolibree.sdkws.core.AvatarCache;
import com.kolibree.sdkws.core.NoOpAvatarCache;
import com.kolibree.sdkws.di.ApiSDKModule;
import com.kolibree.statsoffline.StatsOfflineModule;
import dagger.Module;
import dagger.Provides;
import io.reactivex.functions.Consumer;
import org.threeten.bp.Clock;

@Module(
    includes = {
      ApiSDKModule.class,
      ProcessedBrushingsModule.class,
      SynchronizatorModule.class,
      AppDataModule.class,
      StatsOfflineModule.class
    })
public class UnityAndroidSdkModule {

  @Provides
  @AppScope
  static Credentials providesCredentials(
      Context context, KolibreeGuard kolibreeGuard, UnityCredentials credentials) {
    try {
      byte[] iv = CryptoUtilsKt.extractHexToByteArray(credentials.getClientIv());
      return new Credentials(
          credentials.getClientId(), kolibreeGuard.reveal(credentials.getClientSecret(), iv));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("The client secret is not encrypted, please see the readme file");
    }
  }

  @Provides
  static DefaultEnvironment providesDefaultEnvironment(UnityCredentials credentials) {
    return new DefaultEnvironment(getEnvironmentAsEnum(credentials.getEnvironment()));
  }

  private static Environment getEnvironmentAsEnum(String environment) {
    for (Environment env : Environment.values()) {
      if (env.toString().equalsIgnoreCase(environment)) {
        return env;
      }
    }
    // If no valid environment is provided, the default environment is STAGING
    return Environment.STAGING;
  }

  @Provides
  static JobScheduler providesJobScheduler(Context context) {
    return (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
  }

  @Provides
  static Clock provideUTCClock() {
    return TrustedClock.getUtcClock();
  }

  @Provides
  static Consumer<Throwable> provideRxErrorHandler() {
    return new KolibreeRxErrorHandler();
  }

  @Provides
  static SharedPreferences providesSharedPreferences(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  @Provides
  @AppDataSyncEnabled
  static boolean providesIsAppDataSyncEnabled() {
    return true;
  }

  @Provides
  static ApplicationContext providesApplicationContext(Context context) {
    return new ApplicationContext(context);
  }

  @Provides
  AvatarCache providesAvatarCache() {
    return NoOpAvatarCache.INSTANCE;
  }

  @Provides
  static ExceptionLogger providesExceptionLogger() {
    return NoOpExceptionLogger.INSTANCE;
  }
}
