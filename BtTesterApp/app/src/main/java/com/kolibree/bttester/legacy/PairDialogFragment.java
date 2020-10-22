/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.legacy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.kolibree.android.sdk.scan.ToothbrushScanResult;
import com.kolibree.android.sdk.util.IBluetoothUtils;
import com.kolibree.bttester.R;
import com.kolibree.bttester.common.BaseDaggerDialogFragment;
import com.kolibree.pairing.assistant.PairingAssistant;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import timber.log.Timber;

/** Created by miguelaragues on 23/11/17. */
public class PairDialogFragment extends BaseDaggerDialogFragment {

  public static final String TAG = PairDialogFragment.class.getSimpleName();
  private Button scanButton;
  private OnDeviceSelectedListener listener;

  public static void showIfNotPresent(FragmentManager fragmentManager) {
    if (fragmentManager.findFragmentByTag(TAG) == null) {
      PairDialogFragment.newInstance().show(fragmentManager, PairDialogFragment.TAG);
    }
  }

  private static PairDialogFragment newInstance() {
    Bundle args = new Bundle();

    PairDialogFragment fragment = new PairDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  private static final int REQUEST_PERMISSION_REQ_CODE = 10;

  @Inject PairingAssistant pairingAssistant;
  @Inject IBluetoothUtils bluetoothUtils;

  private DeviceListAdapter adapter;
  private static final long SCAN_DURATION = 15;

  private final CompositeDisposable disposables = new CompositeDisposable();
  private Disposable scanDisposable = null;

  @Override
  public void onAttach(final Context context) {
    super.onAttach(context);
    try {
      this.listener = (OnDeviceSelectedListener) context;
    } catch (final ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement OnDeviceSelectedListener");
    }
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Select device");

    View dialogView =
        LayoutInflater.from(getActivity()).inflate(R.layout.fragment_pair_device, null);
    setupListView(dialogView);

    final AlertDialog dialog = builder.setView(dialogView).create();

    dialog.setCanceledOnTouchOutside(false);

    scanButton = dialogView.findViewById(R.id.action_cancel);
    scanButton.setOnClickListener(
        v -> {
          if (scanDisposable != null) {
            stopScan();
          } else {
            startScan();
          }
        });

    if (savedInstanceState == null) {
      startScan();
    }

    return dialog;
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onPause() {
    stopScan();

    super.onPause();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    disposables.dispose();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);

    listener.onDialogCanceled();
  }

  @Override
  public void onRequestPermissionsResult(
      final int requestCode,
      final @NonNull String[] permissions,
      final @NonNull int[] grantResults) {
    switch (requestCode) {
      case REQUEST_PERMISSION_REQ_CODE:
        {
          if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // We have been granted the Manifest.permission.ACCESS_COARSE_LOCATION permission. Now
            // we may proceed with scanning.
            startScan();
          } else {
            Toast.makeText(
                    getActivity(),
                    "This doesn't work without location permission",
                    Toast.LENGTH_LONG)
                .show();

            getActivity().finish();
          }
          break;
        }
    }
  }

  private void setupListView(View dialogView) {
    ListView listview = dialogView.findViewById(R.id.scanner_list);
    listview.setEmptyView(dialogView.findViewById(R.id.scanner_empty));
    adapter = new DeviceListAdapter(getContext());
    listview.setAdapter(adapter);

    listview.setOnItemClickListener(onDeviceSelected);
  }

  private final OnItemClickListener onDeviceSelected =
      new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          ToothbrushScanResult result = adapter.getItem(position);
          listener.onDeviceSelected(result);

          dismiss();
        }
      };

  private void startScan() {
    if (!bluetoothUtils.isBluetoothEnabled()) {
      Toast.makeText(getContext(), "Enable bluetooth", Toast.LENGTH_LONG).show();

      dismiss();
      return;
    }

    if (!bluetoothUtils.deviceSupportsBle()) {
      Toast.makeText(
              getContext(), "This device does not support Bluetooth Low Energy", Toast.LENGTH_LONG)
          .show();

      dismiss();
      return;
    }

    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {

      requestPermissions(
          new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
      return;
    }

    scanDisposable =
        pairingAssistant
            .scannerObservable()
            .subscribeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(
                ignore -> {
                  adapter.clearDevices();

                  scanButton.setText(R.string.scanner_action_cancel);
                })
            .doOnDispose(() -> scanButton.setText(R.string.scanner_action_scan))
            .takeUntil(Observable.timer(SCAN_DURATION, TimeUnit.SECONDS))
            .doOnSubscribe(ignore -> Timber.d("Subscribed to scan"))
            .doFinally(
                () -> {
                  stopScan();

                  scanDisposable = null;
                })
            .subscribe(
                result -> adapter.addResult(result),
                error -> Toast.makeText(getContext(), "Scan error", Toast.LENGTH_SHORT).show());
    disposables.add(scanDisposable);
  }

  /** Stop scan if user tap Cancel button */
  private void stopScan() {
    if (scanDisposable != null && !scanDisposable.isDisposed()) {
      scanDisposable.dispose();

      scanButton.setText(R.string.scanner_action_scan);
    }
  }

  static class DeviceListAdapter extends ArrayAdapter<ToothbrushScanResult> {

    private final ArrayList<ToothbrushScanResult> availableDevices = new ArrayList<>();

    DeviceListAdapter(@NonNull Context context) {
      super(context, 0);
    }

    @Override
    public int getCount() {
      return availableDevices.size();
    }

    @Nullable
    @Override
    public ToothbrushScanResult getItem(int position) {
      return availableDevices.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      DeviceViewHolder holder;
      if (convertView == null) {
        convertView =
            LayoutInflater.from(getContext()).inflate(R.layout.device_list_row, parent, false);
        holder = new DeviceViewHolder(convertView);

        convertView.setTag(holder);
      } else {
        holder = (DeviceViewHolder) convertView.getTag();
      }

      holder.render(getItem(position));

      return convertView;
    }

    void addResult(@NonNull ToothbrushScanResult newResult) {
      if (!availableDevices.contains(newResult)) {
        availableDevices.add(newResult);
        notifyDataSetChanged();
      }
    }

    void clearDevices() {
      availableDevices.clear();
      notifyDataSetChanged();
    }

    static class DeviceViewHolder {

      @BindView(R.id.scan_device_title)
      TextView title;

      @BindView(R.id.scan_device_subtitle)
      TextView subtitle;

      DeviceViewHolder(View view) {
        ButterKnife.bind(this, view);
      }

      @SuppressLint("SetTextI18n")
      void render(ToothbrushScanResult scanResult) {
        title.setText(scanResult.getName() + "\tAddress: " + scanResult.getMac());
        subtitle.setText(
            "Owner Id: "
                + scanResult.getOwnerDevice()
                + "\tIs Seamless: "
                + scanResult.isSeamlessConnectionAvailable());
      }
    }
  }

  /** Interface required to be implemented by activity. */
  public interface OnDeviceSelectedListener {

    /** Fired when user selected the device. */
    void onDeviceSelected(@NonNull ToothbrushScanResult scanResult);

    /** Fired when scanner dialog has been cancelled without selecting a device. */
    void onDialogCanceled();
  }
}
