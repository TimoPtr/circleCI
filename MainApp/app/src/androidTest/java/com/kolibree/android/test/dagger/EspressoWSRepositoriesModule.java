package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.commons.interfaces.RemoteBrushingsProcessor;
import com.kolibree.android.network.environment.EnvironmentManagerModule;
import com.kolibree.sdkws.account.AccountManager;
import com.kolibree.sdkws.api.gruware.GruwareManager;
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsDatastore;
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade;
import com.kolibree.sdkws.brushing.wrapper.BrushingFacadeImpl;
import com.kolibree.sdkws.core.GruwareRepository;
import com.kolibree.sdkws.internal.OfflineUpdateDatastore;
import com.kolibree.sdkws.profile.ProfileManager;
import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;

@Module(includes = {EnvironmentManagerModule.class})
public abstract class EspressoWSRepositoriesModule {

  @SuppressWarnings("KotlinInternalInJava")
  @Provides
  @AppScope
  static OfflineUpdateDatastore bindsOfflineUpdateDatastore() {
    return mock(OfflineUpdateDatastore.class);
  }

  @SuppressWarnings("KotlinInternalInJava")
  @Provides
  @AppScope
  static BrushingsDatastore providesBrushingsDatastore() {
    return mock(BrushingsDatastore.class);
  }

  @Provides
  @AppScope
  static AccountManager bindsAccountManager() {
    return mock(AccountManager.class);
  }

  @Provides
  @AppScope
  static ProfileManager bindsProfileManager() {
    return mock(ProfileManager.class);
  }

  @Provides
  @AppScope
  static GruwareManager bindsGruWareManager() {
    return mock(GruwareManager.class);
  }

  @Provides
  @AppScope
  static GruwareRepository bindsGruWareRepository() {
    return mock(GruwareRepository.class);
  }

  @BindsOptionalOf
  abstract RemoteBrushingsProcessor bindRemoteBrushingsProcessor();

  @Binds
  abstract BrushingFacade bindsBrushingManagerWrapper(BrushingFacadeImpl brushingManagerWrapper);
}
