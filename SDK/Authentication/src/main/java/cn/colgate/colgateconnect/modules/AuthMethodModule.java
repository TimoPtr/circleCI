package cn.colgate.colgateconnect.modules;

import android.annotation.SuppressLint;
import cn.colgate.colgateconnect.auth.AuthenticationFlowActivity;
import cn.colgate.colgateconnect.auth.choose.ChooseAuthMethodFragment;
import cn.colgate.colgateconnect.auth.sms.SmsAuthFlow;
import cn.colgate.colgateconnect.auth.sms.SmsAuthFlowFactory;
import cn.colgate.colgateconnect.auth.sms.SmsAuthFragment;
import com.kolibree.android.app.dagger.scopes.FragmentScope;
import com.kolibree.sdkws.core.IKolibreeConnector;
import com.kolibree.sdkws.sms.data.AccountData;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@SuppressLint("SdkPublicClassInNonKolibreePackage")
@Module
public abstract class AuthMethodModule {

  @FragmentScope
  @ContributesAndroidInjector
  abstract ChooseAuthMethodFragment contributeChooseAuthMethodFragment();

  @FragmentScope
  @ContributesAndroidInjector(modules = SmsAuthModule.class)
  abstract SmsAuthFragment contributeSmsAuthFragment();

  @Module
  abstract static class SmsAuthModule {

    @Provides
    static SmsAuthFlow providesSmsAccountFlow(
        AuthenticationFlowActivity activity, IKolibreeConnector connector) {
      AccountData data = activity.extractAccountData();
      return SmsAuthFlowFactory.create(connector, data);
    }
  }
}
