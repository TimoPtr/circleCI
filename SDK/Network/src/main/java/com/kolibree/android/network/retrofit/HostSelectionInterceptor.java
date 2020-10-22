package com.kolibree.android.network.retrofit;

import android.net.Uri;
import com.kolibree.android.network.NetworkExtensionsKt;
import com.kolibree.android.network.environment.Endpoint;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Provider;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;

/**
 * Support for dynamic host changes, such as Kolibree's secret settings
 *
 * <p>I haven't been able to test it in the application
 *
 * <p>Inspired by
 * https://gist.github.com/swankjesse/8571a8207a5815cca1fb#file-hostselectioninterceptor-java
 */
class HostSelectionInterceptor implements Interceptor {

  private final Provider<Endpoint> endpointProvider;

  @Inject
  HostSelectionInterceptor(Provider<Endpoint> endpointProvider) {
    this.endpointProvider = endpointProvider;
  }

  @Override
  public okhttp3.Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    String url = endpointProvider.get().url();

    Uri uri = Uri.parse(url);
    if (uri != null) {
      HttpUrl newUrl =
          request
              .url()
              .newBuilder()
              .scheme(uri.getScheme())
              .host(uri.getHost())
              .port(NetworkExtensionsKt.pickTheRightPort(request.url(), uri))
              .build();
      request = request.newBuilder().url(newUrl).build();
    }

    return chain.proceed(request);
  }
}
