package com.kolibree.sdkws.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** Unused for now, we must still work on Retrofit automatically parsing our data */
public class AccountV3 {

  @SerializedName("auth_type")
  @Expose
  private String authType;

  @SerializedName("owner_profile_id")
  @Expose
  private long ownerProfileId;

  public String getAuthType() {
    return authType;
  }

  public long getOwnerProfileId() {
    return ownerProfileId;
  }
}
