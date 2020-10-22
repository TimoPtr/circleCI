/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.binary;

import static com.kolibree.android.sdk.core.binary.PayloadWriter.MAX_UNSIGNED_INT8;
import static org.junit.Assert.assertArrayEquals;

import com.kolibree.android.sdk.version.HardwareVersion;
import com.kolibree.android.sdk.version.SoftwareVersion;
import java.nio.charset.Charset;
import org.junit.Test;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

/** {@link PayloadWriter} test unit */
public class PayloadWriterTest {

  @Test
  public void testWriteByte() {
    final PayloadWriter test = new PayloadWriter(4);
    test.writeByte((byte) -1).writeByte((byte) 0xFF).writeByte((byte) 0x00).writeByte((byte) 128);

    assertArrayEquals(new byte[] {-1, (byte) 0xFF, 0x00, (byte) 128}, test.getBytes());
  }

  @Test
  public void testWriteUnsignedInt8() {
    final PayloadWriter test = new PayloadWriter(3);
    test.writeUnsignedInt8((short) 0)
        .writeUnsignedInt8((short) 10)
        .writeUnsignedInt8((short) MAX_UNSIGNED_INT8);

    assertArrayEquals(new byte[] {(byte) 0x00, (byte) 0x0A, (byte) 0xff}, test.getBytes());
  }

  @Test
  public void testWriteUnsignedInt16() {
    final PayloadWriter test = new PayloadWriter(2);
    test.writeUnsignedInt16(0xABCD);

    assertArrayEquals(new byte[] {(byte) 0xCD, (byte) 0xAB}, test.getBytes());
  }

  @Test
  public void testWriteInt32() {
    final PayloadWriter test = new PayloadWriter(4);
    test.writeInt32(0x89ABCDEF);

    assertArrayEquals(
        new byte[] {(byte) 0xEF, (byte) 0xCD, (byte) 0xAB, (byte) 0x89}, test.getBytes());
  }

  @Test
  public void testWriteByteArray() {
    final byte[] set = new byte[] {-1, (byte) 0xFF, 0x00, (byte) 128};
    final PayloadWriter test = new PayloadWriter(4);
    test.writeByteArray(set);

    assertArrayEquals(set, test.getBytes());
  }

  @Test
  public void testWriteDate_sendUTC_dateTime() {
    final int year = 2018;
    final int month = 11;
    final int day = 20;
    final int hours = 11;
    final int minutes = 54;
    final int seconds = 45;
    final int offsetHours = 8;
    final OffsetDateTime dateTime =
        OffsetDateTime.of(
            year, month, day, hours, minutes, seconds, 0, ZoneOffset.ofHours(offsetHours));

    final PayloadWriter test = new PayloadWriter(6);
    test.writeDate(dateTime);

    assertArrayEquals(
        new byte[] {(byte) (year - 2000), month, day, hours - offsetHours, minutes, seconds},
        test.getBytes());
  }

  @Test
  public void testWriteString() {
    final String testString = "Super test with spanish characters áíóúñ, and french ones èçàùê";
    final byte[] testBytes = testString.getBytes(Charset.forName("UTF-8"));
    final PayloadWriter test = new PayloadWriter(testBytes.length).writeString(testString);

    assertArrayEquals(testBytes, test.getBytes());
  }

  @Test
  public void testWriteHardwareVersion() {
    assertArrayEquals(
        new byte[] {0x15, 0x00, 0x74, 0x12},
        new PayloadWriter(4).writeHardwareVersion(new HardwareVersion(21, 4724)).getBytes());
  }

  @Test
  public void testWriteSoftwareVersion() {
    assertArrayEquals(
        new byte[] {0x04, 0x02, 0x20, 0x01},
        new PayloadWriter(4).writeSoftwareVersion(new SoftwareVersion(4, 2, 288)).getBytes());
  }
}
