package com.kolibree.android.sdk.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.provider.Settings;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.android.commons.ToothbrushModel;

/**
 * Created by aurelien and Samuel Peter on 24/10/16.
 *
 * <p>Kolibree toothbrush utilities
 */
@Keep
public final class KolibreeUtils {

  private static final String KOLIBREE_V1_MAC_ADDRESS_PREFIX = "00:13:EF";
  private static final String KOLIBREE_V2_MAC_ADDRESS_PREFIX = "C0:4B";

  private KolibreeUtils() {}

  /**
   * Check if a Bluetooth device is a Kolibree V1 toothbrush
   *
   * @param device a non null discovered bluetooth device
   * @return true if this device is a Kolibree V1 toothbrush, false otherwise
   */
  public static boolean isKolibreeV1(@NonNull BluetoothDevice device) {
    return device.getAddress().startsWith(KOLIBREE_V1_MAC_ADDRESS_PREFIX);
  }

  /**
   * Check if a Bluetooth device is a Kolibree V2 toothbrush
   *
   * @param device a non null discovered bluetooth device
   * @return true if this device is a Kolibree V2 toothbrush, false otherwise
   */
  public static boolean isKolibreeV2(@NonNull BluetoothDevice device) {
    return device.getAddress().startsWith(KOLIBREE_V2_MAC_ADDRESS_PREFIX);
  }

  /**
   * Get battery level from voltage level
   *
   * @param volt double current battery output voltage
   * @return int battery percent [0, 100]
   */
  public static int toBatteryLevel(double volt) {
    double res = 0;

    if (volt > 4150) {
      res = 100;
    } else {
      final double z = (volt - 3695.5) / 309.59;
      final double[] p =
          new double[] {
            -0.0074375,
            -0.01069,
            0.06279,
            0.0652,
            -0.22384,
            -0.12665,
            0.40534,
            0.0096391,
            -0.33418,
            0.47723,
            0.58278
          };

      for (int i = 0; i < p.length; i++) {
        res += p[i] * Math.pow(z, p.length - 1 - i);
      }
    }

    return (int) Math.max(0, Math.min(100 * res, 100));
  }

  /**
   * Parse a version string (ex: 0.4.1) and get it as long Kolibree format (ex: 0x00040001)
   *
   * @param version non null String version
   * @return long version
   */
  public static long parseVersionString(@NonNull String version) {
    final String[] digits = version.split("\\.");

    if (digits.length == 3) { // Firmware version
      return Long.parseLong(
          String.format(
              "%02X%02X%04X",
              Integer.parseInt(digits[0]),
              Integer.parseInt(digits[1]),
              Integer.parseInt(digits[2])),
          16);
    } else if (digits.length == 2) {
      return Long.parseLong(
          String.format("%04X%04X", Integer.parseInt(digits[0]), Integer.parseInt(digits[1])), 16);
    } else {
      throw new IllegalArgumentException("Unsupported version format");
    }
  }

  /**
   * Compute CRC16 CCITT of a byte array
   *
   * <p>Courtesy of http://introcs.cs.princeton.edu/java/51data/CRC16CCITT.java.html and Ghassen
   * KEFI
   *
   * @param bytes non null byte array
   * @return CRC16 CCITT value
   */
  public static int crc16ccitt(byte[] bytes) {
    final int polynomial = 0x1021; // 0001 0000 0010 0001  (0, 5, 12)
    int crc = 0; // initial value

    for (byte b : bytes) {
      for (int i = 0; i < 8; i++) {
        boolean bit = ((b >> (7 - i) & 1) == 1);
        boolean c15 = ((crc >> 15 & 1) == 1);
        crc <<= 1;
        if (c15 ^ bit) {
          crc ^= polynomial;
        }
      }
    }

    return crc & 0xffff;
  }

  /**
   * Compute sum-of-bytes checksum
   *
   * @param array non null byte array
   * @return byte sum-of-bytes checksum
   */
  public static byte sumOfBytesChecksum(@NonNull byte[] array) {
    short sum = 0;

    for (byte b : array) {
      sum += (short) (b & 0xFF);
    }

    return (byte) (sum & 0xFF);
  }

  @SuppressLint("HardwareIds")
  public static long getOwnerDeviceId(@NonNull Context context) {
    return Long.parseLong(
        Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
            .substring(8),
        16);
  }

  public static String getNameFromMacAddress(@NonNull ToothbrushModel model, @NonNull String mac) {
    final String[] digits = mac.split(":");

    // Here concatenation is more efficient than using a StringBuffer
    return model.name() + "_" + digits[3] + digits[4] + digits[5];
  }
}
