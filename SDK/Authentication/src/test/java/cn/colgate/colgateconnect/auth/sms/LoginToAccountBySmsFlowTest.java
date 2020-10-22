package cn.colgate.colgateconnect.auth.sms;

import cn.colgate.colgateconnect.auth.sms.login.LoginToAccountBySmsFlow;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.sdkws.sms.SmsAccountManager;
import com.kolibree.sdkws.sms.SmsToken;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class LoginToAccountBySmsFlowTest extends BaseUnitTest {

  @Mock SmsAccountManager smsAccountManager;

  LoginToAccountBySmsFlow smsFlow;

  @Override
  public void setup() throws Exception {
    super.setup();

    smsFlow = new LoginToAccountBySmsFlow(smsAccountManager);
  }

  @Test
  public void execute_invokesLoginToAccount() throws Exception {
    SmsToken token = new SmsToken("+123", "11aa22");
    String code = "123456";
    smsFlow.execute(token, code);
    Mockito.verify(smsAccountManager).loginToAccount(token, code);
  }
}
