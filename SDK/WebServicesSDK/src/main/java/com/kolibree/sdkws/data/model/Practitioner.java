package com.kolibree.sdkws.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Keep;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by aurelien on 23/12/15.
 *
 * <p>Dental practitioner model
 */
@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
public class Practitioner implements Parcelable {
  public static final Parcelable.Creator<Practitioner> CREATOR =
      new Parcelable.Creator<Practitioner>() {
        public Practitioner createFromParcel(Parcel in) {
          return new Practitioner(in);
        }

        public Practitioner[] newArray(int size) {
          return new Practitioner[size];
        }
      };

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty("last_name")
  private String lastName;

  @JsonProperty("speciality")
  private String speciality;

  @JsonProperty("job")
  private String job;

  @JsonProperty("gender")
  private String gender;

  @JsonProperty("is_office_admin")
  private boolean officeAdmin;

  @JsonProperty("profile_image")
  private String profileImage;

  @JsonProperty("id")
  private long id;

  @JsonProperty("name_title")
  private String nameTitle;

  @JsonProperty("email")
  private String email;

  @JsonProperty("share")
  private boolean share;

  @JsonProperty("token")
  private String token;

  @SuppressWarnings("unused")
  public Practitioner() {}

  private Practitioner(Parcel in) {
    firstName = in.readString();
    lastName = in.readString();
    speciality = in.readString();
    job = in.readString();
    gender = in.readString();
    officeAdmin = in.readInt() == 1;
    profileImage = in.readString();
    id = in.readLong();
    nameTitle = in.readString();
    email = in.readString();
    share = in.readInt() == 1;
    token = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeString(firstName);
    out.writeString(lastName);
    out.writeString(speciality);
    out.writeString(job);
    out.writeString(gender);
    out.writeInt(officeAdmin ? 1 : 0);
    out.writeString(profileImage);
    out.writeLong(id);
    out.writeString(nameTitle);
    out.writeString(email);
    out.writeInt(share ? 1 : 0);
    out.writeString(token);
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getSpeciality() {
    return speciality;
  }

  public String getJob() {
    return job;
  }

  public String getGender() {
    return gender;
  }

  public boolean isOfficeAdmin() {
    return officeAdmin;
  }

  public String getProfileImage() {
    return profileImage;
  }

  public long getId() {
    return id;
  }

  public String getNameTitle() {
    return nameTitle;
  }

  public String getEmail() {
    return email;
  }

  public boolean isShared() {
    return share;
  }

  public void setShared(boolean shared) {
    this.share = shared;
  }

  public String getToken() {
    return token;
  }
}
