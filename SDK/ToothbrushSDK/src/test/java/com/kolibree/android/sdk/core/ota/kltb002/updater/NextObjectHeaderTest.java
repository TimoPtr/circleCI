/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.core.ota.kltb002.updater.NextObjectHeaderKt.BYTES_PER_CHUNK;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.NextObjectHeaderKt.CHUNKS_PER_OBJECT;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.kolibree.android.app.test.BaseUnitTest;
import java.util.Arrays;
import org.junit.Test;

/** Created by miguelaragues on 13/4/18. */
@SuppressWarnings("KotlinInternalInJava")
public class NextObjectHeaderTest extends BaseUnitTest {

  @Test
  public void
      create_chunksRemainingOverCHUNKS_PER_OBJECT_0ChunksTransmitted_createsHeaderWithMaxNumberOfChunks() {
    int chunksRemaining = CHUNKS_PER_OBJECT + 1;
    byte[] data = createData(chunksRemaining);

    byte[] expectedData = createData(CHUNKS_PER_OBJECT);

    int chunksTransmitted = 0;
    NextObjectHeader header = NextObjectHeader.create(data, chunksTransmitted, chunksRemaining);

    assertArrayEquals(expectedData, header.dataToSend());
    assertEquals(CHUNKS_PER_OBJECT, header.numberOfChunksToSend());
    assertEquals(BYTES_PER_CHUNK, header.bytesInLastChunk());
    assertFalse(header.isLastObject());
  }

  @Test
  public void
      create_chunksRemainingBelowCHUNKS_PER_OBJECT_withChunksTransmitted_createsHeaderWithExpectedOffset() {
    int chunksRemaining = 5;
    int size = CHUNKS_PER_OBJECT + chunksRemaining;
    byte[] data = createData(size);

    /*
    Fill last chunks with value 1, and the expected data is a 100 length array of 1's
     */
    int remainingChunksIndex = (size - chunksRemaining) * BYTES_PER_CHUNK;
    Arrays.fill(data, remainingChunksIndex, data.length, (byte) 1);

    byte[] expectedData = createData(chunksRemaining);
    Arrays.fill(expectedData, (byte) 1);

    int chunksTransmitted = CHUNKS_PER_OBJECT;
    NextObjectHeader header = NextObjectHeader.create(data, chunksTransmitted, chunksRemaining);

    assertArrayEquals(expectedData, header.dataToSend());
    assertEquals(chunksRemaining, header.numberOfChunksToSend());
    assertEquals(BYTES_PER_CHUNK, header.bytesInLastChunk());
    assertTrue(header.isLastObject());
  }

  @Test
  public void
      create_chunksRemainingBelowCHUNKS_PER_OBJECT_0ChunksTransmitted_createsHeaderWithRemainingNumberOfChunks() {
    int chunksRemaining = CHUNKS_PER_OBJECT - 5;
    byte[] data = createData(chunksRemaining);

    int chunksTransmitted = 0;
    NextObjectHeader header = NextObjectHeader.create(data, chunksTransmitted, chunksRemaining);

    assertArrayEquals(data, header.dataToSend());
    assertEquals(chunksRemaining, header.numberOfChunksToSend());
    assertEquals(BYTES_PER_CHUNK, header.bytesInLastChunk());
    assertTrue(header.isLastObject());
  }

  @Test
  public void
      create_chunksRemainingBelowCHUNKS_PER_OBJECT_withLastChunkLessThanBYTES_PER_CHUNK_createsWithExpectedBytesInLastChunk() {
    int chunksRemaining = CHUNKS_PER_OBJECT - 5;
    int expectedLastBytes = 7;
    byte[] data = new byte[chunksRemaining * BYTES_PER_CHUNK + expectedLastBytes];

    int chunksTransmitted = 0;
    NextObjectHeader header = NextObjectHeader.create(data, chunksTransmitted, chunksRemaining);

    assertArrayEquals(data, header.dataToSend());
    assertEquals(chunksRemaining, header.numberOfChunksToSend());
    assertEquals(expectedLastBytes, header.bytesInLastChunk());
    assertTrue(header.isLastObject());
  }

  private byte[] createData(int chunks) {
    return new byte[chunks * BYTES_PER_CHUNK];
  }
}
