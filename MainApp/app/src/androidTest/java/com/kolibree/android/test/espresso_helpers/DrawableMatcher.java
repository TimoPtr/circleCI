package com.kolibree.android.test.espresso_helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.io.File;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Copied from
 *
 * <p>https://github.com/xrigau/droidcon-android-espresso/blob/master/app/src/instrumentTest/java/com/xrigau/droidcon/espresso/helper/DrawableMatcher.java
 */
public class DrawableMatcher extends TypeSafeMatcher<View> {

  private final int resourceId;
  private File expectedDrawableFile;
  private String resourceName = null;
  private Drawable expectedDrawable, actualDrawable = null;
  private View targetView;

  public DrawableMatcher(int resourceId) {
    super(View.class);
    this.resourceId = resourceId;
  }

  public DrawableMatcher(Drawable drawable) {
    this(0);
    expectedDrawable = drawable;
  }

  public DrawableMatcher(Drawable drawable, File expectedDrawableFile) {
    this(0);
    expectedDrawable = drawable;
    this.expectedDrawableFile = expectedDrawableFile;
  }

  @Override
  public boolean matchesSafely(View target) {
    targetView = target;

    if (expectedDrawable == null) {
      loadDrawableFromResources(target.getContext());
    }
    if (invalidExpectedDrawable()) {
      return false;
    }

    if (target instanceof ImageView) {
      return hasImage((ImageView) target) || hasBackground(target);
    }
    if (target instanceof TextView) {
      return hasCompoundDrawable((TextView) target) || hasBackground(target);
    }
    return hasBackground(target);
  }

  private void loadDrawableFromResources(@NonNull Context context) {
    try {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        expectedDrawable = context.getResources().getDrawable(resourceId);
      else expectedDrawable = context.getDrawable(resourceId);
      resourceName = context.getResources().getResourceEntryName(resourceId);
    } catch (Resources.NotFoundException ignored) {
      // view could be from a context unaware of the resource id.
    }
  }

  private boolean invalidExpectedDrawable() {
    return expectedDrawable == null;
  }

  private boolean hasImage(ImageView target) {
    return isSameDrawable(target.getDrawable());
  }

  private boolean hasCompoundDrawable(TextView target) {
    if (target.getCompoundDrawables() == null) {
      return false;
    }
    for (Drawable drawable : target.getCompoundDrawables()) {
      if (isSameDrawable(drawable)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasBackground(View target) {
    return isSameDrawable(target.getBackground());
  }

  private boolean isSameDrawable(Drawable drawable) {
    if (drawable == null) {
      return false;
    }

    actualDrawable = drawable;

    if (drawable instanceof BitmapDrawable && expectedDrawable instanceof BitmapDrawable)
      return ((BitmapDrawable) expectedDrawable)
          .getBitmap()
          .sameAs(((BitmapDrawable) drawable).getBitmap());

    return expectedDrawable.getConstantState().equals(drawable.getConstantState());
  }

  @Override
  public void describeTo(Description description) {
    if (resourceId != 0) {
      description.appendText("with drawable from resource id: ");
      description.appendValue(resourceId);
      if (resourceName != null) {
        description.appendText("[");
        description.appendText(resourceName);
        description.appendText("]");
      }
    } else if (expectedDrawable != null) {
      description.appendText("with expectedDrawable: ");
      description.appendValue(expectedDrawable);
      description.appendText(" got ");
      description.appendValue(actualDrawable);
      description.appendText(" on target ");
      description.appendValue(targetView);
      if (expectedDrawableFile != null) {
        description.appendText(" with file ");
        description.appendValue(expectedDrawableFile.getAbsolutePath());
        description.appendText(" with size ");
        description.appendValue(expectedDrawableFile.length());
      }
    }
  }
}
