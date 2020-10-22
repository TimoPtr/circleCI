package cn.colgate.colgateconnect.base;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.disposables.CompositeDisposable;

public class BaseViewPagerFragment extends Fragment {

  protected CompositeDisposable disposables = new CompositeDisposable();

  private Unbinder unbinder;

  protected final View inflateView(LayoutInflater inflater, ViewGroup container, int layoutResId) {
    View view = inflater.inflate(layoutResId, container, false);
    bindView(view);
    return view;
  }

  @Override
  public void onAttach(@NonNull Context context) {
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

  private void bindView(View view) {
    if (unbinder != null) {
      unbinder.unbind();
    }

    unbinder = ButterKnife.bind(this, view);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (unbinder != null) {
      unbinder.unbind();
      unbinder = null;
    }
  }
}
