package com.kolibree.android.sdk.util;

import androidx.annotation.NonNull;

/** Created by miguelaragues on 9/10/17. */
public final class ByteUtils {

  private ByteUtils() {}

  @NonNull
  public static CharSequence prettyPrint(byte[] bytes) {
    if (bytes == null) {
      return "null";
    }

    return prettyPrint(bytes, bytes.length);
  }

  @NonNull
  public static CharSequence prettyPrint(final byte[] bytes, final int length) {
    if (bytes == null) {
      return "null";
    }

    return prettyPrint(bytes, length, 0xFF);
  }

  @NonNull
  public static CharSequence prettyPrint(final byte[] bytes, final int length, int mask) {
    if (bytes == null) {
      return "null";
    }
    if (bytes.length == 0) {
      return "empty";
    }

    final StringBuilder stringBuilder = new StringBuilder("(0x) ");
    for (int i = 0; i < length; i++) {
      if (i < length) { // display byte
        stringBuilder.append(String.format("%02X", bytes[i] & mask));
        if (i < length - 1) {
          stringBuilder.append("-");
        }
      } else {
        stringBuilder.append(" ... ");
        break;
      }
    }

    return stringBuilder;
  }
}
