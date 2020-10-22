package cn.colgate.colgateconnect.modules;

import android.annotation.SuppressLint;
import cn.colgate.colgateconnect.auth.AuthenticationFlowActivity;
import com.kolibree.android.app.dagger.scopes.ActivityScope;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@SuppressLint("SdkPublicClassInNonKolibreePackage")
@Module(includes = SmsAccountModule.class)
public abstract class AuthenticationFlowModule {

  @ActivityScope
  @ContributesAndroidInjector(modules = AuthFragmentsModule.class)
  abstract AuthenticationFlowActivity bindAuthenticationFlowActivity();
}
