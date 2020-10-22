package cn.colgate.colgateconnect.model;

import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.commons.profile.Gender;
import com.kolibree.android.commons.profile.Handedness;
import com.kolibree.android.commons.profile.SourceApplication;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.threeten.bp.LocalDate;

/**
 * Your custom profile that will implement our IProfile from the SDK you can add as many attributes
 * as you wish until there are the ones needed by the IProfile interface.
 */
public class DemoProfile implements IProfile {

  private final Long id;
  private final String firstName;
  private final Gender gender;
  private final Handedness handedness;
  private final int brushingGoalTime;
  private final String createdDate;
  private final String pictureUrl;
  private final String pictureLastModifier;
  private LocalDate birthday;

  /**
   * Basic constructor, but you can have as many attributes as you need, those ones are only the one
   * needed by the SDK, that you will need to provide
   */
  public DemoProfile(
      Long id,
      String firstName,
      Gender gender,
      Handedness handedness,
      int brushingGoalTime,
      String createdDate,
      LocalDate birthday,
      String pictureUrl,
      String pictureLastModifier) {
    this.id = id;
    this.firstName = firstName;
    this.gender = gender;
    this.handedness = handedness;
    this.brushingGoalTime = brushingGoalTime;
    this.createdDate = createdDate;
    this.birthday = birthday;
    this.pictureUrl = pictureUrl;
    this.pictureLastModifier = pictureLastModifier;
  }

  public static DemoProfile fromIProfile(IProfile profile) {
    return new DemoProfile(
        profile.getId(),
        profile.getFirstName(),
        profile.getGender(),
        profile.getHandedness(),
        profile.getBrushingGoalTime(),
        profile.getCreatedDate(),
        profile.getBirthday(),
        profile.getPictureUrl(),
        profile.getPictureLastModifier());
  }

  @Nullable
  @Override
  public LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }

  @Override
  public int getBrushingGoalTime() {
    return brushingGoalTime;
  }

  @NotNull
  @Override
  public String getCreatedDate() {
    return createdDate;
  }

  @NotNull
  @Override
  public String getFirstName() {
    return firstName;
  }

  @NotNull
  @Override
  public Gender getGender() {
    return gender;
  }

  @NotNull
  @Override
  public Handedness getHandedness() {
    return handedness;
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public boolean isMale() {
    return gender == Gender.MALE;
  }

  @Override
  public boolean isRightHanded() {
    return handedness == Handedness.RIGHT_HANDED;
  }

  @Nullable
  @Override
  public String getPictureUrl() {
    return pictureUrl;
  }

  @Nullable
  @Override
  public String getPictureLastModifier() {
    return pictureLastModifier;
  }

  @Nullable
  @Override
  public String getCountry() {
    return "FR";
  }

  @NotNull
  @Override
  public String toString() {
    return "DemoProfile{"
        + "id="
        + id
        + ", firstName='"
        + firstName
        + '\''
        + ", gender="
        + gender
        + ", handedness="
        + handedness
        + ", brushingGoalTime="
        + brushingGoalTime
        + ", createdDate='"
        + createdDate
        + '\''
        + ", birthday="
        + birthday
        + ", pictureUrl='"
        + pictureUrl
        + ", pictureLastModifier='"
        + pictureLastModifier
        + '\''
        + '}';
  }

  @Nullable
  @Override
  public SourceApplication getSourceApplication() {
    return null;
  }
}
