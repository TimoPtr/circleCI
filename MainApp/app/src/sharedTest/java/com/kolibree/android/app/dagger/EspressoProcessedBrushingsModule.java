/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger;

import static com.nhaarman.mockitokotlin2.SpyingKt.spy;

import com.kolibree.android.processedbrushings.CheckupCalculator;
import com.kolibree.android.processedbrushings.CheckupCalculatorImpl;
import com.kolibree.android.processedbrushings.ProcessedBrushingsBaseModule;
import dagger.Module;
import dagger.Provides;

@Module(includes = ProcessedBrushingsBaseModule.class)
public abstract class EspressoProcessedBrushingsModule {

  private static volatile CheckupCalculator checkupCalculatorSingleton;

  // Using @Singleton here leads to scope issues
  @SuppressWarnings("KotlinInternalInJava")
  @Provides
  static CheckupCalculator provideCheckupCalculator(CheckupCalculatorImpl checkupCalculator) {
    if (checkupCalculatorSingleton == null) {
      synchronized (EspressoProcessedBrushingsModule.class) {
        if (checkupCalculatorSingleton == null) {
          checkupCalculatorSingleton = spy(checkupCalculator);
        }
      }
    }
    return checkupCalculatorSingleton;
  }
}
