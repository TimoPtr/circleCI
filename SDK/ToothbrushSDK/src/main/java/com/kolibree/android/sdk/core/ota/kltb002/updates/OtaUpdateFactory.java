/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updates;

import static com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE;
import static com.kolibree.android.commons.UpdateType.TYPE_GRU;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import com.kolibree.android.commons.AvailableUpdate;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.error.FailureReason;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class responsible for returning a {@link OtaUpdate} for a given file
 *
 * <p>Created by miguelaragues on 5/1/18.
 */
public final class OtaUpdateFactory {

  @WorkerThread
  @NonNull
  public OtaUpdate create(@NonNull AvailableUpdate availableUpdate, @NonNull ToothbrushModel model)
      throws Exception {
    byte[] binaryContent = readBinaryContent(availableUpdate);

    switch (availableUpdate.getType()) {
      case TYPE_FIRMWARE:
        return createFirmwareOtaUpdate(availableUpdate, binaryContent, model);
      case TYPE_GRU:
        return createGruOtaUpdate(availableUpdate, binaryContent);
    }

    throw new FailureReason("Available update is of unknown type " + availableUpdate.getType());
  }

  private byte[] readBinaryContent(@NonNull AvailableUpdate availableUpdate)
      throws FailureReason, IOException {
    final File otaFile = getFile(availableUpdate.getUpdateFilePath());

    if (!otaFile.exists() || !otaFile.canRead()) {
      throw new FailureReason("Could not read firmware file");
    }

    // Read the file content
    return readWholeFile(otaFile);
  }

  private OtaUpdate createGruOtaUpdate(AvailableUpdate availableUpdate, byte[] binaryContent) {
    return new GRUDataUpdate(binaryContent, availableUpdate);
  }

  private OtaUpdate createFirmwareOtaUpdate(
      AvailableUpdate availableUpdate, byte[] binaryContent, ToothbrushModel model)
      throws FailureReason {
    if (model == ToothbrushModel.ARA) {
      return new AraFirmwareUpdate(
          binaryContent, availableUpdate.getVersion(), availableUpdate.getCrc32());
    } else if (model == ToothbrushModel.CONNECT_E1) {
      return new E1FirmwareUpdate(
          binaryContent, availableUpdate.getVersion(), availableUpdate.getCrc32());
    }

    throw new FailureReason("File is intended for an unknown toothbrush");
  }

  /**
   * Read a whole file
   *
   * @param file non null File
   * @return non null byte array
   * @throws IOException if the file could not be read
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  @NonNull
  @VisibleForTesting
  byte[] readWholeFile(@NonNull File file) throws IOException {
    final int size = (int) file.length();
    final byte[] bytes = new byte[size];
    try (FileInputStream fis = new FileInputStream(file);
        BufferedInputStream buf = new BufferedInputStream(fis); ) {
      buf.read(bytes, 0, bytes.length);
    }

    return bytes;
  }

  @VisibleForTesting
  @NonNull
  File getFile(@NonNull String path) {
    return new File(path);
  }
}
