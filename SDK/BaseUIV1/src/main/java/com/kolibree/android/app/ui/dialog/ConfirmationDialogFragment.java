package com.kolibree.android.app.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/** Created by aurelien on 14/10/15. */
@Keep
public class ConfirmationDialogFragment extends DialogFragment {

  public static final String TAG = ConfirmationDialogFragment.class.getSimpleName();

  private static final String ARG_TITLE = "title";
  private static final String ARG_MESSAGE = "message";
  private static final String ARG_POSITIVE_BUTTON = "positive";
  private static final String ARG_NEGATIVE_BUTTON = "negative";
  private ConfirmationDialogCallback dialogCallback;

  public static Builder builder(@NonNull Context context) {
    return new Builder(context.getApplicationContext());
  }

  /**
   * Create a new ConfirmationDialogFragment that uses the specified parameters.
   *
   * @param title the title of the dialog fragment
   * @param message the message to display
   * @param positiveButtonText the text for the positive button
   * @param negativeButtonText the text for the negative button
   * @return a new ConfirmationDialogFragment that uses the specified parameters.
   */
  public static ConfirmationDialogFragment newInstance(
      String title, String message, String positiveButtonText, String negativeButtonText) {
    final ConfirmationDialogFragment dialog = new ConfirmationDialogFragment();
    final Bundle arguments = new Bundle();

    arguments.putString(ARG_TITLE, title);
    arguments.putString(ARG_MESSAGE, message);
    arguments.putString(ARG_POSITIVE_BUTTON, positiveButtonText);
    arguments.putString(ARG_NEGATIVE_BUTTON, negativeButtonText);
    dialog.setArguments(arguments);

    return dialog;
  }

  /**
   * Create a new ConfirmationDialogFragment that uses the specified parameters.
   *
   * @param title the resource id for the title of the dialog fragment
   * @param message the resource id for the message to display
   * @param positiveButtonText the resource id for the text for the positive button
   * @param negativeButtonText the resource id for the text for the negative button
   * @return a new ConfirmationDialogFragment that uses the specified parameters.
   */
  public static ConfirmationDialogFragment newInstance(
      Context context,
      @StringRes int title,
      @StringRes int message,
      @StringRes int positiveButtonText,
      @StringRes int negativeButtonText) {
    return newInstance(
        context.getString(title),
        context.getString(message),
        context.getString(positiveButtonText),
        context.getString(negativeButtonText));
  }

  /**
   * Create a new ConfirmationDialogFragment that uses the specified parameters.
   *
   * @param title the resource id for the title of the dialog fragment
   * @param message the resource id for the message to display
   * @param positiveButtonText the resource id for the text for the positive button
   * @return a new ConfirmationDialogFragment that uses the specified parameters.
   */
  public static ConfirmationDialogFragment newInstance(
      Context context,
      @StringRes int title,
      @StringRes int message,
      @StringRes int positiveButtonText) {
    return newInstance(
        context.getString(title),
        context.getString(message),
        context.getString(positiveButtonText),
        null);
  }

  /**
   * Show the ConfirmationDialogFragment unless it's already displayed.
   *
   * @param fragmentManager the fragment manager used to show the fragment
   */
  public void showIfNotPresent(FragmentManager fragmentManager) {
    fragmentManager.executePendingTransactions();
    Fragment fragment = fragmentManager.findFragmentByTag(TAG);

    if (fragment == null) {
      showNow(fragmentManager, TAG);
    }
  }

  /**
   * Set the ConfirmationDialogCallback to notify.
   *
   * @param dialogCallback the ConfirmationDialogCallback to notify.
   * @return this ConfirmationDialogFragment
   */
  public ConfirmationDialogFragment setConfirmationListener(
      ConfirmationDialogCallback dialogCallback) {
    this.dialogCallback = dialogCallback;

    return this;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    if (getArguments() == null) {
      return KolibreeDialog.create(getContext()).create();
    }

    final String title = getArguments().getString(ARG_TITLE);
    final String message = getArguments().getString(ARG_MESSAGE);
    final String yesButtonText = getArguments().getString(ARG_POSITIVE_BUTTON);
    final String noButtonText = getArguments().getString(ARG_NEGATIVE_BUTTON);

    KolibreeDialog dialog =
        KolibreeDialog.create(getContext())
            .positiveButton(yesButtonText, this::onPositiveButtonClicked);

    if (noButtonText != null) {
      dialog = dialog.negativeButton(noButtonText, this::onNegativeButtonClicked);
    }

    return dialog.title(title).message(message).create();
  }

  private void onNegativeButtonClicked() {
    ConfirmationDialogCallback confirmationDialogCallback = getConfirmationDialogCallback();

    if (confirmationDialogCallback != null) {
      confirmationDialogCallback.onConfirmation(false);
    }

    dismiss();
  }

  private void onPositiveButtonClicked() {
    ConfirmationDialogCallback confirmationDialogCallback = getConfirmationDialogCallback();

    if (confirmationDialogCallback != null) {
      confirmationDialogCallback.onConfirmation(true);
    }

    dismiss();
  }

  @Nullable
  private ConfirmationDialogCallback getConfirmationDialogCallback() {
    if (dialogCallback != null) {
      return dialogCallback;
    } else if (getTargetFragment() instanceof ConfirmationDialogCallback) {
      return ((ConfirmationDialogCallback) getTargetFragment());
    } else if (getActivity() instanceof ConfirmationDialogCallback) {
      return (ConfirmationDialogCallback) getActivity();
    }

    return null;
  }

  /** To be implemented by caller Fragment or Activity. */
  public interface ConfirmationDialogCallback {

    void onConfirmation(boolean answeredYes);
  }

  public static class Builder {

    private final Context applicationContext;
    private String title;
    private String message;
    private String confirmText;
    private String cancelText;

    private Builder(Context applicationContext) {
      this.applicationContext = applicationContext;
    }

    public Builder title(@StringRes int titleResId) {
      return title(applicationContext.getString(titleResId));
    }

    Builder title(@Nullable String title) {
      this.title = title;

      return this;
    }

    public Builder message(@StringRes int messageResId) {
      return message(applicationContext.getString(messageResId));
    }

    Builder message(@Nullable String message) {
      this.message = message;

      return this;
    }

    public Builder confirmText(@StringRes int confirmTextResId) {
      return confirmText(applicationContext.getString(confirmTextResId));
    }

    Builder confirmText(@Nullable String confirmText) {
      this.confirmText = confirmText;

      return this;
    }

    public Builder cancelText(@StringRes int cancelTextResId) {
      return cancelText(applicationContext.getString(cancelTextResId));
    }

    Builder cancelText(@Nullable String cancelText) {
      this.cancelText = cancelText;

      return this;
    }

    public ConfirmationDialogFragment create() {
      return newInstance(title, message, confirmText, cancelText);
    }
  }
}
