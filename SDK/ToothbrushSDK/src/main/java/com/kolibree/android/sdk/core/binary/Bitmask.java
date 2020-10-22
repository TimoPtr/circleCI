package com.kolibree.android.sdk.core.binary;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

/**
 * Created by aurelien on 06/12/16.
 *
 * <p>Byte bitmask
 */
@Keep
public final class Bitmask {

  /** Byte value */
  private byte bitmask;

  public Bitmask() {}

  public Bitmask(byte initialValue) {
    bitmask = initialValue;
  }

  /**
   * Set a bit in the bitmask
   *
   * @param bitIndex index of bit (0 to 7)
   * @param value bit value
   * @return this instance to chain calls
   */
  @NonNull
  public synchronized Bitmask set(int bitIndex, boolean value) {
    checkBitIndex(bitIndex);
    final byte mask = (byte) Math.pow(2f, bitIndex);

    if (value) {
      bitmask |= mask;
    } else {
      bitmask &= ~mask;
    }

    return this;
  }

  /**
   * Get bitmask
   *
   * @return byte-wrapped bitmask
   */
  public synchronized byte get() {
    return bitmask;
  }

  /**
   * Get the bit at given index
   *
   * @param bitIndex [0, 8[ bit index
   * @return bit value
   */
  public synchronized boolean getBit(int bitIndex) {
    checkBitIndex(bitIndex);
    return (bitmask & (byte) Math.pow(2f, bitIndex)) != 0;
  }

  /**
   * Get the low nibble of the inner byte
   *
   * <p>Example low nibble of 11110000 is 0000, low nibble of 0xAB is 0x0B
   *
   * @return byte-wrapped low nibble
   */
  public synchronized byte getLowNibble() {
    return (byte) (bitmask & 0x0F);
  }

  /**
   * Set the low nibble of the inner byte
   *
   * <p>Example low nibble of 11110000 is 0000, low nibble of 0xAB is 0x0B
   *
   * @param lowNibble byte-wrapped low nibble
   * @return this instance
   */
  @NonNull
  public synchronized Bitmask setLowNibble(byte lowNibble) {
    checkNibble(lowNibble);
    bitmask &= 0xF0;
    bitmask |= (lowNibble & 0x0F);

    return this;
  }

  /**
   * Get the high nibble of the inner byte
   *
   * <p>Example high nibble of 11110000 is 1111, high nibble of 0xAB is 0x0A
   *
   * @return byte-wrapped high nibble
   */
  public synchronized byte getHighNibble() {
    // The unsigned shift (>>>) fills the upper bits with zero, regardless of the sign
    return (byte) ((bitmask >>> 4) & 0x0F);
  }

  /**
   * Set the high nibble of the inner byte
   *
   * <p>Example high nibble of 11110000 is 1111, high nibble of 0xAB is 0x0A
   *
   * @param highNibble byte-wrapped high nibble
   * @return this instance
   */
  @NonNull
  public synchronized Bitmask setHighNibble(byte highNibble) {
    checkNibble(highNibble);
    bitmask &= 0x0F;
    bitmask |= ((highNibble << 4) & 0xF0);

    return this;
  }

  private static void checkBitIndex(int bitIndex) {
    if (bitIndex < 0 || bitIndex > 7) {
      throw new IllegalArgumentException("Invalid bit index " + bitIndex);
    }
  }

  private static void checkNibble(byte nibble) {
    if (nibble < 0x0 || nibble > 0xF) {
      throw new IllegalArgumentException("Nibble values must be in [0x0, 0xF]");
    }
  }
}
