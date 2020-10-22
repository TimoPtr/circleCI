package cn.colgate.colgateconnect;

import android.app.Activity;
import android.app.Application;
import android.view.View;
import androidx.lifecycle.Lifecycle;
import cn.colgate.colgateconnect.di.DaggerDemoAppComponent;
import cn.colgate.colgateconnect.di.DemoAppComponent;
import cn.colgate.colgateconnect.initializers.FlipperInitializer;
import cn.colgate.colgateconnect.wxapi.WXApiManager;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.kolibree.android.angleandspeed.speedcontrol.mvi.SpeedControlTranslationKey;
import com.kolibree.android.angleandspeed.testangles.mvi.TestAnglesTranslationKey;
import com.kolibree.android.app.dagger.HasViewInjector;
import com.kolibree.android.app.dagger.ViewInjector;
import com.kolibree.android.coachplus.mvi.CoachPlusTranslationKey;
import com.kolibree.android.feature.FeatureToggle;
import com.kolibree.android.jaws.coach.CoachPlusView;
import com.kolibree.android.jaws.color.ColorJawsView;
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsView;
import com.kolibree.android.logging.KLTimberTree;
import com.kolibree.android.sba.testbrushing.TestBrushingTranslationKey;
import com.kolibree.android.tracker.Analytics;
import com.kolibree.android.tracker.EventTracker;
import com.kolibree.android.tracker.EventTrackerLifecycleCallbacks;
import com.kolibree.android.translationssupport.Translations;
import com.kolibree.android.translationssupport.TranslationsProvider;
import com.kolibree.core.dagger.CoreSDK;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

/**
 * The custom app should implement HasActivityInjector and HasServiceInjector in order to be able to
 * inject dependencies into the SDK
 */
public class MainApp extends Application implements HasAndroidInjector, HasViewInjector {

  public static DemoAppComponent demoAppComponent;

  @Inject DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

  @Inject CoreSDK coreSDK;

  @Inject Set<FeatureToggle<?>> featureToggles;

  @Inject Consumer<Throwable> errorHandler;

  @Inject EventTracker eventTracker;

  @Inject FlipperInitializer flipperInitializer;

  @Override
  public void onCreate() {
    super.onCreate();
    AndroidThreeTen.init(this);

    initDagger();

    // App won't crash on undeliverable RX exceptions.
    // Has to be called after Dagger initialization
    RxJavaPlugins.setErrorHandler(errorHandler);

    Timber.plant(KLTimberTree.create(BuildConfig.DEBUG));

    overrideTranslations();

    configurePushAgent();

    initWeChatApi();

    initEventTracker();

    flipperInitializer.initialize(this);
  }

  private void initDagger() {
    demoAppComponent =
        DaggerDemoAppComponent.builder()
            .context(this)
            .initSDK(CoreSDK.init(this)) // the SDK needs to know about your current context
            .build();

    demoAppComponent.inject(this);
  }

  private void initWeChatApi() {
    WXApiManager.getInstance().setupWXApi(this);
  }

  private void initEventTracker() {
    registerActivityLifecycleCallbacks(
        new EventTrackerLifecycleCallbacks() {
          @Override
          public void setScreenName(Activity activity, String screenName) {
            eventTracker.setCurrentScreen(activity, screenName);
          }

          @Override
          public void unregisterEventTracker(@NotNull Lifecycle lifecycle) {
            lifecycle.removeObserver(eventTracker);
          }

          @Override
          public void registerEventTracker(@NotNull Lifecycle lifecycle) {
            lifecycle.addObserver(eventTracker);
            Analytics.init(eventTracker);
          }
        });
  }

  private void configurePushAgent() {
    UMConfigure.setLogEnabled(true);
    UMConfigure.init(
        this,
        "5c20a93af1f5564b73000046",
        "offical",
        UMConfigure.DEVICE_TYPE_PHONE,
        "0a3b626ae5ac626a68c30387fc9e308f");

    PushAgent pushAgent = PushAgent.getInstance(this);
    pushAgent.register(
        new IUmengRegisterCallback() {
          @Override
          public void onSuccess(String s) {
            Timber.i("push注册成功：deviceToken：--------> %s", s);
          }

          @Override
          public void onFailure(String s, String s1) {
            Timber.i("push注册失败：deviceToken：--------> %s %s", s, s1);
          }
        });
    /*pushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);*/
  }

  @Override
  public DispatchingAndroidInjector<Object> androidInjector() {
    return dispatchingAndroidInjector;
  }

  @NotNull
  @Override
  public <T extends View> ViewInjector<T> viewInjector(@NotNull Class<T> clazz) {
    // NOTE: we need to return injector for every class we want to support, wrapping up app
    // component
    if (CoachPlusView.class.equals(clazz)) {
      return view -> demoAppComponent.inject((CoachPlusView) view);
    }

    if (ColorJawsView.class.equals(clazz)) {
      return view -> demoAppComponent.inject((ColorJawsView) view);
    }

    if (GuidedBrushingJawsView.class.equals(clazz)) {
      return view -> demoAppComponent.inject((GuidedBrushingJawsView) view);
    }

    return view -> {
      // , avoid NPE, we return an empty injector here
    };
  }

  private void overrideTranslations() {
    TranslationsProvider translationsProvider = new TranslationsProvider();

    translationsProvider.addLanguageSupport(
        // This overrides translations for US locale only.
        // If you need to override translations for other languages,
        // I need to call addLanguageSupport for every language you need.
        Locale.US,
        new HashMap<Integer, String>() {
          {
            // See CoachPlusTranslationKey class for list of customizable keys
            // for Coach+
            put(CoachPlusTranslationKey.TITLE, "Demo Coach+");

            // See TestAnglesTranslationKey class for list of customizable keys
            // for Test Angles
            put(TestAnglesTranslationKey.INTRO_HEADER, "Demo Test Angles");

            // See TestAnglesTranslationKey class for list of customizable keys
            // for Speed Control
            put(SpeedControlTranslationKey.INTRO_HEADER, "Demo Speed Control");

            // See TestBrushingTranslationKey class for list of customizable keys
            // for Test Brushing
            put(TestBrushingTranslationKey.INTRO_BRUSHING_TIP, "Demo Test Brushing");
          }
        });

    Translations.init(this, translationsProvider);
  }
}
