package com.kolibree.android.accountinternal.account;

import static org.mockito.Mockito.mock;

import android.content.Context;
import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.sdkws.brushing.persistence.dao.BrushingDao;
import com.kolibree.sdkws.data.model.gopirate.GoPirateDao;
import com.kolibree.sdkws.data.model.gopirate.GoPirateDatastore;
import com.kolibree.sdkws.internal.OfflineUpdateDao;
import com.kolibree.sdkws.internal.OfflineUpdateDatastore;
import com.kolibree.sdkws.room.ApiRoomDatabase;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("KotlinInternalInJava")
@Module
public abstract class PersistenceModule {

  @Provides
  static SupportSQLiteOpenHelper providesSupportSQLiteOpenHelper() {
    return mock(SupportSQLiteOpenHelper.class);
  }

  @Provides
  @AppScope
  static ApiRoomDatabase providesAppDatabase(Context context) {
    return Room.inMemoryDatabaseBuilder(context, ApiRoomDatabase.class).build();
  }

  @Provides
  static BrushingDao providesBrushingDao(ApiRoomDatabase appDatabase) {
    return appDatabase.brushingDao();
  }

  @Provides
  static OfflineUpdateDao providesOfflineUpdateDao(ApiRoomDatabase appDatabase) {
    return appDatabase.offlineUpdateDao();
  }

  @Provides
  static GoPirateDao providesGoPirateDao(ApiRoomDatabase appDatabase) {
    return appDatabase.goPirateDao();
  }

  @Binds
  abstract GoPirateDatastore bindsInternalGoPirateDatastore(GoPirateDao goPirateDao);

  @Binds
  abstract OfflineUpdateDatastore bindsOfflineUpdateDatastore(OfflineUpdateDao offlineUpdateDao);
}
