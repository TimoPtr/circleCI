package com.kolibree.sdkws.profile;

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.sdkws.api.ConnectivityApiManager;
import com.kolibree.sdkws.api.ConnectivityApiManagerImpl;
import com.kolibree.sdkws.profile.persistence.repo.ProfileRepository;
import com.kolibree.sdkws.profile.persistence.repo.ProfileRepositoryImpl;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module
public abstract class ProfileNetworkModule {

  @Provides
  @AppScope
  static ProfileApi providesProfileApiService(Retrofit retrofit) {
    return retrofit.create(ProfileApi.class);
  }

  @Binds
  abstract ProfileManager bindsProfileManager(ProfileManagerImpl accountManager);

  @Binds
  abstract ProfileRepository bindsProfileRepository(ProfileRepositoryImpl profileRepository);

  @Binds
  abstract ConnectivityApiManager bindsConnectivityApiManager(
      ConnectivityApiManagerImpl connectivityApiManager);
}
