/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.utils;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

/** Created by miguelaragues on 16/3/18. */
@Keep
public class FileDownloader {

  @VisibleForTesting public static final String KOLIBREE_FILES_CACHE = "kolibree_cached_files";

  private final Context context;

  @VisibleForTesting final List<Call> ongoingCalls = new ArrayList<>();

  @Inject
  public FileDownloader(Context context) {
    this.context = context.getApplicationContext();
  }

  /**
   * Downloads the file located in the specified url and returns the File where it's stored
   *
   * <p>If a file with the expected name already exists, it returns the File without downloading it
   */
  @NonNull
  public File download(@NonNull String url) throws IOException {
    return download(new URL(url));
  }

  /**
   * Downloads the file located in the specified url and returns the File where it's stored
   *
   * <p>The file name will be the last segment of the url. If a file with the expected name already
   * exists, it returns the File without downloading it
   */
  @NonNull
  public File download(@NonNull URL url) throws IOException {
    return download(url, extractFileName(url));
  }

  /**
   * Downloads the file located in the specified url to the specified file name and returns the File
   * where it's stored
   *
   * <p>If a file with the name already exists, it returns the File without downloading it
   */
  @NonNull
  public File download(@NonNull String url, @NonNull String filename) throws IOException {
    return download(new URL(url), filename);
  }

  /**
   * Downloads the file located in the specified url to the specified file name and returns the File
   * where it's stored
   *
   * <p>If a file with the name already exists, it returns the File without downloading it
   */
  @NonNull
  public File download(@NonNull URL url, @NonNull String filename) throws IOException {
    File file = new File(createDefaultCacheDir(), filename);

    if (fileIsNotEmpty(file)) {
      return file;
    }

    Request request = new Request.Builder().url(url).build();

    OkHttpClient client = new Builder().build();

    Call call = client.newCall(request);
    synchronized (ongoingCalls) {
      ongoingCalls.add(call);
    }
    try {
      try (Response response = call.execute()) {
        if (!response.isSuccessful()) {
          throw new IOException("Unexpected code " + response + " attempting to download " + url);
        }

        if (bodyIsEmpty(response)) {
          throw new IOException("Unexpected empty body attempting to download " + url);
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
          //noinspection ConstantConditions
          fos.write(response.body().bytes());

          synchronized (ongoingCalls) {
            ongoingCalls.remove(call);
          }
        }

        return file;
      }
    } catch (IOException ioexception) {
      //noinspection ResultOfMethodCallIgnored
      file.delete();

      throw ioexception;
    }
  }

  private boolean bodyIsEmpty(Response response) {
    return response.body() == null || response.body().contentLength() == 0;
  }

  private boolean fileIsNotEmpty(File file) {
    return file.exists() && file.isFile() && file.length() > 0;
  }

  public void cancelRequests() {
    synchronized (ongoingCalls) {
      for (Call request : ongoingCalls) {
        if (!request.isCanceled()) request.cancel();
      }

      ongoingCalls.clear();
    }
  }

  @NonNull
  private String extractFileName(URL url) {
    return Uri.parse(url.toString()).getLastPathSegment();
  }

  private File createDefaultCacheDir() {
    File cache = new File(context.getApplicationContext().getCacheDir(), KOLIBREE_FILES_CACHE);
    if (!cache.exists()) {
      //noinspection ResultOfMethodCallIgnored
      cache.mkdirs();
    }
    return cache;
  }
}
