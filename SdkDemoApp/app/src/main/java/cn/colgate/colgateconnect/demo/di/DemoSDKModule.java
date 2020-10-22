package cn.colgate.colgateconnect.demo.di;

import android.content.SharedPreferences;
import cn.colgate.colgateconnect.demo.AccountInfo;
import cn.colgate.colgateconnect.demo.DataStore;
import com.kolibree.android.app.dagger.AppScope;
import dagger.Module;
import dagger.Provides;

@Module
/**
 * Object provided into the demo app, only needed by the demo app. Not required by the module at
 * all.
 */
public abstract class DemoSDKModule {

  @Provides
  @AppScope
  static AccountInfo providesProfileManager() {
    return new AccountInfo();
  }

  @Provides
  static DataStore providesDataStore(SharedPreferences sharedPreferences) {
    return new DataStore(sharedPreferences);
  }
}
