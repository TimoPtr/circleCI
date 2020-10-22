package cn.colgate.colgateconnect.scan;

import com.kolibree.android.sdk.scan.ToothbrushScanResult;

// interface only used when the user clicks on a discovered toothbrush
public interface ScanView {

  void onItemClick(ToothbrushScanResult result);
}
