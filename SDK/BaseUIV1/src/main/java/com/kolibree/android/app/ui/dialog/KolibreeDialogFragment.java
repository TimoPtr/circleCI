package com.kolibree.android.app.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.kolibree.android.baseui.v1.R;

/** Created by aurelien on 13/10/15. */
@Deprecated
public abstract class KolibreeDialogFragment extends BaseDialogFragment {

  private Unbinder unbinder;

  protected final View inflateView(LayoutInflater inflater, ViewGroup container, int layoutResId) {
    View view = inflater.inflate(layoutResId, container, false);
    bindView(view);
    return view;
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

  TextView title;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    final View v = inflater.inflate(R.layout.dialog_kolibree, container, false);

    title = v.findViewById(R.id.dialog_title);

    // Content
    final FrameLayout dialogContainer = v.findViewById(R.id.dialog_container);
    final View content = inflateView(inflater, container, getContentLayout());
    dialogContainer.addView(content);

    return v;
  }

  @LayoutRes
  protected abstract int getContentLayout();

  protected final void setTitle(String title) {
    this.title.setText(title);
  }

  protected final void setTitle(@StringRes int titleResId) {
    setTitle(getActivity().getString(titleResId));
  }

  protected void hideTitle() {
    this.title.setVisibility(View.GONE);
  }
}
