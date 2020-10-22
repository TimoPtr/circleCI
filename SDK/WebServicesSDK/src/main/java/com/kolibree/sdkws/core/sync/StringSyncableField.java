package com.kolibree.sdkws.core.sync;

/** Created by aurelien on 11/10/15. */
public final class StringSyncableField extends SyncableField<String> {
  public StringSyncableField(String fieldName, String snapshotValue, String newValue) {
    super(fieldName, snapshotValue, newValue);
  }
}
