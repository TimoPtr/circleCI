package com.kolibree.android.app.ui.ota;

import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.toothbrushupdate.CheckOtaUpdatePrerequisitesModule;
import dagger.Module;
import dagger.Provides;

/** {@link OtaUpdateActivity} module */
@Module(includes = {CheckOtaUpdatePrerequisitesModule.class})
public abstract class OtaUpdateActivityModule {

  @Provides
  static boolean providesIsMandatoryUpdate(OtaUpdateActivity otaUpdateActivity) {
    return otaUpdateActivity.isMandatoryUpdate();
  }

  @Provides
  static String providesToothbrushMac(OtaUpdateActivity otaUpdateActivity) {
    return otaUpdateActivity.toothbrushMac();
  }

  @Provides
  static ToothbrushModel providesToothbrushModel(OtaUpdateActivity otaUpdateActivity) {
    return otaUpdateActivity.toothbrushModel();
  }
}
