package cn.colgate.colgateconnect.demo;

import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.pairing.session.PairingSession;

/** Simple class that will store the data associated to the logged user for the session */
public class ProfileInfo {

  private IProfile profile; // logged profile
  private PairingSession pairingSession = null; // connected TB

  public ProfileInfo() {
    profile = null;
  }

  public IProfile getProfile() {
    return profile;
  }

  public void setProfile(IProfile profile) {
    this.profile = profile;
  }

  public PairingSession getPairingSession() {
    return pairingSession;
  }

  public void setPairingSession(PairingSession pairingSession) {
    this.pairingSession = pairingSession;
  }
}
