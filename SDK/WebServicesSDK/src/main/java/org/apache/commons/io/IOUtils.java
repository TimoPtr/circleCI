/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io;

import android.annotation.SuppressLint;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import org.apache.commons.io.output.StringBuilderWriter;

/**
 * General IO stream manipulation utilities.
 *
 * <p>This class provides static utility methods for input/output operations.
 *
 * <ul>
 *   <li><b>[Deprecated]</b> closeQuietly - these methods close a stream ignoring nulls and
 *       exceptions
 *   <li>toXxx/read - these methods read data from a stream
 *   <li>write - these methods write data to a stream
 *   <li>copy - these methods copy all the data from one stream to another
 *   <li>contentEquals - these methods compare the content of two streams
 * </ul>
 *
 * <p>The byte-to-char methods and char-to-byte methods involve a conversion step. Two methods are
 * provided in each case, one that uses the platform default encoding and the other which allows you
 * to specify an encoding. You are encouraged to always specify an encoding because relying on the
 * platform default can lead to unexpected results, for example when moving from development to
 * production.
 *
 * <p>All the methods in this class that read a stream are buffered internally. This means that
 * there is no cause to use a <code>BufferedInputStream</code> or <code>BufferedReader</code>. The
 * default buffer size of 4K has been shown to be efficient in tests.
 *
 * <p>The various copy methods all delegate the actual copying to one of the following methods:
 *
 * <ul>
 *   <li>{@link #copyLarge(InputStream, OutputStream, byte[])}
 *   <li>{@link #copyLarge(Reader, Writer, char[])}
 * </ul>
 *
 * For example, {@link #copy(InputStream, OutputStream)} calls {@link #copyLarge(InputStream,
 * OutputStream)} which calls {@link #copy(InputStream, OutputStream, int)} which creates the buffer
 * and calls {@link #copyLarge(InputStream, OutputStream, byte[])}.
 *
 * <p>Applications can re-use buffers by using the underlying methods directly. This may improve
 * performance for applications that need to do a lot of copying.
 *
 * <p>Wherever possible, the methods in this class do <em>not</em> flush or close the stream. This
 * is to avoid making non-portable assumptions about the streams' origin and further use. Thus the
 * caller is still responsible for closing streams after use.
 *
 * <p>Origin of code: Excalibur.
 */
@SuppressLint({"SdkPublicClassInNonKolibreePackage", "DeobfuscatedPublicSdkClass"})
public class IOUtils {
  // NOTE: This class is focused on InputStream, OutputStream, Reader and
  // Writer. Each method should take at least one of these as a parameter,
  // or return one of them.

  /**
   * Represents the end-of-file (or stream).
   *
   * @since 2.5 (made public)
   */
  public static final int EOF = -1;

  /** The system line separator string. */
  public static final String LINE_SEPARATOR;
  /**
   * The default buffer size ({@value}) to use for {@link #copyLarge(InputStream, OutputStream)} and
   * {@link #copyLarge(Reader, Writer)}
   */
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
  /** The default buffer size to use for the skip() methods. */
  private static final int SKIP_BUFFER_SIZE = 2048;
  // Allocated in the relevant skip method if necessary.
  /*
   * These buffers are static and are shared between threads.
   * This is possible because the buffers are write-only - the contents are never read.
   *
   * N.B. there is no need to synchronize when creating these because:
   * - we don't care if the buffer is created multiple times (the data is ignored)
   * - we always use the same size buffer, so if it it is recreated it will still be OK
   * (if the buffer size were variable, we would need to synch. to ensure some other thread
   * did not create a smaller one)
   */
  private static char[] SKIP_CHAR_BUFFER;
  private static byte[] SKIP_BYTE_BUFFER;

  static {
    // avoid security issues
    StringBuilderWriter buf = null;
    PrintWriter out = null;
    try {
      buf = new StringBuilderWriter(4);
      out = new PrintWriter(buf);
      out.println();
      LINE_SEPARATOR = buf.toString();
    } finally {
      if (buf != null) {
        buf.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }

  /** Instances should NOT be constructed in standard programming. */
  public IOUtils() {
    super();
  }

  // -----------------------------------------------------------------------

  /**
   * Closes a URLConnection.
   *
   * @param conn the connection to close.
   * @since 2.4
   */
  public static void close(final URLConnection conn) {
    if (conn instanceof HttpURLConnection) {
      ((HttpURLConnection) conn).disconnect();
    }
  }

  // read toString
  // -----------------------------------------------------------------------

  /**
   * Gets the contents of an <code>InputStream</code> as a String using the default character
   * encoding of the platform.
   *
   * <p>This method buffers the input internally, so there is no need to use a <code>
   * BufferedInputStream</code>.
   *
   * @param input the <code>InputStream</code> to read from
   * @return the requested String
   * @throws NullPointerException if the input is null
   * @throws IOException if an I/O error occurs
   * @deprecated 2.5 use {@link #toString(InputStream, Charset)} instead
   */
  @Deprecated
  public static String toString(final InputStream input) throws IOException {
    return toString(input, Charset.defaultCharset());
  }

  /**
   * Gets the contents of an <code>InputStream</code> as a String using the specified character
   * encoding.
   *
   * <p>This method buffers the input internally, so there is no need to use a <code>
   * BufferedInputStream</code>.
   *
   * @param input the <code>InputStream</code> to read from
   * @param encoding the encoding to use, null means platform default
   * @return the requested String
   * @throws NullPointerException if the input is null
   * @throws IOException if an I/O error occurs
   * @since 2.3
   */
  public static String toString(final InputStream input, final Charset encoding)
      throws IOException {
    StringBuilderWriter sw = null;
    try {
      sw = new StringBuilderWriter();
      copy(input, sw, encoding);
      return sw.toString();
    } finally {
      if (sw != null) {
        sw.close();
      }
    }
  }

  /**
   * Gets the contents of an <code>InputStream</code> as a String using the specified character
   * encoding.
   *
   * <p>Character encoding names can be found at <a
   * href="http://www.iana.org/assignments/character-sets">IANA</a>.
   *
   * <p>This method buffers the input internally, so there is no need to use a <code>
   * BufferedInputStream</code>.
   *
   * @param input the <code>InputStream</code> to read from
   * @param encoding the encoding to use, null means platform default
   * @return the requested String
   * @throws NullPointerException if the input is null
   * @throws IOException if an I/O error occurs
   * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
   *     .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
   */
  public static String toString(final InputStream input, final String encoding) throws IOException {
    return toString(input, Charsets.toCharset(encoding));
  }

  /**
   * Gets the contents at the given URI.
   *
   * @param uri The URI source.
   * @param encoding The encoding name for the URL contents.
   * @return The contents of the URL as a String.
   * @throws IOException if an I/O exception occurs.
   * @since 2.3.
   */
  public static String toString(final URI uri, final Charset encoding) throws IOException {
    return toString(uri.toURL(), Charsets.toCharset(encoding));
  }

  /**
   * Gets the contents at the given URL.
   *
   * @param url The URL source.
   * @param encoding The encoding name for the URL contents.
   * @return The contents of the URL as a String.
   * @throws IOException if an I/O exception occurs.
   * @since 2.3
   */
  public static String toString(final URL url, final Charset encoding) throws IOException {
    InputStream inputStream = null;
    try {
      inputStream = url.openStream();
      return toString(inputStream, encoding);
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  // lineIterator
  // -----------------------------------------------------------------------

  // write byte[]
  // -----------------------------------------------------------------------

  /**
   * Writes bytes from a <code>byte[]</code> to chars on a <code>Writer</code> using the specified
   * character encoding.
   *
   * <p>This method uses {@link String#String(byte[], String)}.
   *
   * @param data the byte array to write, do not modify during output, null ignored
   * @param output the <code>Writer</code> to write to
   * @param encoding the encoding to use, null means platform default
   * @throws NullPointerException if output is null
   * @throws IOException if an I/O error occurs
   * @since 2.3
   */
  public static void write(final byte[] data, final Writer output, final Charset encoding)
      throws IOException {
    if (data != null) {
      output.write(new String(data, Charsets.toCharset(encoding)));
    }
  }

  // write char[]
  // -----------------------------------------------------------------------

  /**
   * Writes chars from a <code>char[]</code> to bytes on an <code>OutputStream</code> using the
   * specified character encoding.
   *
   * <p>This method uses {@link String#String(char[])} and {@link String#getBytes(String)}.
   *
   * @param data the char array to write, do not modify during output, null ignored
   * @param output the <code>OutputStream</code> to write to
   * @param encoding the encoding to use, null means platform default
   * @throws NullPointerException if output is null
   * @throws IOException if an I/O error occurs
   * @since 2.3
   */
  public static void write(final char[] data, final OutputStream output, final Charset encoding)
      throws IOException {
    if (data != null) {
      output.write(new String(data).getBytes(Charsets.toCharset(encoding)));
    }
  }

  // write CharSequence
  // -----------------------------------------------------------------------

  /**
   * Writes chars from a <code>CharSequence</code> to bytes on an <code>OutputStream</code> using
   * the specified character encoding.
   *
   * <p>This method uses {@link String#getBytes(String)}.
   *
   * @param data the <code>CharSequence</code> to write, null ignored
   * @param output the <code>OutputStream</code> to write to
   * @param encoding the encoding to use, null means platform default
   * @throws NullPointerException if output is null
   * @throws IOException if an I/O error occurs
   * @since 2.3
   */
  public static void write(
      final CharSequence data, final OutputStream output, final Charset encoding)
      throws IOException {
    if (data != null) {
      write(data.toString(), output, encoding);
    }
  }

  // write String
  // -----------------------------------------------------------------------

  /**
   * Writes chars from a <code>String</code> to bytes on an <code>OutputStream</code> using the
   * specified character encoding.
   *
   * <p>This method uses {@link String#getBytes(String)}.
   *
   * @param data the <code>String</code> to write, null ignored
   * @param output the <code>OutputStream</code> to write to
   * @param encoding the encoding to use, null means platform default
   * @throws NullPointerException if output is null
   * @throws IOException if an I/O error occurs
   * @since 2.3
   */
  public static void write(final String data, final OutputStream output, final Charset encoding)
      throws IOException {
    if (data != null) {
      output.write(data.getBytes(Charsets.toCharset(encoding)));
    }
  }

  // copy from InputStream
  // -----------------------------------------------------------------------

  /**
   * Copies bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
   *
   * <p>This method buffers the input internally, so there is no need to use a <code>
   * BufferedInputStream</code>.
   *
   * <p>Large streams (over 2GB) will return a bytes copied value of <code>-1</code> after the copy
   * has completed since the correct number of bytes cannot be returned as an int. For large streams
   * use the <code>copyLarge(InputStream, OutputStream)</code> method.
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 1.1
   */
  public static int copy(final InputStream input, final OutputStream output) throws IOException {
    final long count = copyLarge(input, output);
    if (count > Integer.MAX_VALUE) {
      return -1;
    }
    return (int) count;
  }

  /**
   * Copies bytes from an <code>InputStream</code> to an <code>OutputStream</code> using an internal
   * buffer of the given size.
   *
   * <p>This method buffers the input internally, so there is no need to use a <code>
   * BufferedInputStream</code>.
   *
   * <p>
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   * @param bufferSize the bufferSize used to copy from the input to the output
   * @return the number of bytes copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 2.5
   */
  public static long copy(final InputStream input, final OutputStream output, final int bufferSize)
      throws IOException {
    return copyLarge(input, output, new byte[bufferSize]);
  }

  /**
   * Copies bytes from a large (over 2GB) <code>InputStream</code> to an <code>OutputStream</code>.
   *
   * <p>This method buffers the input internally, so there is no need to use a <code>
   * BufferedInputStream</code>.
   *
   * <p>The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   * @return the number of bytes copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 1.3
   */
  public static long copyLarge(final InputStream input, final OutputStream output)
      throws IOException {
    return copy(input, output, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Copies bytes from a large (over 2GB) <code>InputStream</code> to an <code>OutputStream</code>.
   *
   * <p>This method uses the provided buffer, so there is no need to use a <code>BufferedInputStream
   * </code>.
   *
   * <p>
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   * @param buffer the buffer to use for the copy
   * @return the number of bytes copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 2.2
   */
  public static long copyLarge(
      final InputStream input, final OutputStream output, final byte[] buffer) throws IOException {
    long count = 0;
    int n;
    while (EOF != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  /**
   * Copies bytes from an <code>InputStream</code> to chars on a <code>Writer</code> using the
   * specified character encoding.
   *
   * <p>This method buffers the input internally, so there is no need to use a <code>
   * BufferedInputStream</code>.
   *
   * <p>This method uses {@link InputStreamReader}.
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>Writer</code> to write to
   * @param inputEncoding the encoding to use for the input stream, null means platform default
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 2.3
   */
  public static void copy(final InputStream input, final Writer output, final Charset inputEncoding)
      throws IOException {
    final InputStreamReader in = new InputStreamReader(input, Charsets.toCharset(inputEncoding));
    copy(in, output);
  }

  // copy from Reader
  // -----------------------------------------------------------------------

  /**
   * Copies chars from a <code>Reader</code> to a <code>Writer</code>.
   *
   * <p>This method buffers the input internally, so there is no need to use a <code>BufferedReader
   * </code>.
   *
   * <p>Large streams (over 2GB) will return a chars copied value of <code>-1</code> after the copy
   * has completed since the correct number of chars cannot be returned as an int. For large streams
   * use the <code>copyLarge(Reader, Writer)</code> method.
   *
   * @param input the <code>Reader</code> to read from
   * @param output the <code>Writer</code> to write to
   * @return the number of characters copied, or -1 if &gt; Integer.MAX_VALUE
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 1.1
   */
  public static int copy(final Reader input, final Writer output) throws IOException {
    final long count = copyLarge(input, output);
    if (count > Integer.MAX_VALUE) {
      return -1;
    }
    return (int) count;
  }

  /**
   * Copies chars from a large (over 2GB) <code>Reader</code> to a <code>Writer</code>.
   *
   * <p>This method buffers the input internally, so there is no need to use a <code>BufferedReader
   * </code>.
   *
   * <p>The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
   *
   * @param input the <code>Reader</code> to read from
   * @param output the <code>Writer</code> to write to
   * @return the number of characters copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 1.3
   */
  public static long copyLarge(final Reader input, final Writer output) throws IOException {
    return copyLarge(input, output, new char[DEFAULT_BUFFER_SIZE]);
  }

  /**
   * Copies chars from a large (over 2GB) <code>Reader</code> to a <code>Writer</code>.
   *
   * <p>This method uses the provided buffer, so there is no need to use a <code>BufferedReader
   * </code>.
   *
   * <p>
   *
   * @param input the <code>Reader</code> to read from
   * @param output the <code>Writer</code> to write to
   * @param buffer the buffer to be used for the copy
   * @return the number of characters copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 2.2
   */
  public static long copyLarge(final Reader input, final Writer output, final char[] buffer)
      throws IOException {
    long count = 0;
    int n;
    while (EOF != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  // content equals
  // -----------------------------------------------------------------------

  /**
   * Reads characters from an input character stream. This implementation guarantees that it will
   * read as many characters as possible before giving up; this may not always be the case for
   * subclasses of {@link Reader}.
   *
   * @param input where to read input from
   * @param buffer destination
   * @param offset initial offset into buffer
   * @param length length to read, must be &gt;= 0
   * @return actual length read; may be less than requested if EOF was reached
   * @throws IOException if a read error occurs
   * @since 2.2
   */
  public static int read(
      final Reader input, final char[] buffer, final int offset, final int length)
      throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Length must not be negative: " + length);
    }
    int remaining = length;
    while (remaining > 0) {
      final int location = length - remaining;
      final int count = input.read(buffer, offset + location, remaining);
      if (EOF == count) { // EOF
        break;
      }
      remaining -= count;
    }
    return length - remaining;
  }

  /**
   * Reads bytes from an input stream. This implementation guarantees that it will read as many
   * bytes as possible before giving up; this may not always be the case for subclasses of {@link
   * InputStream}.
   *
   * @param input where to read input from
   * @param buffer destination
   * @param offset initial offset into buffer
   * @param length length to read, must be &gt;= 0
   * @return actual length read; may be less than requested if EOF was reached
   * @throws IOException if a read error occurs
   * @since 2.2
   */
  public static int read(
      final InputStream input, final byte[] buffer, final int offset, final int length)
      throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Length must not be negative: " + length);
    }
    int remaining = length;
    while (remaining > 0) {
      final int location = length - remaining;
      final int count = input.read(buffer, offset + location, remaining);
      if (EOF == count) { // EOF
        break;
      }
      remaining -= count;
    }
    return length - remaining;
  }
}
