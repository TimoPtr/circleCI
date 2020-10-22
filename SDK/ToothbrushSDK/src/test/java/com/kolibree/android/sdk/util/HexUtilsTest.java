package com.kolibree.android.sdk.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Created by miguelaragues on 9/10/17. */
public class HexUtilsTest {

  @Test
  public void convertToColonSeparatedHex() {
    byte[] byteArray = new byte[] {(byte) 0xAC, 0x02, (byte) 0xE8};
    String expectedHexString = "AC:02:E8";

    assertTrue(expectedHexString.equalsIgnoreCase(HexUtils.convertToColonSeparatedHex(byteArray)));
  }

  @Test
  public void hexStringToByteArray_colon() {
    String hexString = "AC:02:E8";
    byte[] expectedByteArray = new byte[] {(byte) 0xAC, 0x02, (byte) 0xE8};

    assertArrayEquals(
        "Expected\t"
            + ByteUtils.prettyPrint(expectedByteArray)
            + "\nGot\t"
            + ByteUtils.prettyPrint(HexUtils.hexStringToByteArray(hexString)),
        expectedByteArray,
        HexUtils.hexStringToByteArray(hexString));
  }

  @Test
  public void hexStringToByteArray_space() {
    String hexString = "AC 02 E8";
    byte[] expectedByteArray = new byte[] {(byte) 0xAC, 0x02, (byte) 0xE8};

    assertArrayEquals(
        "Expected\t"
            + ByteUtils.prettyPrint(expectedByteArray)
            + "\nGot\t"
            + ByteUtils.prettyPrint(HexUtils.hexStringToByteArray(hexString)),
        expectedByteArray,
        HexUtils.hexStringToByteArray(hexString));
  }

  @Test
  public void hexStringToByteArray_newLine() {
    String hexString = "AC:02\nE8";
    byte[] expectedByteArray = new byte[] {(byte) 0xAC, 0x02, (byte) 0xE8};

    assertArrayEquals(
        "Expected\t"
            + ByteUtils.prettyPrint(expectedByteArray)
            + "\nGot\t"
            + ByteUtils.prettyPrint(HexUtils.hexStringToByteArray(hexString)),
        expectedByteArray,
        HexUtils.hexStringToByteArray(hexString));
  }

  @Test
  public void macMinusOne() {
    String originalMac = "AC:02:E8";
    String expectedMac = "AC:02:E7";

    assertTrue(expectedMac.equalsIgnoreCase(HexUtils.macMinusOne(originalMac)));
  }

  @Test
  public void macMinusOne_endingIn00() {
    String originalMac = "AC:02:00";
    String expectedMac = "AC:02:FF";

    assertTrue(expectedMac.equalsIgnoreCase(HexUtils.macMinusOne(originalMac)));
  }
}
