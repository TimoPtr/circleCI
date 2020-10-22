package com.kolibree.bttester;

import android.app.Application;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.kolibree.android.sdk.KolibreeAndroidSdk;
import com.kolibree.android.sdk.core.NeverScanBeforeReconnectStrategy;
import com.kolibree.android.sdk.dagger.SdkComponent;
import com.kolibree.bttester.di.AppComponent;
import com.kolibree.bttester.di.DaggerAppComponent;
import com.kolibree.bttester.utils.DebugLoggingTree;
import com.kolibree.kml.Kml;
import com.uber.rxdogtag.RxDogTag;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import javax.inject.Inject;
import timber.log.FileLoggingTree;
import timber.log.Timber;

/** Created by miguelaragues on 23/11/17. */
public class App extends Application implements HasAndroidInjector {

  public static AppComponent appComponent;
  @Inject DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

  @Inject Consumer<Throwable> errorHandler;

  @Override
  public DispatchingAndroidInjector<Object> androidInjector() {
    return dispatchingAndroidInjector;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    initKml();

    RxJavaPlugins.setErrorHandler(errorHandler);

    RxDogTag.builder().install();

    initDagger();

    initTimezones();

    Timber.plant(new DebugLoggingTree());
    Timber.plant(new FileLoggingTree());

    RxJavaPlugins.setErrorHandler(Timber::wtf);
  }

  private void initKml() {
    Kml.INSTANCE.init();
  }

  private void initDagger() {
    SdkComponent sdkComponent =
        KolibreeAndroidSdk.init(this, null, NeverScanBeforeReconnectStrategy.INSTANCE);

    appComponent = DaggerAppComponent.builder().context(this).sdkComponent(sdkComponent).build();

    appComponent.inject(this);
  }

  protected void initTimezones() {
    AndroidThreeTen.init(this);
  }
}
