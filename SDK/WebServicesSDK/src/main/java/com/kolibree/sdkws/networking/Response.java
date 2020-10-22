package com.kolibree.sdkws.networking;

import com.kolibree.android.network.api.ApiError;
import org.json.JSONException;

/** Created by aurelien on 17/09/15. */
public final class Response {
  private int httpCode;
  private String body;
  private ApiError error;

  public Response(int httpCode, String content) {
    if (httpCode == 200) {
      this.httpCode = 200;
      this.body = content;
      this.error = null;
    } else {
      this.body = null;

      try {
        if (content != null) {
          this.error = new ApiError(content);
        } else {
          this.error = ApiError.generateUnknownError(httpCode);
        }
        this.httpCode = httpCode;
      } catch (JSONException e) { // Should never happen (server response corrupted)
        this.error = ApiError.generateUnknownError(httpCode);
        this.httpCode = httpCode;
      }
    }
  }

  public Response(ApiError error) {
    this.httpCode = -1;
    this.body = null;
    this.error = error;
  }

  public int getHttpCode() {
    return httpCode;
  }

  public String getBody() {
    return body;
  }

  public boolean succeeded() {
    return httpCode == 200;
  }

  public ApiError getError() {
    return error;
  }
}
