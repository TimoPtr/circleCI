/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.TimberTagKt.otaTagFor;

import androidx.annotation.VisibleForTesting;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import com.kolibree.android.sdk.version.SoftwareVersion;
import timber.log.Timber;

/**
 * Creates the appropriate tools to perform an Ota Update.
 *
 * <p>Created by miguelaragues on 27/4/18.
 */
class OtaToolsFactory {

  private static final String TAG = otaTagFor(BaseFastOtaWriter.class);

  private static final String FIRST_FAST_OTA_BOOTLOADER = "0.17.65535";

  OtaUpdater createOtaUpdater(
      InternalKLTBConnection connection, BleDriver driver, OtaUpdate otaUpdate, int attempt) {
    if (supportsFastOta(connection) && otaUpdate.getType() == OtaUpdate.TYPE_GRU_DATA) {
      Timber.tag(TAG).v("KLTB002FastGruUpdater is our man for the update job");
      return KLTB002FastGruUpdater.create(connection, driver, attempt);
    }
    Timber.tag(TAG).v("KLTB002BootloaderOtaUpdater is our man for the update job");
    return new KLTB002BootloaderOtaUpdater(connection, driver, attempt);
  }

  /**
   * Creates an OtaWriter capable of writing a {@link OtaUpdate} on the specified {@link
   * KLTBConnection}.
   *
   * @return an OtaWriter instance that can write an OTA update
   */
  OtaWriter createOtaWriter(
      InternalKLTBConnection kltbConnection, BleDriver driver, OtaUpdate otaUpdate, int attempt) {
    if (supportsFastOta(kltbConnection)) {
      if (otaUpdate.getType() == OtaUpdate.TYPE_FIRMWARE) {
        Timber.tag(TAG).v("FastFirmwareWriter is our man for the write job");
        return FastFirmwareWriter.create(driver, attempt);
      } else {
        Timber.tag(TAG).v("FastGruWriter is our man for the write job");
        return FastGruWriter.create(driver, attempt);
      }
    }

    Timber.tag(TAG).v("LegacyOtaWriter is our man for the write job");
    return LegacyOtaWriter.create(kltbConnection, driver);
  }

  @VisibleForTesting
  boolean supportsFastOta(KLTBConnection kltbConnection) {
    SoftwareVersion bootloaderVersion = kltbConnection.toothbrush().getBootloaderVersion();

    SoftwareVersion firstFastOtaBootloader = new SoftwareVersion(FIRST_FAST_OTA_BOOTLOADER);

    boolean supportsFastOta = bootloaderVersion.compareTo(firstFastOtaBootloader) >= 0;
    Timber.tag(TAG)
        .v("Fast OTA supported for %s = %b", bootloaderVersion.toString(), supportsFastOta);

    return supportsFastOta;
  }
}
