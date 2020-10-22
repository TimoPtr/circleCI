package com.kolibree.sdkws.data.database.contract;

/** Created by aurelien on 18/04/16. */
public interface GoPirateContract {

  String TABLE_NAME = "gopirate";

  String COLUMN_PROFILE_ID = "profile_id";
  String COLUMN_RANK = "rank";
  String COLUMN_GOLD = "gold";
  String COLUMN_LAST_WORLD_REACHED = "last_world_reached";
  String COLUMN_LAST_LEVEL_REACHED = "last_level_reached";
  String COLUMN_LAST_LEVEL_BRUSH = "last_level_brush";
  String COLUMN_LAST_SHIP_BOUGHT = "last_ship_bought";
  String COLUMN_AVATAR_COLOR = "avatar_color";
  String COLUMN_TREASURES = "treasures";
  String COLUMN_BRUSHING_NUMBER = "brushing_number";

  // Create request
  @Deprecated
  String CREATE_REQUEST =
      "CREATE TABLE IF NOT EXISTS "
          + TABLE_NAME
          + "("
          + COLUMN_PROFILE_ID
          + " INTEGER NOT NULL,"
          + COLUMN_RANK
          + " INTEGER NOT NULL,"
          + COLUMN_GOLD
          + " INTEGER NOT NULL, "
          + COLUMN_LAST_WORLD_REACHED
          + " INTEGER NOT NULL, "
          + COLUMN_LAST_LEVEL_REACHED
          + " INTEGER NOT NULL, "
          + COLUMN_LAST_LEVEL_BRUSH
          + " INTEGER NOT NULL, "
          + COLUMN_LAST_SHIP_BOUGHT
          + " INTEGER NOT NULL, "
          + COLUMN_AVATAR_COLOR
          + " INTEGER NOT NULL, "
          + COLUMN_TREASURES
          + " TEXT NOT NULL, "
          + COLUMN_BRUSHING_NUMBER
          + " INTEGER NOT NULL);";
}
