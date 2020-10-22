/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kolibree.android.baseui.v1.R;

/**
 * Created by aurelien on 06/10/15.
 *
 * <p>Kolibree-branded popup dialog
 */
@Deprecated
public final class PopupDialogFragment extends KolibreeDialogFragment
    implements View.OnClickListener {

  public static PopupDialogFragment newInstance(String title, String message, int popupId) {
    final PopupDialogFragment f = new PopupDialogFragment();
    final Bundle args = new Bundle();
    args.putString("title", title);
    args.putString("message", message);
    args.putInt("popupid", popupId);
    f.setArguments(args);

    return f;
  }

  private int popupId;

  @Override
  public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
    final Bundle args = getArguments();

    // Title
    setTitle(args.getString("title"));

    // Message
    final TextView message = v.findViewById(R.id.dialog_message);
    message.setText(args.getString("message"));

    // Ok button
    final Button ok = v.findViewById(R.id.ok);
    ok.setOnClickListener(this);

    this.popupId = args.getInt("popupid");
  }

  @Override
  protected int getContentLayout() {
    return R.layout.dialog_popup;
  }

  @Override
  public void onClick(View view) {
    ((PopupClosedListener) (getTargetFragment() != null ? getTargetFragment() : getActivity()))
        .onPopupClosed(popupId);
    dismiss();
  }

  /** To be implemented by caller Fragment or Activity */
  public interface PopupClosedListener {

    void onPopupClosed(int popupId);
  }
}
