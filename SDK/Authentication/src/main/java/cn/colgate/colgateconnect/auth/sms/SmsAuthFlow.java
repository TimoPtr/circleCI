package cn.colgate.colgateconnect.auth.sms;

import android.annotation.SuppressLint;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.sdkws.sms.SmsToken;
import io.reactivex.Single;
import java.util.List;

@SuppressLint({"SdkPublicClassInNonKolibreePackage", "DeobfuscatedPublicSdkClass"})
public interface SmsAuthFlow {

  Single<List<IProfile>> execute(SmsToken token, String code);
}
