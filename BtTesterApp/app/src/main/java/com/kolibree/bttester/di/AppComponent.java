package com.kolibree.bttester.di;

import android.content.Context;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.app.dagger.BaseUIModule;
import com.kolibree.android.app.dagger.CommonsAndroidModule;
import com.kolibree.android.processedbrushings.ProcessedBrushingsModule;
import com.kolibree.android.sdk.dagger.SdkComponent;
import com.kolibree.android.synchronizator.SynchronizatorModule;
import com.kolibree.bttester.App;
import com.kolibree.pairing.PairingModule;
import com.kolibree.statsoffline.StatsOfflineModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

/** Created by miguelaragues on 23/11/17. */
@AppScope
@Component(
    dependencies = SdkComponent.class,
    modules = {
      AndroidSupportInjectionModule.class,
      BindingModule.class,
      KolibreeModule.class,
      AppModule.class,
      BtTesterApiSDKModule.class,
      ProcessedBrushingsModule.class,
      SynchronizatorModule.class,
      PairingModule.class,
      BaseUIModule.class,
      CommonsAndroidModule.class,
      StatsOfflineModule.class
    })
public interface AppComponent {

  Context context();

  SdkComponent sdkComponent();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder context(Context context);

    Builder sdkComponent(SdkComponent sdkComponent);

    AppComponent build();
  }

  void inject(App app);
}
