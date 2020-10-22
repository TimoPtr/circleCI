package com.kolibree.sdkws.api.gruware;

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.sdkws.core.GruwareRepository;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module
abstract class GruWareApiModule {

  @Provides
  @AppScope
  static GruwareApi providesAccountApiService(Retrofit retrofit) {
    return retrofit.create(GruwareApi.class);
  }
}

@Module(includes = GruWareApiModule.class)
public abstract class GruWareModule {

  @Binds
  abstract GruwareManager bindsGruWareManager(GruwareManagerImpl gruWareManager);

  @Binds
  abstract GruwareRepository bindsGruWareRepository(GruwareRepositoryImpl repo);
}
