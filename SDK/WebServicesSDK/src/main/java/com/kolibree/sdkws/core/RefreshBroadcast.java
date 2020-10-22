package com.kolibree.sdkws.core;

import androidx.annotation.NonNull;
import com.jakewharton.rxrelay2.PublishRelay;
import io.reactivex.Observable;

public final class RefreshBroadcast {

  /**
   * Profile changes and synchronization relay
   *
   * <p>Emits each time profile / brushing related screen should refresh its content
   */
  private final PublishRelay<Long> refreshRelay = PublishRelay.create();
  /** Public version of the above relay */
  private final Observable<Long> refreshObservable = refreshRelay.hide();

  public RefreshBroadcast() {}

  public void sendRefreshBroadcast(@NonNull Long profileId) {
    refreshRelay.accept(profileId);
  }

  @NonNull
  public Observable<Long> getRefreshObservable() {
    return refreshObservable;
  }
}
