package com.kolibree.android.test;

import androidx.annotation.NonNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public abstract class SharedTestUtils {

  private SharedTestUtils() {}

  public static String getResourcePath(@NonNull String path) {
    return SharedTestUtils.class.getClassLoader().getResource(path).toString();
  }

  public static String getJson(@NonNull String path) throws IOException {
    try (InputStream is = SharedTestUtils.class.getClassLoader().getResourceAsStream(path)) {
      byte[] encoded = readBytes(is);

      return new String(encoded, Charset.defaultCharset());
    }
  }

  private static byte[] readBytes(InputStream inputStream) throws IOException {
    byte[] b = new byte[1024];
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    int c;
    while ((c = inputStream.read(b)) != -1) {
      os.write(b, 0, c);
    }
    return os.toByteArray();
  }
}
