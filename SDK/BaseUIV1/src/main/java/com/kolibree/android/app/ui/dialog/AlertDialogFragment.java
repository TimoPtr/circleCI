package com.kolibree.android.app.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.kolibree.android.baseui.v1.R;

/**
 * Created by aurelien on 10/01/17.
 *
 * <p>Common alert dialog
 */
public final class AlertDialogFragment extends DialogFragment implements View.OnClickListener {

  private static final String ARG_TITLE = "argTitle";
  private static final String ARG_MESSAGE = "argMessage";
  private static final String ARG_BUTTON_TEXT = "argButtonText";
  private static final String ARG_ICON = "argIcon";
  private static final String ARG_CANCELABLE = "argCancelable";

  private AlertDialogCallback callback;

  /**
   * Alert dialog that will show a dialog with the values specified in the parameters. This dialog
   * will be cancelled if the user touches outside of it
   *
   * <p>The icon drawable will be tinted to primary color
   */
  @NonNull
  public static AlertDialogFragment newInstance(
      @NonNull String title,
      @NonNull String message,
      @Nullable String buttonText,
      @DrawableRes int icon,
      @Nullable AlertDialogCallback callback) {
    return newInstance(title, message, buttonText, icon, callback, true);
  }

  /**
   * Alert dialog that will show a non-cancelable dialog with the values specified in the
   * parameters.
   *
   * <p>The icon drawable will be tinted to primary color
   */
  @NonNull
  public static AlertDialogFragment nonCancelableInstance(
      @NonNull String title,
      @NonNull String message,
      @Nullable String buttonText,
      @DrawableRes int icon,
      @Nullable AlertDialogCallback callback) {
    return newInstance(title, message, buttonText, icon, callback, false);
  }

  @NonNull
  private static AlertDialogFragment newInstance(
      @NonNull String title,
      @NonNull String message,
      @Nullable String buttonText,
      @DrawableRes int icon,
      @Nullable AlertDialogCallback callback,
      boolean cancelable) {
    final Bundle args = new Bundle();
    args.putString(ARG_TITLE, title);
    args.putString(ARG_MESSAGE, message);
    args.putString(ARG_BUTTON_TEXT, buttonText);
    args.putInt(ARG_ICON, icon);
    args.putBoolean(ARG_CANCELABLE, cancelable);

    final AlertDialogFragment f = new AlertDialogFragment();
    f.setArguments(args);
    f.setCallback(callback);
    return f;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setCancelable(hasToBeCancelable());
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View v = inflater.inflate(R.layout.dialog_alert, container, false);

    // Title
    final TextView title = v.findViewById(R.id.alert_dialog_title);
    title.setText(getArguments().getString(ARG_TITLE));

    // Message
    final TextView message = v.findViewById(R.id.alert_dialog_message);
    message.setText(getArguments().getString(ARG_MESSAGE));

    // Button
    final Button button = v.findViewById(R.id.alert_dialog_button);
    if (getArguments().getString(ARG_BUTTON_TEXT) != null) {
      button.setText(getArguments().getString(ARG_BUTTON_TEXT));
      button.setOnClickListener(this);
    } else {
      button.setVisibility(View.INVISIBLE);
    }

    // Icon
    final ImageView icon = v.findViewById(R.id.alert_dialog_icon);
    icon.setImageResource(getArguments().getInt(ARG_ICON));

    return v;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getDialog().setCanceledOnTouchOutside(hasToBeCancelable());
  }

  @Override
  public void onClick(View view) {
    dismiss();

    if (callback != null) {
      callback.onAlertDialogClosed();
    }
  }

  private void setCallback(@Nullable AlertDialogCallback callback) {
    this.callback = callback;
  }

  private boolean hasToBeCancelable() {
    return getArguments() != null && getArguments().getBoolean(ARG_CANCELABLE, true);
  }

  public interface AlertDialogCallback {

    /** Called when the user clicks on the button. */
    void onAlertDialogClosed();
  }
}
