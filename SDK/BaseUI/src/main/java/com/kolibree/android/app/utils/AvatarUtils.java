/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import com.kolibree.android.annotation.VisibleForApp;
import com.kolibree.android.baseui.R;
import com.kolibree.android.extensions.ContextExtensionsKt;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by aurelien on 01/10/15.
 *
 * <p>Kolibree avatars utility
 *
 * <p>Courtesy of :
 * http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework/20559175#20559175
 */
@VisibleForApp
public final class AvatarUtils implements AvatarDataStore {

  @Inject
  AvatarUtils() {}

  /**
   * Changes the orientation of the bitmap.
   *
   * @param bitmap the bitmap
   * @param orientation the desired orientation
   * @return the bitmap with the specified orientation
   */
  public static Bitmap fixOrientation(Bitmap bitmap, int orientation) {
    Bitmap out;

    switch (orientation) {
      case ExifInterface.ORIENTATION_ROTATE_90:
        out = rotate(bitmap, 90);
        bitmap.recycle();
        break;

      case ExifInterface.ORIENTATION_ROTATE_180:
        out = rotate(bitmap, 180);
        bitmap.recycle();
        break;

      case ExifInterface.ORIENTATION_ROTATE_270:
        out = rotate(bitmap, 270);
        bitmap.recycle();
        break;

      case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
        out = flip(bitmap, true, false);
        bitmap.recycle();
        break;

      case ExifInterface.ORIENTATION_FLIP_VERTICAL:
        out = flip(bitmap, false, true);
        bitmap.recycle();
        break;

      default:
        out = bitmap;
    }

    return out;
  }

  private static Bitmap rotate(Bitmap bitmap, float degrees) {
    Matrix matrix = new Matrix();
    matrix.postRotate(degrees);
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
  }

  private static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
    Matrix matrix = new Matrix();
    matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
  }

  /**
   * Save the bitmap to local storage.
   *
   * @param context the context to use
   * @param avatar the bitmap to save
   * @return true if bitmap was successfully changes, null otherwise
   */
  @Nullable
  @Override
  public File saveToStorage(@NonNull Context context, @NonNull Bitmap avatar) {
    final String name = System.currentTimeMillis() + ".jpg";
    final File file = new File(context.getCacheDir(), name);

    try {
      final FileOutputStream out = new FileOutputStream(file);
      avatar.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.flush();
      out.close();

      return file;
    } catch (Exception exception) {
      Timber.e(exception);
    }

    return null;
  }

  /**
   * Given a profile name, return a Drawable with the initial letter in uppercase inside a circle,
   * similar to Gmail avatars.
   *
   * @param context a Context
   * @param profileName the profile name from where we will extract the initial letter
   * @param backgroundColorResId color resource id of the background
   * @param textColorResId color resource id of the letter
   * @param typeface (optional) font for the letter, if null - standard typeface with bold is used
   * @param borderColorResId (optional) color of the border around circle
   * @return a Drawable with the initial letter in uppercase inside a circle
   */
  @NonNull
  public static Drawable getGmailLikeAvatar(
      @NonNull Context context,
      @NonNull String profileName,
      @ColorRes int backgroundColorResId,
      @ColorRes int textColorResId,
      @Nullable Typeface typeface,
      @Nullable @ColorRes Integer borderColorResId) {
    return getGmailLikeAvatar(
        profileName,
        ContextCompat.getColor(context, backgroundColorResId),
        ContextCompat.getColor(context, textColorResId),
        typeface,
        borderColorResId == null ? null : ContextCompat.getColor(context, borderColorResId));
  }

  private static Drawable getGmailLikeAvatar(
      @NonNull String profileName,
      int backgroundColor,
      int textColor,
      @Nullable Typeface typeface,
      @Nullable Integer borderColor) {
    TextDrawable.IConfigBuilder builder =
        TextDrawable.builder()
            .beginConfig()
            .textColor(textColor)
            .withBorder(
                borderColor == null ? 0 : 10,
                borderColor == null ? R.color.transparent : borderColor);
    if (typeface != null) {
      builder = builder.useFont(typeface);
    } else {
      builder = builder.bold();
    }
    return builder
        .endConfig()
        .buildRound(
            profileName.isEmpty()
                ? "?"
                : String.valueOf(profileName.charAt(0)).toUpperCase(Locale.getDefault()),
            backgroundColor);
  }

  /**
   * Given a profile name, return a Drawable with the initial letter in uppercase inside a circle,
   * similar to Gmail avatars.
   *
   * @param context a Context
   * @param profileName the profile name from where we will extract the initial letter
   * @return a Drawable with the initial letter in uppercase inside a circle
   */
  @Deprecated
  @NonNull
  public static Drawable getGmailLikeAvatar(
      @NonNull Context context, @Nullable String profileName) {
    return getGmailLikeAvatar(
        profileName == null ? "" : profileName,
        R.color.white,
        ContextExtensionsKt.getColorFromAttr(context, R.attr.colorPrimary),
        null,
        ContextExtensionsKt.getColorFromAttr(context, R.attr.colorPrimary));
  }
}
