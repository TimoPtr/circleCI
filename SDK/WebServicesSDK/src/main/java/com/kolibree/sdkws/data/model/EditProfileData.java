package com.kolibree.sdkws.data.model;

import static com.kolibree.android.accountinternal.AccountUtilsKt.getAgeFromBirthDate;
import static com.kolibree.android.commons.ApiConstants.DATE_FORMATTER;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Ignore;
import com.google.gson.annotations.SerializedName;
import com.kolibree.android.commons.profile.Gender;
import com.kolibree.sdkws.profile.ProfileSyncableFields;
import org.threeten.bp.LocalDate;

/** Created by aurelien on 10/10/15. */
@Keep
public final class EditProfileData implements ProfileSyncableFields {

  public static final String VALUE_RIGHT_HANDED = "R"; // TODO refaire ca avec des setteurs booleans
  public static final String VALUE_LEFT_HANDED = "L";

  public static final int UNSET = -1;

  public static final String FIELD_FIRST_NAME = "first_name";
  public static final String FIELD_GENDER = "gender";
  public static final String FIELD_AGE = "age";
  public static final String FIELD_BRUSHING_GOAL_TIME = "brushing_goal_time";
  public static final String FIELD_BRUSHING_NUMBER = "brushing_number";
  public static final String FIELD_SURVEY_HANDEDNESS = "survey_handedness";
  public static final String FIELD_COUNTRY = "address_country";

  // SyncableField fields
  @SerializedName(FIELD_FIRST_NAME)
  private String firstName;

  @SerializedName(FIELD_GENDER)
  private String gender;

  @SerializedName(FIELD_SURVEY_HANDEDNESS)
  private String handedness;

  @SerializedName(FIELD_COUNTRY)
  private String countryCode;

  @SerializedName(FIELD_BRUSHING_GOAL_TIME)
  private int brushingTime;

  @SerializedName(FIELD_BRUSHING_NUMBER)
  private int brushingNumber;

  @SerializedName(FIELD_AGE)
  private int age;

  @SerializedName("birthday")
  private String birthday;

  @Ignore private String picturePath;

  public EditProfileData() {
    brushingNumber = UNSET;
    brushingTime = UNSET;
    age = UNSET;
  }

  public void setBirthday(@NonNull LocalDate birthday) {
    this.birthday = DATE_FORMATTER.format(birthday);
    age = getAgeFromBirthDate(birthday);
  }

  /** Getter for offline update internal */
  @Nullable
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getGender() {
    return gender;
  }

  @Deprecated // use setGender(Gender) instead
  public void setGender(String gender) {
    this.gender = gender;
  }

  public void setGender(@NonNull Gender gender) {
    this.gender = gender.getSerializedName();
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public String getHandedness() {
    return handedness;
  }

  public String getBirthday() {
    return birthday;
  }

  public void setHandedness(String handedness) {
    this.handedness = handedness;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public int getBrushingTime() {
    return brushingTime;
  }

  public void setBrushingTime(int brushingTime) {
    this.brushingTime = brushingTime;
  }

  public String getPicturePath() {
    return picturePath;
  }

  public void setPicturePath(String picturePath) {
    this.picturePath = picturePath;
  }

  @Override
  public int getBrushingNumber() {
    return brushingNumber;
  }

  public void setBrushingNumber(int brushingNumber) {
    this.brushingNumber = brushingNumber;
  }

  public boolean isTypePicture() {
    return picturePath != null;
  }

  public boolean isTypeFields() {
    return firstName != null
        || gender != null
        || age != UNSET
        || brushingTime != UNSET
        || brushingNumber != UNSET
        || handedness != null
        || countryCode != null;
  }

  public boolean hasUpdate() {
    return isTypePicture() || isTypeFields();
  }
}
