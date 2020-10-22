package com.kolibree.bttester.common;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import androidx.fragment.app.DialogFragment;
import dagger.android.support.AndroidSupportInjection;

/** Created by miguelaragues on 16/10/17. */
public abstract class BaseDaggerDialogFragment extends DialogFragment {

  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      // Perform injection here before M, L (API 22) and below because onAttach(Context)
      // is not yet available at L.
      AndroidSupportInjection.inject(this);
    }
    super.onAttach(activity);
  }

  @Override
  public void onAttach(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      // Perform injection here for M (API 23) due to deprecation of onAttach(Activity).
      AndroidSupportInjection.inject(this);
    }
    super.onAttach(context);
  }
}
