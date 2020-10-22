package cn.colgate.colgateconnect.di;

import cn.colgate.colgateconnect.BuildConfig;
import cn.colgate.colgateconnect.R;
import com.kolibree.core.dagger.CoreDependenciesModule;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;

/** Module that provide your client_id and client_secret to the SDK */
@Module
public abstract class CredentialsModule {

  /**
   * Get client ID resource from the strings.xml file according the right environment
   *
   * @return the client ID, resID format
   */
  @Provides
  @Named(CoreDependenciesModule.DI_CLIENT_ID_RES)
  static Integer providesClientIdRes() {
    return R.string.new_client_id;
  }

  /**
   * Get client Secret resource from the strings.xml file according the right environment
   *
   * @return the client secret, resId format
   */
  @Provides
  @Named(CoreDependenciesModule.DI_CLIENT_SECRET_RES)
  static Integer providesClientSecretRes() {
    return R.string.new_client_secret;
  }

  @Provides
  @Named(CoreDependenciesModule.DI_CLIENT_PROD)
  static Boolean isInProd() {
    return BuildConfig.PRODUCTION;
  }
}
