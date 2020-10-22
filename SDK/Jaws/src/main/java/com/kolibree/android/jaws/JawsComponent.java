/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.jaws;

import android.content.Context;
import androidx.annotation.Keep;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.app.dagger.CommonsAndroidModule;
import com.kolibree.android.jaws.color.ColorJawsModule;
import com.kolibree.android.processedbrushings.ProcessedBrushingsModule;
import dagger.BindsInstance;
import dagger.Component;

@AppScope
@Component(
    modules = {
      CommonsAndroidModule.class,
      ProcessedBrushingsModule.class,
      JawsModule.class,
      ColorJawsModule.class
    })
@Keep
public interface JawsComponent {

  @Keep
  @Component.Builder
  interface Builder {

    JawsComponent build();

    @BindsInstance
    Builder context(Context context);
  }
}
