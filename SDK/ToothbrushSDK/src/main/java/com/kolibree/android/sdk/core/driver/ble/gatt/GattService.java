package com.kolibree.android.sdk.core.driver.ble.gatt;

import androidx.annotation.NonNull;
import java.util.UUID;

/**
 * Created by aurelien on 15/11/16.
 *
 * <p>Kolibree GATT service definitions
 */
public enum GattService {
  /** Sensors service. */
  SENSORS("01"),

  /** Device service. */
  DEVICE("04"),

  /** Brushing records service. */
  BRUSHING("05"),

  /** OTA update service. */
  OTA_UPDATE("02"),

  /** Files service. Replaces Brushing service on FW >= 1.2.0 */
  FILES("06"),

  /** Plaqless service. Only present in Plaqless toothbrush */
  PLAQLESS("07");

  /** Kolibree service UUID. */
  public final UUID UUID;

  /** Kolibree service identifier. */
  public final String IDENTIFIER;

  GattService(@NonNull String identifier) {
    UUID = java.util.UUID.fromString(identifier + "000000-a1d0-4989-b640-cfb64e5c34e0");
    IDENTIFIER = identifier;
  }

  @Override
  public String toString() {
    return "GattService{" + "UUID=" + UUID + ", IDENTIFIER='" + IDENTIFIER + '\'' + '}';
  }
}
