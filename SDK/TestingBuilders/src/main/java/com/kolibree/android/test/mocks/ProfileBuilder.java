package com.kolibree.android.test.mocks;

import static com.kolibree.android.accountinternal.profile.models.Profile.DEFAULT_AGE;
import static com.kolibree.android.commons.ApiConstants.DATETIME_PATTERN;

import androidx.annotation.NonNull;
import com.kolibree.android.accountinternal.profile.models.Profile;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.commons.profile.Gender;
import com.kolibree.android.commons.profile.Handedness;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

/** Created by miguelaragues on 17/11/17. */
@SuppressWarnings("KotlinInternalInJava")
public class ProfileBuilder {

  public static final String DEFAULT_NAME = "Dummy";
  public static final long DEFAULT_ID = 34L;
  public static final Gender DEFAULT_GENDER = Gender.MALE;
  public static final Handedness DEFAULT_HANDEDNESS = Handedness.LEFT_HANDED;
  public static final LocalDate DEFAULT_BIRTHDAY = LocalDate.now().minusYears(20);

  private static final int DEFAULT_BRUSHING_TIME = 120;
  static final DateTimeFormatter CREATE_DATE_FORMATTER =
      DateTimeFormatter.ofPattern(DATETIME_PATTERN);
  private Integer targetBrushingTime;
  private Long id;

  private String name;
  private Handedness surveyHandednesss;
  private ZonedDateTime creationDate;
  private Integer age;
  private String pictureUrl;
  private String pictureLastModifier;
  private String country;
  private Gender gender;
  private int points;
  private boolean exactBirthday;
  private LocalDate birthday;
  private boolean isDeletable = false;

  private ProfileBuilder() {}

  public static ProfileBuilder create() {
    return new ProfileBuilder().withDefaultState();
  }

  private ProfileBuilder withDefaultState() {
    return withId(DEFAULT_ID)
        .withTargetBrushingTime(DEFAULT_BRUSHING_TIME)
        .withName(DEFAULT_NAME)
        .withPoints()
        .withHandednessLeft()
        .withMaleGender()
        .withAge(DEFAULT_AGE)
        .withCreationDate(TrustedClock.getNowZonedDateTime());
  }

  private ProfileBuilder withSurveyHandednesss(Handedness surveyHandednesss) {
    this.surveyHandednesss = surveyHandednesss;

    return this;
  }

  public ProfileBuilder withHandednessRight() {
    return withSurveyHandednesss(Handedness.RIGHT_HANDED);
  }

  public ProfileBuilder withHandednessLeft() {
    return withSurveyHandednesss(Handedness.LEFT_HANDED);
  }

  public ProfileBuilder withGender(@NonNull Gender gender) {
    this.gender = gender;
    return this;
  }

  public ProfileBuilder withMaleGender() {
    return withGender(Gender.MALE);
  }

  public ProfileBuilder withFemaleGender() {
    return withGender(Gender.FEMALE);
  }

  public ProfileBuilder withUnknownGender() {
    return withGender(Gender.UNKNOWN);
  }

  @NonNull
  public ProfileBuilder withCreationDate(@NonNull ZonedDateTime creationDate) {
    this.creationDate = creationDate;

    return this;
  }

  public ProfileBuilder withTargetBrushingTime(int targetBrushingTime) {
    this.targetBrushingTime = targetBrushingTime;

    return this;
  }

  public ProfileBuilder withId(long id) {
    this.id = id;

    return this;
  }

  public ProfileBuilder withPoints() {
    this.points = 50;

    return this;
  }

  public ProfileBuilder withPoints(int points) {
    this.points = points;

    return this;
  }

  public ProfileBuilder withName(String name) {
    this.name = name;

    return this;
  }

  public ProfileBuilder withAge(int age) {
    this.age = age;

    return this;
  }

  public ProfileBuilder withExactBirthday(boolean exactBirthday) {
    this.exactBirthday = exactBirthday;

    return this;
  }

  public ProfileBuilder withBirthday(LocalDate birthday) {
    this.birthday = birthday;

    return this;
  }

  public ProfileBuilder withPictureUrl(String pictureUrl) {
    this.pictureUrl = pictureUrl;

    return this;
  }

  public ProfileBuilder withPictureLastModifier(String pictureLastModifier) {
    this.pictureLastModifier = pictureLastModifier;

    return this;
  }

  public ProfileBuilder withCountry(String country) {
    this.country = country;

    return this;
  }

  public ProfileBuilder withDeletable(boolean isDeletable) {
    this.isDeletable = isDeletable;

    return this;
  }

  /*
  Stats stats,
  boolean hasExactBirthdate,
  LocalDate birthday) {*/

  public Profile build() {
    return new Profile(
        pictureUrl,
        pictureLastModifier,
        name,
        country,
        gender,
        surveyHandednesss,
        targetBrushingTime,
        id,
        birthday,
        creationDate.format(CREATE_DATE_FORMATTER),
        null,
        null,
        false,
        isDeletable,
        points,
        exactBirthday,
        age == null ? 0 : age,
        0);
  }
}
