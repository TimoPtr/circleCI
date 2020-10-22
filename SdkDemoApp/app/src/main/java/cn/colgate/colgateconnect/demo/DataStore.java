package cn.colgate.colgateconnect.demo;

import android.content.SharedPreferences;
import cn.colgate.colgateconnect.model.DemoProfile;
import com.google.gson.Gson;
import com.kolibree.android.accountinternal.profile.models.IProfile;

/** Store locally the profile of the user to connect him again at the next session */
public final class DataStore {

  private static final String PROFILE = "profile";
  private final SharedPreferences sharedPreferences;
  private final Gson gson = new Gson();

  public DataStore(SharedPreferences sharedPreferences) {
    this.sharedPreferences = sharedPreferences;
  }

  /**
   * Store current profile
   *
   * @param profile profile to store
   */
  public void storeProfile(IProfile profile) {
    sharedPreferences
        .edit()
        .putString(PROFILE, gson.toJson(DemoProfile.fromIProfile(profile), DemoProfile.class))
        .apply();
  }

  public void clean() {
    sharedPreferences.edit().clear().apply();
  }

  /**
   * Read shared pref to check if there is a profile stored
   *
   * @return profile stored
   */
  public IProfile getStoredProfile() {
    String profileJson = sharedPreferences.getString(PROFILE, null);
    if (profileJson == null) {
      return null;
    }
    return gson.fromJson(profileJson, DemoProfile.class);
  }
}
