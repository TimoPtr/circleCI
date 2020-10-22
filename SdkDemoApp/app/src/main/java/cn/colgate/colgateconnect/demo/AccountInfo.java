package cn.colgate.colgateconnect.demo;

import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.pairing.session.PairingSession;
import java.util.ArrayList;
import java.util.List;

/** Simple class that will store the data associated to the logged user for the session */
public class AccountInfo {

  private IProfile currentProfile; // logged currentProfile
  private PairingSession pairingSession = null; // connected TB
  private List<IProfile> profiles = new ArrayList<>();

  public AccountInfo() {
    currentProfile = null;
  }

  public List<IProfile> getProfiles() {
    return profiles;
  }

  public void setProfiles(List<IProfile> profiles) {
    this.profiles.clear();
    this.profiles.addAll(profiles);
  }

  public IProfile getCurrentProfile() {
    return currentProfile;
  }

  public void setCurrentProfile(IProfile currentProfile) {
    this.currentProfile = currentProfile;
  }

  public PairingSession getPairingSession() {
    return pairingSession;
  }

  public void setPairingSession(PairingSession pairingSession) {
    this.pairingSession = pairingSession;
  }
}
