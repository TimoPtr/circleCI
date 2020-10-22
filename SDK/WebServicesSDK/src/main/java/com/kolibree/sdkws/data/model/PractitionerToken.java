package com.kolibree.sdkws.data.model;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by aurelien on 22/05/17.
 *
 * <p>Dental practitioner token model
 */
@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PractitionerToken {

  @JsonProperty("profile")
  private long profile;

  @JsonProperty("practitioner")
  private Practitioner practitioner;

  @JsonProperty("token")
  private String token;

  @JsonProperty("type")
  private String type;

  @JsonProperty("name")
  private String studyName;

  public long getProfile() {
    return profile;
  }

  @Nullable // this can be null if type="novibration"
  public Practitioner getPractitioner() {
    return practitioner;
  }

  public String getToken() {
    return token;
  }

  public String getType() {
    return type;
  }

  public String getStudyName() {
    return studyName;
  }
}
