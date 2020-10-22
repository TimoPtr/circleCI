package com.kolibree.sdkws.internal;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import com.kolibree.sdkws.core.sync.SyncableField;
import com.kolibree.sdkws.data.database.contract.OfflineUpdateContract;
import com.kolibree.sdkws.data.model.gopirate.UpdateGoPirateData;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Created by aurelien on 21/08/15. */
@Entity(
    tableName = "offlineupdate",
    primaryKeys = {OfflineUpdateContract.COLUMN_PROFILE_ID, "type"})
public final class OfflineUpdateInternal {

  public static final int TYPE_PICTURE = 1;
  public static final int TYPE_FIELDS = 2;
  public static final int TYPE_DELETE_PROFILE = 3;
  public static final int TYPE_GO_PIRATE = 4;

  private static final String FIELD_SYNCABLE_FIELDS = "sync";

  private int type;

  @Nullable private String data;

  @ColumnInfo(name = OfflineUpdateContract.COLUMN_PROFILE_ID)
  private long profileId;

  public OfflineUpdateInternal() {}

  @Ignore
  public OfflineUpdateInternal(ArrayList<SyncableField> fields, long profileId)
      throws JSONException {
    this.type = TYPE_FIELDS;
    this.profileId = profileId;

    // Convert data to json
    final JSONObject root = new JSONObject();
    final JSONArray list = new JSONArray();

    for (SyncableField f : fields) {
      list.put(f.toJSONObject());
    }

    root.put(FIELD_SYNCABLE_FIELDS, list);
    data = root.toString();
  }

  @Ignore
  public OfflineUpdateInternal(String localPicturePath, long profileId) {
    this.type = TYPE_PICTURE;
    this.data = localPicturePath;
    this.profileId = profileId;
  }

  @Ignore
  public OfflineUpdateInternal(long profileId) {
    this.type = TYPE_DELETE_PROFILE;
    this.data = "";
    this.profileId = profileId;
  }

  @Ignore
  public OfflineUpdateInternal(UpdateGoPirateData data, long profileId) {
    this.type = TYPE_GO_PIRATE;
    this.data = data.toString();
    this.profileId = profileId;
  }

  public ArrayList<SyncableField> getSyncableFields() {
    if (type == TYPE_FIELDS) {
      try {
        final JSONObject root = new JSONObject(data);
        final JSONArray list = root.getJSONArray(FIELD_SYNCABLE_FIELDS);
        final ArrayList<SyncableField> fields = new ArrayList<>(list.length());

        for (int i = 0; i < list.length(); i++) {
          fields.add(SyncableField.parse(list.getJSONObject(i)));
        }

        return fields;
      } catch (JSONException e) {
      }
    }

    return null;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  @Nullable
  public String getData() {
    return data;
  }

  public void setData(@Nullable String data) {
    this.data = data;
  }

  public long getProfileId() {
    return profileId;
  }

  public void setProfileId(long profileId) {
    this.profileId = profileId;
  }

  public String getPicturePath() {
    if (type == TYPE_PICTURE) {
      return data;
    }

    return null;
  }

  public UpdateGoPirateData getGoPirateUpdateData() {
    if (type == TYPE_GO_PIRATE) {
      try {
        return new UpdateGoPirateData(data);
      } catch (JSONException e) {
      }
    }

    return null;
  }

  public void merge(OfflineUpdateInternal newUpdate) {
    if (type == TYPE_PICTURE) {
      data = newUpdate.getPicturePath();
    } else if (type == TYPE_FIELDS) {
      final ArrayList<SyncableField> newData = newUpdate.getSyncableFields();
      final ArrayList<SyncableField> oldData = getSyncableFields();

      // Iterate through new values
      if (newData != null && oldData != null) {
        for (SyncableField newField : newData) {
          SyncableField oldField = null;

          for (SyncableField f : oldData) {
            if (f.is(newField)) {
              oldField = f;
              break;
            }
          }

          if (oldField != null) { // Replace old "new value"
            oldField.updateNewValue(newField);
          } else {
            oldData.add(newField); // Add this field to current update
          }
        }
      }

      // Come back to json
      try {
        final JSONObject root = new JSONObject();
        final JSONArray list = new JSONArray();

        for (SyncableField f : oldData) {
          list.put(f.toJSONObject());
        }

        root.put(FIELD_SYNCABLE_FIELDS, list);
        data = root.toString();
      } catch (JSONException e) {
      }
    } else if (type == TYPE_GO_PIRATE) {
      final UpdateGoPirateData local = getGoPirateUpdateData();
      final UpdateGoPirateData remote = newUpdate.getGoPirateUpdateData();

      if (local != null && remote != null) {
        local.setRank(Math.max(local.getRank(), remote.getRank()));
        local.onGoldEarned(remote.getGold());
        local.setLastWorldReached(
            Math.max(local.getLastWorldReached(), remote.getLastWorldReached()));
        local.setLastLevelReached(
            Math.max(local.getLastLevelReached(), remote.getLastLevelReached()));
        local.setLastLevelBrush(remote.getLastLevelBrush());
        local.setLastShipBought(remote.getLastShipBought());
        local.setAvatarColor(remote.getAvatarColor());

        if (remote.hasBrushing()) {
          local.onBrushing();
        }

        for (Integer treasure : remote.getNewTreasures()) {
          if (!local.hasTreasure(treasure)) {
            local.onNewTreasure(treasure);
          }
        }

        data = local.toString();
      }
    }
  }

  @Nullable
  public SyncableField getUpdateForField(String fieldName) {
    ArrayList<SyncableField> fields = getSyncableFields();
    if (fields != null) {
      for (SyncableField f : fields) {
        if (f.getFieldName().equals(fieldName)) {
          return f;
        }
      }
    }

    return null;
  }
}
