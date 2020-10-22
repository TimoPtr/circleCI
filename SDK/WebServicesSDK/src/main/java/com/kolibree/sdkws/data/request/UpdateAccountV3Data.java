package com.kolibree.sdkws.data.request;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

/** Immutable class that contains the data to update an Account */
public final class UpdateAccountV3Data {

  /*
  If we add fields, consider moving to a Builder pattern to avoid a public constructor with too many
  parameters
   */
  @SerializedName("parental_email")
  final String parentalEmail;

  @SerializedName("parental_consent")
  final boolean parentalConsent;

  public UpdateAccountV3Data(boolean parentalConsent, String parentalEmail) {
    this.parentalEmail = parentalEmail;
    this.parentalConsent = parentalConsent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateAccountV3Data that = (UpdateAccountV3Data) o;
    return parentalConsent == that.parentalConsent
        && Objects.equals(parentalEmail, that.parentalEmail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parentalEmail, parentalConsent);
  }
}
