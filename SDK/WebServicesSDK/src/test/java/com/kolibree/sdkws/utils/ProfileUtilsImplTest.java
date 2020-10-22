package com.kolibree.sdkws.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.sdkws.core.IKolibreeConnector;
import com.kolibree.sdkws.core.ProfileWrapper;
import org.junit.Test;
import org.mockito.Mock;

public class ProfileUtilsImplTest extends BaseUnitTest {

  @Mock IKolibreeConnector connector;

  @Mock ProfileWrapper profile;

  @Override
  public void setup() throws Exception {
    super.setup();
    when(connector.withCurrentProfile()).thenReturn(profile);
    when(connector.hasConnectedAccount()).thenReturn(true);
  }

  @Test
  public void testIsAllowedToBrush_allowed_returnsTrue() {
    mockProfileAllowedToBrush(true);

    ProfileUtilsImpl utils = new ProfileUtilsImpl(connector);

    assertTrue(utils.isAllowedToBrush());
  }

  @Test
  public void testIsAllowedToBrush_refused_returnsFalse() {
    mockProfileAllowedToBrush(false);

    ProfileUtilsImpl utils = new ProfileUtilsImpl(connector);

    assertFalse(utils.isAllowedToBrush());
  }

  @Test
  public void testIsAllowedToBrush_returnsFalse_whenHasNoConnectedAccount() {
    mockProfileAllowedToBrush(true);
    when(connector.withCurrentProfile()).thenReturn(null);

    ProfileUtilsImpl utils = new ProfileUtilsImpl(connector);

    assertFalse(utils.isAllowedToBrush());
  }

  private void mockProfileAllowedToBrush(boolean result) {
    when(profile.isAllowedToBrush()).thenReturn(result);
  }
}
