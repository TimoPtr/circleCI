package cn.colgate.colgateconnect.di;

import android.content.Context;
import cn.colgate.colgateconnect.MainApp;
import cn.colgate.colgateconnect.dagger.VariantModule;
import cn.colgate.colgateconnect.demo.di.DemoSDKModule;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.jaws.coach.CoachPlusView;
import com.kolibree.android.jaws.color.ColorJawsView;
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsView;
import com.kolibree.android.sdk.dagger.SdkComponent;
import com.kolibree.android.synchronizator.SynchronizatorModule;
import com.kolibree.android.tracker.di.EventTrackerModule;
import com.kolibree.core.dagger.CoreModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@AppScope
@Component(
    dependencies = {SdkComponent.class},
    modules = {
      AndroidSupportInjectionModule.class, // required to be able to inject
      DemoActivityModule.class, // include the activities that will use the @Inject annotation
      DemoSDKModule.class, // dependency for this demo app ONLY
      CoreModule.class, // include the android SDK with all the modules
      CredentialsModule.class, // provide the client_id and client_secret to the SDK
      SynchronizatorModule.class,
      EventTrackerModule.class,
      AppModule.class,
      VariantModule.class
    })
public interface DemoAppComponent extends AndroidInjector<MainApp> {

  void inject(CoachPlusView coachPlusView);

  void inject(ColorJawsView view);

  void inject(GuidedBrushingJawsView view);

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder context(Context context);

    Builder initSDK(SdkComponent sdkComponent);

    DemoAppComponent build();
  }
}
