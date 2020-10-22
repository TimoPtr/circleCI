package com.kolibree.android.network.retrofit;

import static com.kolibree.android.network.NetworkConstants.DEFAULT_HTTP_CONNECTION_TIMEOUT;
import static com.kolibree.android.network.NetworkConstants.DEFAULT_HTTP_READ_TIMEOUT;
import static com.kolibree.android.network.NetworkConstants.DEFAULT_HTTP_WRITE_TIMEOUT;

import android.annotation.SuppressLint;
import com.google.common.base.Optional;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kolibree.android.accountinternal.account.ParentalConsent;
import com.kolibree.android.accountinternal.internal.AccountInternal;
import com.kolibree.android.accountinternal.internal.AccountInternalAdapter;
import com.kolibree.android.accountinternal.profile.persistence.ProfileInternalAdapter;
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.feature.FeatureToggle;
import com.kolibree.android.feature.FeatureToggleSetExtKt;
import com.kolibree.android.network.NetworkInterceptorModule;
import com.kolibree.android.network.NetworkLogFeature;
import com.kolibree.android.network.core.AccessTokenManager;
import com.kolibree.android.network.core.AccessTokenManagerImpl;
import com.kolibree.android.network.environment.Endpoint;
import com.kolibree.android.network.errorhandler.ErrorHandlerInterceptor;
import com.kolibree.android.network.token.TokenApi;
import com.kolibree.android.network.token.TokenRefresher;
import com.kolibree.android.network.token.TokenRefresherImpl;
import com.kolibree.retrofit.ParentalConsentTypeAdapter;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.Multibinds;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/** Created by miguelaragues on 25/5/18. */
@Module(includes = NetworkInterceptorModule.class)
public abstract class RetrofitModule {

  @Provides
  @AppScope
  static Retrofit providesRetrofit(Endpoint endpoint, OkHttpClient okHttpClient, Gson gson) {
    return new Retrofit.Builder()
        .baseUrl(endpoint.url())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(CustomRetrofitConverterFactory.create())
        .client(okHttpClient)
        .build();
  }

  @Provides
  @AppScope
  static Gson providesGson() {
    Gson gson =
        new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapterFactory(CustomTypeAdapterFactory.getFACTORY())
            // It can't be part of the factory as they are not TypeAdapter
            .registerTypeAdapter(ParentalConsent.class, new ParentalConsentTypeAdapter())
            .create();

    gson =
        gson.newBuilder()
            .registerTypeAdapter(ProfileInternal.class, new ProfileInternalAdapter(gson))
            .create();

    return gson.newBuilder()
        .registerTypeAdapter(AccountInternal.class, new AccountInternalAdapter(gson))
        .create();
  }

  @SuppressLint("ExperimentalClassUse")
  @Provides
  @AppScope
  static OkHttpClient providesOkHttpClient(
      ConnectivityInterceptor connectivityInterceptor,
      HeaderInterceptor headerInterceptor,
      HostSelectionInterceptor hostInterceptor,
      TokenAuthenticator tokenAuthenticator,
      ErrorHandlerInterceptor errorHandlerInterceptor,
      CurlLoggingInterceptor curlLoggingInterceptor,
      Optional<Interceptor> networkLogsInterceptor,
      Set<FeatureToggle<?>> featureToggles,
      Set<Interceptor> networkInterceptors) {
    OkHttpClient.Builder okClientBuilder = new OkHttpClient.Builder();

    okClientBuilder.addInterceptor(connectivityInterceptor);
    okClientBuilder.addInterceptor(hostInterceptor);
    okClientBuilder.addInterceptor(headerInterceptor);
    okClientBuilder.authenticator(tokenAuthenticator);
    okClientBuilder.addInterceptor(errorHandlerInterceptor);

    if (networkLogsInterceptor.isPresent()) {
      okClientBuilder.addInterceptor(networkLogsInterceptor.get());
    }

    okClientBuilder.connectTimeout(DEFAULT_HTTP_CONNECTION_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    okClientBuilder.readTimeout(DEFAULT_HTTP_READ_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    okClientBuilder.writeTimeout(DEFAULT_HTTP_WRITE_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

    if (FeatureToggleSetExtKt.toggleForFeature(featureToggles, NetworkLogFeature.INSTANCE)
        .getValue()) {
      okClientBuilder.addInterceptor(curlLoggingInterceptor);
      okClientBuilder.addInterceptor(initHttpLoggingInterceptor());
    }

    for (Interceptor interceptor : networkInterceptors) {
      okClientBuilder.addNetworkInterceptor(interceptor);
    }

    return okClientBuilder.build();
  }

  private static HttpLoggingInterceptor initHttpLoggingInterceptor() {
    HttpLoggingInterceptor.Level logLevel = HttpLoggingInterceptor.Level.BODY;
    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(logLevel);
    return interceptor;
  }

  @Provides
  static TokenApi providesTokenApi(Retrofit retrofit) {
    return retrofit.create(TokenApi.class);
  }

  @Binds
  @AppScope
  abstract AccessTokenManager bindsAccessTokenManager(AccessTokenManagerImpl accessTokenManager);

  @Binds
  abstract TokenRefresher bindsTokenRefresher(TokenRefresherImpl tokenRefresher);

  @Multibinds
  abstract Set<Interceptor> bindDefaultInterceptors();
}
