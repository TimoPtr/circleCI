package cn.colgate.colgateconnect.auth.sms;

import cn.colgate.colgateconnect.auth.sms.create.CreateAccountBySmsFlow;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.sdkws.sms.SmsAccountManager;
import com.kolibree.sdkws.sms.SmsToken;
import com.kolibree.sdkws.sms.data.AccountData;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class CreateAccountBySmsFlowTest extends BaseUnitTest {

  @Mock SmsAccountManager smsAccountManager;

  @Mock AccountData data;

  CreateAccountBySmsFlow smsFlow;

  @Override
  public void setup() throws Exception {
    super.setup();

    smsFlow = new CreateAccountBySmsFlow(smsAccountManager, data);
  }

  @Test
  public void execute_invokesCreateAccount() throws Exception {
    SmsToken token = new SmsToken("+123", "11aa22");
    String code = "123456";
    smsFlow.execute(token, code);
    Mockito.verify(smsAccountManager).createAccount(token, code, data);
  }
}
