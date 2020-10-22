/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core;

import static com.kolibree.android.TimberTagKt.bluetoothTagFor;

import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.root.Root;
import com.kolibree.android.sdk.core.binary.PayloadWriter;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.driver.ble.CommandSet;
import io.reactivex.Completable;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import timber.log.Timber;

/** {@link Root} implementation for Ara and CM1 toothbrushes */
final class RootCommonBleImpl implements Root {

  private static final String TAG = bluetoothTagFor(RootCommonBleImpl.class);

  /** Ara and CM1 common driver implementation */
  private final BleDriver driver;

  /**
   * The CM1 toothbrush does not respond error when setting the serial number without granted root
   * access, so we have to memorize the state here
   */
  private final AtomicBoolean rootGranted = new AtomicBoolean(false);

  /**
   * Ara and CM1 {@link Root} implementation constructor
   *
   * @param driver non null BleDriver implementation
   */
  RootCommonBleImpl(@NonNull BleDriver driver) {
    this.driver = driver;
  }

  @NonNull
  @Override
  public Completable grantAccess(int passkey) {
    return Completable.fromAction(
        () -> {
          driver.sendCommand(CommandSet.unlockSensitiveDataWriteProtection(passkey));
          rootGranted.set(true);
          Timber.tag(TAG).i("grantAccess()");
        });
  }

  @Override
  public boolean isAccessGranted() {
    return rootGranted.get();
  }

  @NonNull
  @Override
  public Completable setSerialNumber(@NonNull String serialNumber) {
    return Completable.fromAction(
        () -> {
          // Check root access
          checkRoot();

          // Check length
          final byte[] serialBytes = serialNumber.getBytes(Charset.forName("UTF-8"));

          if (serialBytes.length > 19) {
            throw new InvalidParameterException("UTF-8-converted serial number's max length is 19");
          }

          // Write serial number
          final byte[] payload =
              new PayloadWriter(1 + serialBytes.length)
                  .writeByte((byte) 0x30)
                  .writeString(serialNumber)
                  .getBytes();

          boolean result = driver.setDeviceParameter(payload);

          Timber.tag(TAG).i("setSerialNumber(%s) = %b", serialNumber, result);
        });
  }

  @NonNull
  @Override
  public Completable setMacAddress(@NonNull String macAddress) {
    return Completable.fromAction(
        () -> {
          // Check root
          checkRoot();

          // Check parameter (we MUST validate the mac address before sending it to the toothbrush)
          checkMacAddress(macAddress);

          // Write it
          final byte[] payload =
              new PayloadWriter(7).writeByteArray(getMacAddressBytes(macAddress)).getBytes();

          boolean result = driver.setDeviceParameter(payload);

          Timber.tag(TAG).i("setMacAddress(%s) = %b", macAddress, result);
        });
  }

  /**
   * Check root access
   *
   * @throws IllegalAccessException if root access is not granted
   */
  private void checkRoot() throws IllegalAccessException {
    if (!rootGranted.get()) {
      throw new IllegalAccessException("Root access is not granted");
    }
  }

  /**
   * Check MAC address validity (IEEE 802)
   *
   * @param mac non null digit-separated MAC address (with or without the ':' or '-' separator
   *     chars)
   */
  void checkMacAddress(@NonNull String mac) throws IllegalArgumentException {
    if (!Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$").matcher(mac).find()) {
      throw new InvalidParameterException("Invalid MAC address");
    }
  }

  /**
   * Extract MAC address bytes
   *
   * @param macAddress non null IEEE 802 MAC address
   * @return non null byte array
   */
  @NonNull
  byte[] getMacAddressBytes(@NonNull String macAddress) {
    final byte[] bytes = new byte[6];
    final String rawMac = macAddress.replace(":", "").replace("-", "");

    for (int i = 0; i < 6; i++) {
      bytes[i] = ((Integer) Integer.parseInt(rawMac.substring(i * 2, (i + 1) * 2), 16)).byteValue();
    }

    return bytes;
  }
}
