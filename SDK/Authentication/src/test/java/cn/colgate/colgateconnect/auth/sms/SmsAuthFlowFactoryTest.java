package cn.colgate.colgateconnect.auth.sms;

import cn.colgate.colgateconnect.auth.sms.create.CreateAccountBySmsFlow;
import cn.colgate.colgateconnect.auth.sms.login.LoginToAccountBySmsFlow;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.sdkws.sms.SmsAccountManager;
import com.kolibree.sdkws.sms.data.AccountData;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class SmsAuthFlowFactoryTest extends BaseUnitTest {

  @Mock SmsAccountManager smsAccountManager;

  @Test
  public void create_nullData_loginToAccountFlow() throws Exception {
    SmsAuthFlow flow = SmsAuthFlowFactory.create(smsAccountManager, null);
    Assert.assertTrue(flow instanceof LoginToAccountBySmsFlow);
  }

  @Test
  public void create_notNullData_createAccountFlow() throws Exception {
    AccountData data = Mockito.mock(AccountData.class);
    SmsAuthFlow flow = SmsAuthFlowFactory.create(smsAccountManager, data);
    Assert.assertTrue(flow instanceof CreateAccountBySmsFlow);
  }
}
