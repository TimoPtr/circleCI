package com.kolibree.android.sdk.util;

/** Created by mdaniel on 29/04/2016. */
public class ByteConverter {

  public static String processNBytesString(int size, int[] mBuffer, int mBufferIndex) {
    char[] hexChars = new char[size * 2];
    final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    for (int i = mBufferIndex; i < size + mBufferIndex; i++) {
      hexChars[i * 2] = HEX_CHARS[(mBuffer[i] & 0xF0) >>> 4];
      hexChars[i * 2 + 1] = HEX_CHARS[mBuffer[i] & 0x0F];
    }
    return convertHexToString(new String(hexChars));
  }

  private static String convertHexToString(String hex) {

    StringBuilder sb = new StringBuilder();
    StringBuilder temp = new StringBuilder();

    // 49204c6f7665204a617661 split into two characters 49, 20, 4c...
    for (int i = 0; i < hex.length() - 1; i += 2) {

      // grab the hex in pairs
      String output = hex.substring(i, (i + 2));
      // convert hex to decimal
      int decimal = Integer.parseInt(output, 16);

      if (decimal != 0) {
        // convert the decimal to character
        sb.append((char) decimal);
      }

      temp.append(decimal);
    }

    return sb.toString();
  }

  public static int processTwoBytesUnsigned(int[] mBuffer, int mBufferIndex) {
    mBufferIndex -= 2;
    return mBuffer[mBufferIndex++] | (mBuffer[mBufferIndex++] << 8);
  }

  public static long processFourBytesUnsigned(int[] mBuffer, int mBufferIndex) {
    mBufferIndex -= 4;
    return mBuffer[mBufferIndex++]
        | (mBuffer[mBufferIndex++] << 8)
        | (mBuffer[mBufferIndex++] << 16)
        | (mBuffer[mBufferIndex++] << 24);
  }

  public static short processTwoBytesSigned(int[] mBuffer, int mBufferIndex) {
    mBufferIndex -= 2;
    return (short) (mBuffer[mBufferIndex++] | (mBuffer[mBufferIndex++] << 8));
  }

  public static int processFourBytesSigned(int[] mBuffer, int mBufferIndex) {
    mBufferIndex -= 4;
    return mBuffer[mBufferIndex++]
        | (mBuffer[mBufferIndex++] << 8)
        | (mBuffer[mBufferIndex++] << 16)
        | (mBuffer[mBufferIndex++] << 24);
  }
}
