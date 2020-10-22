package com.kolibree.android.test.dagger;

import static com.kolibree.android.app.sdkwrapper.KolibreeModule.DI_ACTIVE_PROFILE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.annotation.Nullable;
import com.kolibree.R;
import com.kolibree.android.accountinternal.profile.models.Profile;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.app.sdkwrapper.KolibreeFacade;
import com.kolibree.android.google.auth.GoogleSignInCredentials;
import com.kolibree.sdkws.core.IKolibreeConnector;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;

/** Created by miguelaragues on 8/8/17. */
@Module
public abstract class EspressoKolibreeModule {

  @Provides
  @AppScope
  static KolibreeFacade providesKolibreeFacade(IKolibreeConnector connector) {
    KolibreeFacade facade = mock(KolibreeFacade.class);

    when(facade.connector()).thenReturn(connector);

    return facade;
  }

  @Provides
  @Nullable
  @Named(DI_ACTIVE_PROFILE)
  static Profile providesActiveProfile(IKolibreeConnector kolibreeConnector) {
    return kolibreeConnector.getCurrentProfile();
  }

  @Provides
  static GoogleSignInCredentials providesGoogleSignInCredentials() {
    return new GoogleSignInCredentials(
        R.string.google_sign_in_staging_web_client_id,
        R.string.google_sign_in_staging_web_client_id_iv);
  }
}
