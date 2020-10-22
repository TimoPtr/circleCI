package cn.colgate.colgateconnect.auth.sms.create;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import cn.colgate.colgateconnect.auth.sms.SmsAuthFlow;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.sdkws.sms.SmsAccountManager;
import com.kolibree.sdkws.sms.SmsToken;
import com.kolibree.sdkws.sms.data.AccountData;
import io.reactivex.Single;
import java.util.List;

@SuppressLint({"SdkPublicClassInNonKolibreePackage", "DeobfuscatedPublicSdkClass"})
public class CreateAccountBySmsFlow implements SmsAuthFlow {

  private final AccountData accountData;
  private final SmsAccountManager smsAccountManager;

  public CreateAccountBySmsFlow(
      @NonNull SmsAccountManager smsAccountManager, @NonNull AccountData accountData) {

    this.smsAccountManager = smsAccountManager;
    this.accountData = accountData;
  }

  @Override
  public Single<List<IProfile>> execute(SmsToken token, String code) {
    return smsAccountManager.createAccount(token, code, accountData);
  }
}
