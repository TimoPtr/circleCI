/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.appdata;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.kolibree.android.room.DateConvertersLong;
import java.util.concurrent.TimeUnit;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

/** {@link AppData} implementation */
@Entity(
    tableName = "appdata",
    primaryKeys = {"profile_id", "is_synchronized"})
@TypeConverters(AppDataImpl.JsonObjectTypeConverter.class)
@Keep
public final class AppDataImpl implements AppData {

  static final AppDataImpl NULL =
      new AppDataImpl(
          0L,
          0,
          ZonedDateTime.of(LocalDateTime.MIN, ZoneId.of("UTC")).toEpochSecond(),
          new JsonObject(),
          false);

  @ColumnInfo(name = "profile_id")
  private transient long profileId;

  @SerializedName("data_version")
  @ColumnInfo(name = "version")
  private int dataVersion;

  @SerializedName("last_modified")
  @ColumnInfo(name = "timestamp")
  private long timestamp;

  @SerializedName("data")
  @ColumnInfo(name = "data")
  private JsonObject dataJsonObject;

  @ColumnInfo(name = "is_synchronized")
  private transient boolean isSynchronized;

  private AppDataImpl(
      long profileId,
      int dataVersion,
      @NonNull Long timestamp,
      @NonNull JsonObject jsonObject,
      boolean isSynchronized) {
    this.profileId = profileId;
    this.dataVersion = dataVersion;
    this.timestamp = timestamp;
    this.dataJsonObject = jsonObject;
    this.isSynchronized = isSynchronized;
  }

  public AppDataImpl() {}

  /**
   * Create a local app data structure
   *
   * @param profileId long profile ID
   * @param dataVersion int data version
   * @param localDateTime non null [ZonedDateTime]
   * @param jsonData non null JSON [String]
   * @return non null [AppData]
   */
  public static AppData create(
      long profileId, int dataVersion, @NonNull ZonedDateTime localDateTime, String jsonData) {
    return new AppDataImpl(
        profileId,
        dataVersion,
        TimeUnit.MILLISECONDS.toSeconds(
            new DateConvertersLong().setZonedDateTimeUTCToLong(localDateTime)),
        new JsonParser().parse(jsonData).getAsJsonObject(),
        false);
  }

  public void setTimestamp(long dateTime) {
    this.timestamp = dateTime;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @NonNull
  @Override
  public ZonedDateTime getDateTime() {
    return Instant.ofEpochSecond(timestamp).atZone(ZoneOffset.UTC);
  }

  public void setDateTime(ZonedDateTime dateTime) {
    this.timestamp = dateTime.toEpochSecond();
  }

  @Override
  public int getDataVersion() {
    return dataVersion;
  }

  public void setDataVersion(int dataVersion) {
    this.dataVersion = dataVersion;
  }

  @NonNull
  @Override
  public String getData() {
    return dataJsonObject.toString();
  }

  public JsonObject getDataJsonObject() {
    return dataJsonObject;
  }

  public void setData(String data) {
    this.dataJsonObject = new JsonParser().parse(data).getAsJsonObject();
  }

  public void setDataJsonObject(JsonObject data) {
    this.dataJsonObject = data;
  }

  @Override
  public long getProfileId() {
    return profileId;
  }

  public void setProfileId(long profileId) {
    this.profileId = profileId;
  }

  /**
   * Check if the data has been saved on the server
   *
   * @return true if the data is synchronized, false otherwise
   */
  public boolean isSynchronized() {
    return isSynchronized;
  }

  public void setSynchronized(boolean isSynchronized) {
    this.isSynchronized = isSynchronized;
  }

  public static class JsonObjectTypeConverter {

    private JsonParser parser = new JsonParser();

    @TypeConverter
    public String jsonObjectToString(JsonObject object) {
      return object.toString();
    }

    @TypeConverter
    public JsonObject stringToJsonObject(String value) {
      return parser.parse(value).getAsJsonObject();
    }
  }
}
