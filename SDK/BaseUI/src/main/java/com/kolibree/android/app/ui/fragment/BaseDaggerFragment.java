package com.kolibree.android.app.ui.fragment;

import static com.kolibree.android.extensions.DisposableUtils.addSafely;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/** Created by miguelaragues on 19/9/17. */
public abstract class BaseDaggerFragment extends BaseFragment {

  private final CompositeDisposable disposables = new CompositeDisposable();

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

  @Override
  public void onPause() {
    super.onPause();

    disposables.clear();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    disposables.dispose();
  }

  protected final void addToDisposables(Disposable disposable) {
    addSafely(disposables, disposable);
  }
}
