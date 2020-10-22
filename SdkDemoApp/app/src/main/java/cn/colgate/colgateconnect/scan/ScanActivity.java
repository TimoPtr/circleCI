package cn.colgate.colgateconnect.scan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.base.SdkDemoBaseActivity;
import cn.colgate.colgateconnect.demo.AccountInfo;
import com.kolibree.android.sdk.scan.ToothbrushScanResult;
import com.kolibree.pairing.assistant.PairingAssistant;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Scan for nearby toothbrushes using the pairing module Will display the list of discovered
 * toothbrushes in the area and display some data about the TH when clicking on an item from the
 * list.
 */
public class ScanActivity extends SdkDemoBaseActivity implements ScanView {

  private static final int TIMEOUT = 15000;

  // pairing module
  @Inject PairingAssistant pairingAssistant;

  @Inject AccountInfo accountInfo;

  // required to stop the scan
  private Disposable pairingAssistantDisposable;
  private List<ToothbrushScanResult> availableToothbrushes = new ArrayList<>();
  private CompositeDisposable disposables = new CompositeDisposable();
  private RecyclerView availableList;
  private TextView action;
  private CountDownTimer timer =
      new CountDownTimer(TIMEOUT, 1000L) {
        @Override
        public void onTick(long l) {
          long tmp = l / 1000;
          String text = getString(R.string.scanning) + " " + tmp;
          action.setText(text);
        }

        @Override
        public void onFinish() {
          if (availableToothbrushes.isEmpty()) {
            Toast.makeText(ScanActivity.this, R.string.no_tb_detected, Toast.LENGTH_LONG).show();
          }
          stopScan();
        }
      };

  public static void start(Context context) {
    Intent starter = new Intent(context, ScanActivity.class);
    context.startActivity(starter);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);

    initView();
    displayBackButtonToolbar();
    turnOnBluetooth();
  }

  /**
   * Starting scanning for nearby toothbrushes It keeps searching until the observers
   * (pairingAssistantDisposable in this case) are not disposed It will scan for 15sec
   */
  private void startScan() {
    if (locationStatus.isReadyToScan()) {
      try {
        pairingAssistantDisposable =
            pairingAssistant
                .scannerObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onToothbrushFound, Throwable::printStackTrace);
        disposables.add(pairingAssistantDisposable);
        action.setText(R.string.scanning);

        timer.start();
      } catch (IllegalStateException ise) {
        ise.printStackTrace();
      }
    } else {
      showLocationNeededDialog();
    }
  }

  /** disposes the pairing assistant, that was scanning, so it stops the scan */
  private void stopScan() {
    action.setText(R.string.scan);
    timer.cancel();
    if (pairingAssistantDisposable != null && !pairingAssistantDisposable.isDisposed()) {
      pairingAssistantDisposable.dispose();
    }
  }

  /**
   * Add discovered toothbrush in the list if it does not exist yet
   *
   * @param result discovered toothbrush
   */
  private void onToothbrushFound(@NonNull ToothbrushScanResult result) {
    if (shouldAddScanResult(result)) {
      availableToothbrushes.add(result);
      availableList.setAdapter(new ToothbrushAdapter(this, availableToothbrushes));
    }
  }

  private boolean shouldAddScanResult(@NonNull ToothbrushScanResult scanResult) {
    return !availableToothbrushes.contains(scanResult);
  }

  /**
   * Pair to the toothbrush and display the settings of it
   *
   * @param result toothbrush we wish to see the detail from
   */
  @Override
  public void onItemClick(ToothbrushScanResult result) {

    disposables.add(
        pairingAssistant
            .pair(result)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(__ -> showProgress())
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                pairingSession -> {
                  hideProgress();
                  accountInfo.setPairingSession(pairingSession);
                  finish();
                  //  ToothbrushActivity.start(this);
                },
                this::displayErrorOnScreen));
  }

  private void displayErrorOnScreen(Throwable throwable) {
    Toast.makeText(this, R.string.pairing_impossible, Toast.LENGTH_LONG).show();
    throwable.printStackTrace();
    hideProgress();
  }

  private void initView() {
    action = findViewById(R.id.action);

    availableList = findViewById(R.id.available_list);
    availableList.setHasFixedSize(true);
    availableList.setLayoutManager(new LinearLayoutManager(this));

    action.setOnClickListener(v -> startScan());
    action.setText(R.string.scan);
    availableList.setAdapter(new ToothbrushAdapter(this, availableToothbrushes));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    stopScan();
    if (timer != null) {
      timer.cancel();
    }
  }
}
