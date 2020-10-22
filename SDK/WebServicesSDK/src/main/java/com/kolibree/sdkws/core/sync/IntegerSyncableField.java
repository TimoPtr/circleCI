package com.kolibree.sdkws.core.sync;

/** Created by aurelien on 11/10/15. */
public final class IntegerSyncableField extends SyncableField<Integer> {
  public IntegerSyncableField(String fieldName, int snapshotValue, int newValue) {
    super(fieldName, snapshotValue, newValue);
  }
}
