package com.kolibree.android.app.test.mocks;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.net.Uri;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Map;

/** Mock utilities */
@Keep
public final class MocksUtils {

  private MocksUtils() {}

  @NonNull
  public static Uri mockUri(
      @NonNull String scheme, @NonNull String host, @Nullable Map<String, String> parameters) {

    final Uri uri = mock(Uri.class);
    when(uri.toString()).thenReturn(scheme + host);
    when(uri.getScheme()).thenReturn(scheme);
    when(uri.getHost()).thenReturn(host);
    when(uri.getQueryParameter(anyString()))
        .thenAnswer(
            invocation -> {
              if (parameters == null) {
                return null;
              }

              final String parameterName = invocation.getArgument(0);
              return parameters.get(parameterName);
            });

    return uri;
  }
}
