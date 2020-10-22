package com.kolibree.android.test.espresso_helpers;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.test.espresso.matcher.BoundedMatcher;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/** Created by miguelaragues on 21/9/17. */
public abstract class CustomMatchers {

  private CustomMatchers() {}

  public static Matcher<View> withDrawable(final int resourceId) {
    return new DrawableMatcher(resourceId);
  }

  public static Matcher<View> withDrawable(Drawable drawable) {
    return new DrawableMatcher(drawable);
  }

  public static Matcher<View> withDrawable(Drawable drawable, File srcFile) {
    return new DrawableMatcher(drawable, srcFile);
  }

  public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
    return new RecyclerViewMatcher(recyclerViewId);
  }

  public static Matcher<View> withLineCount(int lineCount) {
    return new TextLineCountMatcher(lineCount);
  }

  public static Matcher<View> withProgress(final int expectedProgress) {
    return new BoundedMatcher<View, ProgressBar>(ProgressBar.class) {
      private int realProgress = -1;

      @Override
      public void describeTo(Description description) {
        description.appendText("expected: ");
        description.appendText(String.valueOf(expectedProgress));
        description.appendText(", was: ");
        description.appendText(String.valueOf(realProgress));
      }

      @Override
      public boolean matchesSafely(ProgressBar progressBar) {
        realProgress = progressBar.getProgress();

        return realProgress == expectedProgress;
      }
    };
  }

  public static Matcher<View> withTextInputLayoutError(final String expectedErrorText) {
    return new TypeSafeMatcher<View>() {

      @Override
      public boolean matchesSafely(View view) {
        if (!(view instanceof TextInputLayout)) {
          return false;
        }

        CharSequence error = ((TextInputLayout) view).getError();

        if (error == null) {
          return false;
        }

        String hint = error.toString();

        return expectedErrorText.equals(hint);
      }

      @Override
      public void describeTo(Description description) {}
    };
  }

  public static Matcher<View> withBackgroundColor(@ColorInt Integer color) {
    return new TypeSafeMatcher<View>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("expected: ");
        description.appendText("" + color);
      }

      @Override
      protected boolean matchesSafely(View item) {
        return item.getBackground() instanceof ColorDrawable
            && ((ColorDrawable) item.getBackground()).getColor()
                == ContextCompat.getColor(item.getContext(), color);
      }
    };
  }
}
