package com.kolibree.sdkws.api.request;

import androidx.annotation.NonNull;
import com.kolibree.sdkws.data.JSONModel;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 15/09/15. */
public class Request {
  private RequestMethod method;
  private String service;
  private JSONModel data;

  protected Request(RequestMethod method, String service) {
    this.method = method;
    this.service = service;
  }

  /**
   * Get request call url
   *
   * @return String
   */
  public final @NonNull String getUrl() {
    return service;
  }

  public final JSONModel getData() {
    return data;
  }

  /**
   * Set request inner data
   *
   * @param data JSONModel data
   */
  public final void setData(JSONModel data) {
    this.data = data;
  }

  /**
   * Check request inner data
   *
   * @return true if this request does contain inner data, false otherwise
   */
  public final boolean hasBody() {
    return data != null;
  }

  public final RequestMethod getMethod() {
    return method;
  }
}
