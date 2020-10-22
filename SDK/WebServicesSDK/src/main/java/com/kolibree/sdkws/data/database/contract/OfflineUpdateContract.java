package com.kolibree.sdkws.data.database.contract;

/** Created by aurelien on 18/04/16. */
public interface OfflineUpdateContract {
  String TABLE_NAME = "offlineupdate";

  String COLUMN_PROFILE_ID = "profileid";
  String COLUMN_DATA = "data";
  String COLUMN_TYPE = "type";

  @Deprecated
  String CREATE_REQUEST =
      "CREATE TABLE "
          + TABLE_NAME
          + "("
          + COLUMN_TYPE
          + " INTEGER,"
          + COLUMN_DATA
          + " TEXT,"
          + COLUMN_PROFILE_ID
          + " INTEGER)";
}
