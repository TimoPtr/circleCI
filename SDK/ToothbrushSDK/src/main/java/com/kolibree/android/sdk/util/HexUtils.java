package com.kolibree.android.sdk.util;

import androidx.annotation.Keep;
import com.kolibree.android.commons.models.StrippedMac;

/** Created by miguelaragues on 9/10/17. */
@Keep
public final class HexUtils {

  private HexUtils() {}

  public static String macMinusOne(String originalMac) {
    String macAsHexString = originalMac.replaceAll(":", "");
    byte[] macAsBytes = hexStringToByteArray(macAsHexString);
    macAsBytes[macAsBytes.length - 1] = (byte) (macAsBytes[macAsBytes.length - 1] - 1);
    return convertToColonSeparatedHex(macAsBytes);
  }

  /**
   * Converts a Hex String to an array of bytes
   *
   * <p>Accepts strings separated by ":", " " or new lines
   *
   * @param mac
   * @return
   */
  public static byte[] hexStringToByteArray(String mac) {
    StrippedMac strippedMac = StrippedMac.fromMac(mac);
    mac = strippedMac.getValue();
    int len = mac.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte)
              ((Character.digit(mac.charAt(i), 16) << 4) + Character.digit(mac.charAt(i + 1), 16));
    }
    return data;
  }

  public static String convertToColonSeparatedHex(byte[] data) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < data.length; i++) {
      int halfbyte = (data[i] >>> 4) & 0x0F;
      int two_halfs = 0;
      do {
        if ((0 <= halfbyte) && (halfbyte <= 9)) {
          buf.append((char) ('0' + halfbyte));
        } else {
          buf.append((char) ('a' + (halfbyte - 10)));
        }
        halfbyte = data[i] & 0x0F;
      } while (two_halfs++ < 1);
      buf.append(":");
    }

    buf.deleteCharAt(buf.length() - 1);

    return buf.toString();
  }
}
