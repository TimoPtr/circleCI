package com.kolibree.sdkws.core.sync;

import org.json.JSONException;
import org.json.JSONObject;

/** Created by aurelien on 20/08/15. */
public class SyncableField<T> {
  private static final String FIELD_DATA_TYPE = "data_type";
  private static final String FIELD_FIELD_NAME = "field_name";
  private static final String FIELD_SNAPSHOT_VALUE = "snapshot_value";
  private static final String FIELD_NEW_VALUE = "new_value";

  private static final int TYPE_INTEGER = 1;
  private static final int TYPE_STRING = 2;

  private int dataType;
  private String fieldName;
  private T snapshotValue;
  private T newValue;

  protected SyncableField(String fieldName, T snapshotValue, T newValue) {
    this.dataType = dataType;
    this.fieldName = fieldName;
    this.snapshotValue = snapshotValue;
    this.newValue = newValue;
  }

  public static SyncableField parse(JSONObject json) throws JSONException {
    final int dataType = json.getInt(FIELD_DATA_TYPE);
    final String fieldName = json.getString(FIELD_FIELD_NAME);

    switch (dataType) {
      case TYPE_INTEGER:
        {
          final int snapshotValue = json.getInt(FIELD_SNAPSHOT_VALUE);
          final int newValue = json.getInt(FIELD_NEW_VALUE);
          return new IntegerSyncableField(fieldName, snapshotValue, newValue);
        }

      case TYPE_STRING:
        final String snapshotValue = json.getString(FIELD_SNAPSHOT_VALUE);
        final String newValue = json.getString(FIELD_NEW_VALUE);
        return new StringSyncableField(fieldName, snapshotValue, newValue);

      default:
        throw new JSONException("");
    }
  }

  public T getSnapshotValue() {
    return snapshotValue;
  }

  public T getNewValue() {
    return newValue;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void updateNewValue(SyncableField<T> newField) {
    newValue = newField.getNewValue();
  }

  public boolean is(SyncableField another) {
    return fieldName.equals(another.getFieldName());
  }

  public JSONObject toJSONObject() throws JSONException {
    final JSONObject json = new JSONObject();
    json.put(FIELD_FIELD_NAME, fieldName);
    json.put(FIELD_SNAPSHOT_VALUE, snapshotValue);
    json.put(FIELD_NEW_VALUE, newValue);

    if (this instanceof IntegerSyncableField) {
      json.put(FIELD_DATA_TYPE, TYPE_INTEGER);
    } else if (this instanceof StringSyncableField) {
      json.put(FIELD_DATA_TYPE, TYPE_STRING);
    }

    return json;
  }
}
