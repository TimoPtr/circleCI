package com.kolibree.sdkws.core;

import static com.kolibree.android.extensions.LocaleExtensionsKt.getAcceptedLanguageHeader;
import static com.kolibree.android.network.HttpHeaderConstantsKt.ACCEPT_LANGUAGE;
import static com.kolibree.android.network.HttpHeaderConstantsKt.API_CLIENT_ACCESS_TOKEN;
import static com.kolibree.android.network.HttpHeaderConstantsKt.API_CLIENT_ID_HEADER;
import static com.kolibree.android.network.HttpHeaderConstantsKt.API_CLIENT_SIG_HEADER;
import static com.kolibree.android.network.HttpHeaderConstantsKt.API_DATA_TYPE;
import static com.kolibree.android.network.HttpHeaderConstantsKt.API_DEVICE_ID;
import static com.kolibree.android.network.HttpHeaderConstantsKt.API_DEVICE_PARAMETERS;
import static java.net.HttpURLConnection.HTTP_OK;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.accountinternal.internal.AccountInternal;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.auditor.Auditor;
import com.kolibree.android.feature.FeatureToggle;
import com.kolibree.android.feature.FeatureToggleSetExtKt;
import com.kolibree.android.network.NetworkConstants;
import com.kolibree.android.network.NetworkExtensionsKt;
import com.kolibree.android.network.NetworkLogFeature;
import com.kolibree.android.network.NetworkLogFeatureToggle;
import com.kolibree.android.network.api.ApiError;
import com.kolibree.android.network.api.ApiErrorCode;
import com.kolibree.android.network.api.response.RefreshTokenResponse;
import com.kolibree.android.network.core.AccessTokenManager;
import com.kolibree.android.network.core.capabilities.AcceptCapabilitiesHeaderProvider;
import com.kolibree.android.network.core.useragent.UserAgentHeaderProvider;
import com.kolibree.android.network.environment.Credentials;
import com.kolibree.android.network.environment.Endpoint;
import com.kolibree.android.network.errorhandler.NetworkErrorHandler;
import com.kolibree.android.network.retrofit.DeviceParameters;
import com.kolibree.android.network.utils.NetworkChecker;
import com.kolibree.sdkws.KolibreeUtils;
import com.kolibree.sdkws.api.request.RefreshTokenRequest;
import com.kolibree.sdkws.api.request.Request;
import com.kolibree.sdkws.data.model.RefreshTokenData;
import com.kolibree.sdkws.networking.Response;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import kotlin.Pair;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NonNls;
import org.json.JSONException;
import timber.log.Timber;

/**
 * Responsible for doing remote calls to the backend. It deals with
 *
 * <ul>
 *   <li>Request Header
 *   <li>ClientId + Url encryption
 *   <li>Handle some error scenarios and recover from then, such as
 *       <ul>
 *         <li>Refresh tokens when they expire
 *         <li>Notify App force update
 *       </ul>
 * </ul>
 *
 * Deprecated: Use Retrofit instance created by RetrofitModule for new backend calls.
 */
@Deprecated
class BackendInteractor {

  private static final String FIELD_CONTENT_TYPE = "Content-Type";

  private final NetworkChecker networkChecker;
  private final SynchronizationScheduler synchronizationScheduler;
  private final Provider<Credentials> credentialsProvider;
  private final Provider<Endpoint> endpointProvider;
  private final UserAgentHeaderProvider userAgentHeaderProvider;
  private final AcceptCapabilitiesHeaderProvider acceptCapabilitiesHeaderProvider;
  private final AccessTokenManager accessTokenManager;
  private final KolibreeUtils kolibreeUtils;
  private final DeviceParameters deviceParameters;
  private final AccountDatastore accountDatastore;
  private final InternalForceAppUpdater forceAppUpdater;
  private final NetworkErrorHandler networkErrorHandler;
  private final NetworkLogFeatureToggle networkLogFeatureToggle;

  @Inject
  BackendInteractor(
      KolibreeUtils kolibreeUtils,
      Provider<Credentials> credentialsProvider,
      Provider<Endpoint> endpointProvider,
      NetworkChecker networkChecker,
      SynchronizationScheduler synchronizationScheduler,
      UserAgentHeaderProvider userAgentHeaderProvider,
      AcceptCapabilitiesHeaderProvider acceptCapabilitiesHeaderProvider,
      AccessTokenManager accessTokenManager,
      DeviceParameters deviceParameters,
      AccountDatastore accountDatastore,
      InternalForceAppUpdater forceAppUpdater,
      NetworkErrorHandler networkErrorHandler,
      Set<FeatureToggle<?>> featureToggles) {
    this.kolibreeUtils = kolibreeUtils;
    this.networkChecker = networkChecker;
    this.synchronizationScheduler = synchronizationScheduler;
    this.credentialsProvider = credentialsProvider;
    this.endpointProvider = endpointProvider;
    this.userAgentHeaderProvider = userAgentHeaderProvider;
    this.acceptCapabilitiesHeaderProvider = acceptCapabilitiesHeaderProvider;
    this.accessTokenManager = accessTokenManager;
    this.deviceParameters = deviceParameters;
    this.accountDatastore = accountDatastore;
    this.forceAppUpdater = forceAppUpdater;
    this.networkErrorHandler = networkErrorHandler;
    this.networkLogFeatureToggle =
        (NetworkLogFeatureToggle)
            FeatureToggleSetExtKt.toggleForFeature(featureToggles, NetworkLogFeature.INSTANCE);

    if (!credentialsProvider.get().validateCredentials()) {
      throw new IllegalArgumentException(
          "client id and client secret can't be empty " + credentialsProvider.get());
    }
  }

  /**
   * Call webservice
   *
   * @param request a Request
   */
  @NonNull
  Response call(@NonNull final Request request, String accessToken) {
    if (!networkChecker.hasConnectivity()) {
      synchronizationScheduler.syncWhenConnectivityAvailable();

      return new Response(ApiError.generateNetworkError());
    }

    final Response response = doCallAndRefreshTokenIfNeeded(request, accessToken);

    if (!response.succeeded()
        && response.getError().isNetworkError()) { // Just a retry in case of bad connection
      return doCallAndRefreshTokenIfNeeded(request, accessToken);
    } else {
      networkErrorHandler.acceptApiError(response.getError());

      return response;
    }
  }

  /**
   * Performs the call to the backend and handles some error cases.
   *
   * <p>First of all, it asks [NetworkErrorHandler] if it can handle the error. If affirmative, it
   * returns the Response with the error.
   *
   * <p>If the error is {@link ApiErrorCode#ACCESS_TOKEN_HAS_EXPIRED}, it attempts to refresh the
   * token and retries the orignal call. If we can't refresh the access token, we notify an
   * irrecoverable error through AccessTokenManager
   *
   * @param request the Request to perform
   * @param accessToken the accessToken
   * @return Response. Might contain an error
   */
  @NonNull
  @VisibleForTesting
  Response doCallAndRefreshTokenIfNeeded(@NonNull final Request request, String accessToken) {
    final Response originalResponse = doCall(request, accessToken);

    // If the token has to be refreshed
    if (!originalResponse.succeeded()
        && originalResponse.getError().getInternalErrorCode()
            == ApiErrorCode.ACCESS_TOKEN_HAS_EXPIRED) {
      logDebug("Access token has expired, asking for a new one");
      RefreshTokenData data = getRefreshTokenData();
      if (data == null) {
        logDebug("Refresh token data could not be obtained, probably account is missing");
        return originalResponse;
      }

      final RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(data);
      final Response refreshTokenResponse = doCall(refreshTokenRequest, null);

      if (refreshTokenResponse.succeeded()) {
        try { // We parse the returned account to get new tokens
          final RefreshTokenResponse r = new RefreshTokenResponse(refreshTokenResponse.getBody());

          accessTokenManager.updateTokens(r).blockingAwait();

          // Retry the original request
          return doCall(request, r.getAccessToken());
        } catch (JSONException e) { // Should never happen
          logError(e);
          accessTokenManager.notifyUnableToRefreshToken();
          return new Response(ApiError.generateUnknownError(refreshTokenResponse.getHttpCode()));
        }
      } else { // Something went wrong with refresh token, should not happen
        accessTokenManager.notifyUnableToRefreshToken();
        return new Response(ApiError.generateUnknownError(refreshTokenResponse.getHttpCode()));
      }
    }

    return originalResponse;
  }

  @VisibleForTesting
  @NonNull
  Response doCall(@NonNull final Request request, String accessToken) {
    HttpURLConnection httpCon = null;
    try {
      httpCon = createHttpUrlConnection(request, accessToken);

      StringBuilder requestLogMessage = new StringBuilder("Fetching ");
      requestLogMessage.append(httpCon.getURL());

      if (request.hasBody()) {
        final OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        String body = request.getData().toJsonString();

        requestLogMessage.append("\nbody: ");
        requestLogMessage.append(body);

        out.write(body);
        out.flush();
        out.close();
      }

      logDebug(requestLogMessage.toString());

      // JellyBean 4.1 401 code headers issue workaround
      int responseCode;

      try {
        responseCode = httpCon.getResponseCode();
      } catch (IOException e) {
        responseCode = 401;
      }

      String responseBody = null;

      if (responseCode == HTTP_OK) {
        responseBody = IOUtils.toString(httpCon.getInputStream());
      } else if (httpCon.getErrorStream() != null) {
        responseBody = IOUtils.toString(httpCon.getErrorStream());
      }

      logDebug("Server responded with code : " + responseCode + " to " + request.getUrl());
      logDebug("Server response body : %s", responseBody);

      Response response = new Response(httpCon.getResponseCode(), responseBody);
      forceAppUpdater.maybeNotifyForcedAppUpdate(response);

      extraNetworkLogs(httpCon);

      return response;
    } catch (IOException e) {
      Timber.e(e);
      return new Response(ApiError.generateNetworkError());
    } catch (Exception e) {
      Timber.e(e);
      return new Response(ApiError.generateUnknownError());
    } finally {
      if (httpCon != null) { // Properly close connection
        httpCon.disconnect();
      }
    }
  }

  /**
   * Delegate network request logging to Auditor. For example, on release builds we will upload them
   * to Instabug
   *
   * @param httpCon
   */
  private void extraNetworkLogs(@NonNull HttpURLConnection httpCon) {
    try {
      Auditor.Companion.instance().networkLog(httpCon);
    } catch (Exception e) {
      Timber.e(e);

      // ignore instabug exceptions
    }
  }

  @VisibleForTesting
  @NonNull
  HttpURLConnection createHttpUrlConnection(@NonNull final Request request, String accessToken)
      throws IOException, NoSuchAlgorithmException, InvalidKeyException {
    HttpURLConnection httpCon;

    final String url = endpointProvider.get().url() + request.getUrl();
    final URL connect = new URL(url);

    httpCon = (HttpURLConnection) connect.openConnection();
    httpCon.setConnectTimeout((int) NetworkConstants.DEFAULT_HTTP_CONNECTION_TIMEOUT.toMillis());
    httpCon.setReadTimeout((int) NetworkConstants.DEFAULT_HTTP_READ_TIMEOUT.toMillis());

    if (accessToken != null) {
      httpCon.setRequestProperty(API_CLIENT_ACCESS_TOKEN, accessToken);
    }

    httpCon.setRequestMethod(request.getMethod().name());

    Pair<String, String> userAgentHeader = userAgentHeaderProvider.getUserAgent();
    httpCon.setRequestProperty(userAgentHeader.getFirst(), userAgentHeader.getSecond());
    Pair<String, String> acceptCapabilitiesHeader =
        acceptCapabilitiesHeaderProvider.getCapabilities();
    httpCon.setRequestProperty(
        acceptCapabilitiesHeader.getFirst(), acceptCapabilitiesHeader.getSecond());

    httpCon.setRequestProperty(API_CLIENT_ID_HEADER, credentialsProvider.get().clientId());
    httpCon.setRequestProperty(
        API_CLIENT_SIG_HEADER,
        kolibreeUtils.encrypt(
            NetworkExtensionsKt.prepareUrlForSignatureCalculation(url),
            credentialsProvider.get().clientSecret()));
    httpCon.setRequestProperty(API_DEVICE_ID, kolibreeUtils.getDeviceId());
    httpCon.setRequestProperty(API_DEVICE_PARAMETERS, deviceParameters.encrypt());
    httpCon.setRequestProperty(FIELD_CONTENT_TYPE, API_DATA_TYPE);
    httpCon.setRequestProperty(ACCEPT_LANGUAGE, getAcceptedLanguageHeader());

    return httpCon;
  }

  @Nullable
  private RefreshTokenData getRefreshTokenData() {
    AccountInternal accountInternal = currentAccount();
    if (accountInternal == null) {
      return null;
    }

    return new RefreshTokenData(
        accountInternal.getRefreshToken(), accountInternal.getAccessToken());
  }

  @Nullable
  private AccountInternal currentAccount() {
    return accountDatastore.getAccountMaybe().subscribeOn(Schedulers.io()).blockingGet();
  }

  private static final String TAG = "\uD83D\uDCE1|Network"; // 📡|Network

  private void logDebug(String message) {
    if (networkLogFeatureToggle.getValue()) {
      Timber.tag(TAG).d(message);
    }
  }

  private void logDebug(@NonNls String message, Object... args) {
    if (networkLogFeatureToggle.getValue()) {
      Timber.tag(TAG).d(message, args);
    }
  }

  private void logError(Throwable e) {
    if (networkLogFeatureToggle.getValue()) {
      Timber.tag(TAG).e(e);
    }
  }
}
