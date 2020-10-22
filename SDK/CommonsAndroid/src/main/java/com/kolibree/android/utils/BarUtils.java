package com.kolibree.android.utils;

import android.content.Context;
import android.util.TypedValue;
import androidx.annotation.Keep;

/** Created by Guillaume Agis on 04/12/2018. */
@Keep
public class BarUtils {

  private BarUtils() {}

  /**
   * Get android's status bar height.
   *
   * @param context non null {@link Context}
   * @return system status bar height
   */
  public static int getStatusBarHeight(Context context) {
    int result = 0;
    int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");

    if (resourceId > 0) {
      result = context.getResources().getDimensionPixelSize(resourceId);
    }

    return result;
  }

  /**
   * Get android's toolbar height.
   *
   * @param context non null {@link Context}
   * @return system toolbar's height
   */
  public static int getToolbarHeight(Context context) {
    final TypedValue tv = new TypedValue();

    if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
      return TypedValue.complexToDimensionPixelSize(
          tv.data, context.getResources().getDisplayMetrics());
    }

    return 0;
  }
}
