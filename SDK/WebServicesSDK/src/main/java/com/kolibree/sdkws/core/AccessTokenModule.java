package com.kolibree.sdkws.core;

import androidx.annotation.Nullable;
import com.kolibree.android.accountinternal.internal.AccountInternal;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;

@Module
public class AccessTokenModule {

  public static final String DI_ACCESS_TOKEN = "di_access_token";

  @Provides
  @Nullable
  @Named(DI_ACCESS_TOKEN)
  static String providesAccountInternal(InternalKolibreeConnector kolibreeConnector) {
    AccountInternal account = kolibreeConnector.currentAccount();

    if (account == null) {
      return null;
    }

    return account.getAccessToken();
  }
}
