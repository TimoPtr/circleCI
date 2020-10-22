package com.kolibree.android.sdk.core.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by aurelien on 09/11/15.
 *
 * <p>Base open helper
 */
class OpenHelper extends SQLiteOpenHelper {

  OpenHelper(Context context) {
    super(context, "com.kolibree.android.sdk.db", null, 1);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.execSQL(KnownToothbrushTableContract.CREATE_REQUEST);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {}
}
