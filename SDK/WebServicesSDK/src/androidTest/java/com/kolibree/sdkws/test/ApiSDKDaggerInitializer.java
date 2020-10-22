/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.test;

import android.content.Context;
import androidx.annotation.NonNull;
import com.kolibree.sdkws.test.dagger.DaggerWebServicesSDKEspressoComponent;
import com.kolibree.sdkws.test.dagger.WebServicesSDKEspressoComponent;

/** Created by miguelaragues on 14/3/18. */
class ApiSDKDaggerInitializer {

  private ApiSDKDaggerInitializer() {}

  static WebServicesSDKEspressoComponent initialize(@NonNull Context context) {
    return DaggerWebServicesSDKEspressoComponent.builder().context(context).build();
  }
}
