package cn.colgate.colgateconnect.auth.sms.login;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import cn.colgate.colgateconnect.auth.sms.SmsAuthFlow;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.sdkws.sms.SmsAccountManager;
import com.kolibree.sdkws.sms.SmsToken;
import io.reactivex.Single;
import java.util.List;

@SuppressLint({"SdkPublicClassInNonKolibreePackage", "DeobfuscatedPublicSdkClass"})
public class LoginToAccountBySmsFlow implements SmsAuthFlow {

  private SmsAccountManager smsAccountManager;

  public LoginToAccountBySmsFlow(@NonNull SmsAccountManager smsAccountManager) {
    this.smsAccountManager = smsAccountManager;
  }

  @Override
  public Single<List<IProfile>> execute(SmsToken token, String code) {
    return smsAccountManager.loginToAccount(token, code);
  }
}
