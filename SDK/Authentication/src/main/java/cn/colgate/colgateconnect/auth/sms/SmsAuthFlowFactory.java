package cn.colgate.colgateconnect.auth.sms;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.colgate.colgateconnect.auth.sms.create.CreateAccountBySmsFlow;
import cn.colgate.colgateconnect.auth.sms.login.LoginToAccountBySmsFlow;
import com.kolibree.sdkws.sms.SmsAccountManager;
import com.kolibree.sdkws.sms.data.AccountData;

@SuppressLint({"SdkPublicClassInNonKolibreePackage", "DeobfuscatedPublicSdkClass"})
public class SmsAuthFlowFactory {

  public static SmsAuthFlow create(
      @NonNull SmsAccountManager manager, @Nullable AccountData accountData) {
    if (accountData == null) {
      return new LoginToAccountBySmsFlow(manager);
    }
    return new CreateAccountBySmsFlow(manager, accountData);
  }
}
