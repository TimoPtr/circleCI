package cn.colgate.colgateconnect.update;

import com.kolibree.android.sdk.connection.toothbrush.Toothbrush;
import com.kolibree.android.toothbrushupdate.OtaChecker;
import com.kolibree.android.toothbrushupdate.OtaForConnection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Encapsulate Ota logics, should be done in a ViewModel if you are familiar with it. Verify if
 * there is a new update available or not onto the Toothbrush.
 */
public final class OtaCheckerManager {

  private final OtaChecker otaChecker;
  private final OtaView view;
  private CompositeDisposable disposables = new CompositeDisposable();

  public OtaCheckerManager(OtaChecker otaChecker, OtaView view) {
    this.otaChecker = otaChecker;
    this.view = view;
  }

  /** Verify if a new update is available or not */
  public void onStart() {
    disposables.add(
        otaChecker
            .otaForConnectionsOnce()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onOtaForConnection, Throwable::printStackTrace));
  }

  public void onStop() {
    disposables.clear();
  }

  private void onOtaForConnection(OtaForConnection otaUpdateForConnection) {
    switch (otaUpdateForConnection.getOtaUpdateType()) {
      case STANDARD:
        // This KLTBConnection now will emit true for hasOTAObservable()
        hasOTAObservable(otaUpdateForConnection);
        break;
      case MANDATORY:
        Toothbrush toothbrush = otaUpdateForConnection.getConnection().toothbrush();
        view.onMandatoryUpdateNeeded(toothbrush.getMac(), toothbrush.getModel());
        break;
      case MANDATORY_NEEDS_INTERNET:
        view.requestEnableInternet();
      default:
        // do nothing
    }
  }

  private void hasOTAObservable(OtaForConnection otaUpdateForConnection) {
    disposables.add(
        otaUpdateForConnection
            .getConnection()
            .hasOTAObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                hasOta -> {
                  if (hasOta) {
                    Toothbrush toothbrush = otaUpdateForConnection.getConnection().toothbrush();
                    view.updateAvailable(toothbrush.getMac(), toothbrush.getModel());
                  }
                },
                Throwable::printStackTrace));
  }
}
