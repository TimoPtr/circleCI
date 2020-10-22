package com.kolibree.android.test.utils;

import static org.mockito.Mockito.when;

import com.kolibree.android.accountinternal.profile.models.Profile;
import com.kolibree.android.app.App;
import com.kolibree.sdkws.core.IKolibreeConnector;
import com.kolibree.sdkws.core.ProfileWrapper;

/** Created by miguelaragues on 13/9/17. */
public abstract class SDKHelper {

  public static final String DEFAULT_MAC = "DC:56:8C:A2:F4";

  public static final String DEFAULT_NAME = "Ko Kito";

  private SDKHelper() {}

  public static void setActiveProfile(Profile activeProfile) {
    IKolibreeConnector kolibreeConnector = App.appComponent.kolibreeConnector();

    when(kolibreeConnector.getCurrentProfile()).thenReturn(activeProfile);
    ProfileWrapper activeWrapper = kolibreeConnector.withProfileId(activeProfile.getId());
    when(kolibreeConnector.withCurrentProfile()).thenReturn(activeWrapper);
  }
}
