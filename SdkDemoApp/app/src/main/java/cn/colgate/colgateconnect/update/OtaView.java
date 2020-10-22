package cn.colgate.colgateconnect.update;

import com.kolibree.android.commons.ToothbrushModel;

public interface OtaView {

  void onMandatoryUpdateNeeded(String mac, ToothbrushModel model);

  void requestEnableInternet();

  void updateAvailable(String mac, ToothbrushModel model);
}
