package com.kolibree.android.sdk.core.database;

/**
 * Created by aurelien on 04/02/16.
 *
 * <p>Local toothbrush database contract
 */
interface KnownToothbrushTableContract {

  String TABLE_NAME = "known_toothbrushes";

  String COLUMN_MAC = "mac";
  String COLUMN_NAME = "name";
  String COLUMN_MODEL = "model";

  String CREATE_REQUEST =
      "CREATE TABLE IF NOT EXISTS "
          + TABLE_NAME
          + "("
          + COLUMN_MAC
          + " CHAR(17) NOT NULL,"
          + COLUMN_NAME
          + " TEXT NOT NULL,"
          + COLUMN_MODEL
          + " TINYINT(1) NOT NULL,"
          + "PRIMARY KEY ("
          + COLUMN_MAC
          + "))";

  String TRUNCATE_QUERY = "DELETE FROM " + TABLE_NAME;
}
