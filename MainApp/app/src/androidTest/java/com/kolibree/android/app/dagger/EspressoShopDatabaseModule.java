/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger;

import android.content.Context;
import androidx.room.Room;
import com.kolibree.android.commons.interfaces.Truncable;
import com.kolibree.android.shop.data.ShopRoomDatabase;
import com.kolibree.android.shop.data.persitence.CartDao;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@SuppressWarnings("KotlinInternalInJava")
@Module
public abstract class EspressoShopDatabaseModule {

  @Provides
  @AppScope
  static ShopRoomDatabase providesShopRoomDatabase(Context context) {
    return Room.inMemoryDatabaseBuilder(context, ShopRoomDatabase.class)
        .allowMainThreadQueries()
        .build();
  }

  @Provides
  static CartDao providesCartDao(ShopRoomDatabase database) {
    return database.cartDao();
  }

  @Provides
  @IntoSet
  static Truncable providesTruncable(CartDao dao) {
    return dao;
  }
}
