/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.test.dagger;

import android.content.Context;
import com.kolibree.android.accountinternal.account.AccountInternalModule;
import com.kolibree.android.accountinternal.account.PersistenceModule;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.test.dagger.EspressoSynchronizatorModule;
import com.kolibree.sdkws.core.IKolibreeConnector;
import com.kolibree.sdkws.core.IntegrationTestCoreModule;
import com.kolibree.sdkws.core.avro.AvroFileUploader;
import com.kolibree.sdkws.di.ApiSdkBindingModule;
import com.kolibree.sdkws.internal.OfflineUpdateDao;
import com.kolibree.sdkws.test.ApiSDKTestApp;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

/** Created by miguelaragues on 14/3/18. */
@AppScope
@Component(
    modules = {
      AndroidInjectionModule.class,
      ApiSdkIntegrationTestModule.class,
      IntegrationTestCoreModule.class,
      ApiSdkBindingModule.class,
      PersistenceModule.class,
      AccountInternalModule.class,
      EspressoSynchronizatorModule.class
    })
public interface WebServicesSDKEspressoComponent {

  void inject(ApiSDKTestApp apiSDKTestApp);

  IKolibreeConnector connector();

  AvroFileUploader avroFileUploader();

  @SuppressWarnings("KotlinInternalInJava")
  OfflineUpdateDao offlineUpdateDao();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder context(Context context);

    WebServicesSDKEspressoComponent build();
  }
}
