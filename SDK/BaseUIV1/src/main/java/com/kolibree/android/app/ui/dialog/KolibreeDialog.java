/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog;

import android.content.Context;
import android.widget.Button;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.kolibree.android.baseui.v1.R;
import timber.log.Timber;

/**
 * Created by aurelien on 08/04/17.
 *
 * <p>Branded dialog builder
 *
 * <p>13/11/2017 - maragues
 *
 * <p>Refactor so that it needs a FragmentActivity. This way we can listen to lifecycle events and
 * dismiss the dialog
 */
public final class KolibreeDialog implements DefaultLifecycleObserver {

  /**
   * Creates a lifecycle aware dialog that will dismiss itself when the lifecycle owner is destroyed
   */
  @NonNull
  public static KolibreeDialog create(@NonNull FragmentActivity appCompatActivity) {
    return new KolibreeDialog(appCompatActivity, appCompatActivity.getLifecycle());
  }

  /**
   * Creates a lifecycle aware dialog that will dismiss itself when the lifecycle owner is destroyed
   */
  @NonNull
  public static KolibreeDialog create(@NonNull Context context, @NonNull Lifecycle lifecycle) {
    return new KolibreeDialog(context, lifecycle);
  }

  /**
   * To be used only in DialogFragments
   *
   * <p>Creates a non-lifecycle aware dialog
   */
  @NonNull
  public static KolibreeDialog create(@NonNull Context context) {
    return new KolibreeDialog(context, null);
  }

  private final Context context;
  private final AlertDialog.Builder builder;

  private AlertDialog alertdialog;

  @ColorRes private int negativeButtonColor;

  private KolibreeDialog(@NonNull Context context, @Nullable Lifecycle lifecycle) {
    this.context = context.getApplicationContext();
    this.builder = new AlertDialog.Builder(context, R.style.KolibreeDialog);

    if (lifecycle != null) {
      lifecycle.addObserver(this);
    }
  }

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner) {
    if (alertdialog != null) {
      alertdialog.dismiss();
    }
  }

  @NonNull
  public KolibreeDialog title(@Nullable String title) {
    builder.setTitle(title);

    return this;
  }

  @NonNull
  public KolibreeDialog title(@StringRes int text) {
    return title(context.getString(text));
  }

  @NonNull
  public KolibreeDialog message(@Nullable String message) {
    builder.setMessage(message);

    return this;
  }

  @NonNull
  public KolibreeDialog message(@StringRes int text) {
    return message(context.getString(text));
  }

  @NonNull
  public KolibreeDialog neutralButton(
      @Nullable String text, @Nullable final DialogButtonCallback callback) {
    builder.setNeutralButton(
        text,
        (dialog, id) -> {
          dialog.dismiss();

          notifyOnClick(callback);
        });

    return this;
  }

  private void notifyOnClick(DialogButtonCallback callback) {
    if (callback != null) {
      callback.onClick();
    } else {
      Timber.w("Unable to notify callback, was null in %s", this);
    }
  }

  @NonNull
  public KolibreeDialog neutralButton(@StringRes int text) {
    return neutralButton(context.getString(text), null);
  }

  @NonNull
  public KolibreeDialog neutralButton(
      @StringRes int text, @Nullable final DialogButtonCallback callback) {
    return neutralButton(context.getString(text), callback);
  }

  @NonNull
  public KolibreeDialog positiveButton(@StringRes int text) {
    return positiveButton(context.getString(text));
  }

  @NonNull
  public KolibreeDialog positiveButton(@Nullable String text) {
    return positiveButton(text, null);
  }

  @NonNull
  public KolibreeDialog positiveButton(
      @StringRes int text, @Nullable DialogButtonCallback callback) {

    return positiveButton(context.getString(text), callback);
  }

  @NonNull
  public KolibreeDialog positiveButton(
      @Nullable String text, @Nullable final DialogButtonCallback callback) {
    builder.setPositiveButton(
        text,
        (dialog, id) -> {
          dialog.dismiss();

          notifyOnClick(callback);
        });

    return this;
  }

  @NonNull
  public KolibreeDialog negativeButton(String text, @Nullable final DialogButtonCallback callback) {
    builder.setNegativeButton(
        text,
        (dialog, id) -> {
          dialog.dismiss();

          notifyOnClick(callback);
        });

    negativeButtonColor = R.color.color_grey_selector;
    return this;
  }

  @NonNull
  public KolibreeDialog negativeButton(
      @StringRes int text, @Nullable DialogButtonCallback callback) {

    return negativeButton(context.getString(text), callback);
  }

  @NonNull
  public KolibreeDialog negativeButton(@StringRes int text) {

    return negativeButton(context.getString(text), null);
  }

  @NonNull
  public KolibreeDialog criticalButton(String text, @Nullable final DialogButtonCallback callback) {
    builder.setNegativeButton(
        text,
        (dialog, id) -> {
          dialog.dismiss();

          notifyOnClick(callback);
        });

    negativeButtonColor = R.color.red;
    return this;
  }

  @NonNull
  public KolibreeDialog cancelable(boolean isCancelable) {
    builder.setCancelable(isCancelable);

    return this;
  }

  public AlertDialog show() {
    create();

    alertdialog.show();

    styleButtons();

    return alertdialog;
  }

  private void styleButtons() {
    if (negativeButtonColor != 0) {
      final Button negative = alertdialog.getButton(AlertDialog.BUTTON_NEGATIVE);
      if (negative != null) {
        negative.setTextColor(ContextCompat.getColor(context, negativeButtonColor));
      }
    }
  }

  public AlertDialog create() {
    if (alertdialog == null) {
      alertdialog = builder.create();
    }

    return alertdialog;
  }

  public AlertDialog dialog() {
    return create();
  }

  public interface DialogButtonCallback {

    void onClick();
  }
}
