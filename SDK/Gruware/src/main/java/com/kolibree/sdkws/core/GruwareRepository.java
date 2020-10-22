package com.kolibree.sdkws.core;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kolibree.sdkws.data.model.GruwareData;
import io.reactivex.Single;

/** Created by guillaumeagis on 28/05/18. */
@Keep
public interface GruwareRepository {

  /**
   * @return Single that will emit {@link GruwareData} for the specified parameters. It might be
   *     cached or it might imply a remote request
   *     <p>It will emit a {@link java.io.IOException} if there were errors checking or downloading
   *     the files
   */
  @NonNull
  Single<GruwareData> getGruwareInfo(
      @NonNull String toothbrushModel,
      @NonNull String hardwareVersion,
      @Nullable String serial,
      @NonNull String firmwareVersion);
}
