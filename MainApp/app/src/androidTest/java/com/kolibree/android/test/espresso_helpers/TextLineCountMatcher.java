package com.kolibree.android.test.espresso_helpers;

import android.view.View;
import android.widget.TextView;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/** {@link android.widget.TextView} line count matcher */
final class TextLineCountMatcher extends TypeSafeMatcher<View> {

  private final int expectedLineCount;

  private int currentLineCount;

  TextLineCountMatcher(int expectedLineCount) {
    super(View.class);
    this.expectedLineCount = expectedLineCount;
  }

  @Override
  protected boolean matchesSafely(View item) {
    return item instanceof TextView
        && (currentLineCount = ((TextView) item).getLineCount()) == expectedLineCount;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("expected " + expectedLineCount + " lines, got " + currentLineCount);
  }
}
