package cn.colgate.colgateconnect.di;

import cn.colgate.colgateconnect.cache.DemoAvatarCacheWarmUp;
import com.kolibree.android.app.tracker.DemoAnalyticsTracker;
import com.kolibree.android.tracker.logic.AnalyticsTracker;
import com.kolibree.sdkws.core.AvatarCacheWarmUp;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class AppModule {

  @Binds
  abstract AnalyticsTracker bindAnalyticsTracker(DemoAnalyticsTracker impl);

  @Binds
  abstract AvatarCacheWarmUp bindAvatarCacheWarmUp(DemoAvatarCacheWarmUp impl);
}
