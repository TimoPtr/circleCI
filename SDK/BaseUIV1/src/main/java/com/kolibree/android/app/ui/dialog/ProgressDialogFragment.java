/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.kolibree.android.baseui.v1.R;
import com.kolibree.android.extensions.ContextExtensionsKt;

public class ProgressDialogFragment extends DialogFragment {

  private static final String TAG = ProgressDialogFragment.class.getSimpleName();

  /**
   * Show a ProgressDialogFragment if it's not already displayed.
   *
   * @param fragmentManager the fragment manager to be used
   * @return a non-null ProgressDialogFragment. The returned instance is not necessarily displayed
   */
  @NonNull
  public static ProgressDialogFragment showIfNotPresent(FragmentManager fragmentManager) {
    ProgressDialogFragment dialogFragment = findProgressDialogFragment(fragmentManager);
    if (dialogFragment == null) {
      dialogFragment = new ProgressDialogFragment();

      dialogFragment.showNow(fragmentManager, TAG);
    }

    return dialogFragment;
  }

  /**
   * Dismiss, if present, the ProgressDialogFragment.
   *
   * @param fragmentManager the fragment manager to be used
   */
  public static void maybeDismiss(FragmentManager fragmentManager) {
    ProgressDialogFragment dialogFragment = findProgressDialogFragment(fragmentManager);
    if (dialogFragment != null) {
      dialogFragment.dismiss();
    }
  }

  private static ProgressDialogFragment findProgressDialogFragment(
      FragmentManager fragmentManager) {
    return (ProgressDialogFragment) fragmentManager.findFragmentByTag(TAG);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setCancelable(false);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    ProgressDialog dialog = new ProgressDialog(getContext());
    dialog.setTitle(ContextExtensionsKt.appName(requireContext()));
    dialog.setMessage(getString(R.string.please_wait));
    dialog.setIndeterminate(true);

    return dialog;
  }
}
