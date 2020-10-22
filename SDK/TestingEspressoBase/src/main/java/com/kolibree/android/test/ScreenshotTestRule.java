/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test;

import android.graphics.Bitmap;
import androidx.test.runner.screenshot.BasicScreenCaptureProcessor;
import androidx.test.runner.screenshot.ScreenCapture;
import androidx.test.runner.screenshot.ScreenCaptureProcessor;
import androidx.test.runner.screenshot.Screenshot;
import java.io.IOException;
import java.util.HashSet;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/** Created by miguelaragues on 27/2/18. */
public class ScreenshotTestRule extends TestWatcher {

  public void takeScreenshot(String screenShotName) {
    ScreenCapture capture = Screenshot.capture();
    capture.setName(screenShotName);
    capture.setFormat(Bitmap.CompressFormat.PNG);

    HashSet<ScreenCaptureProcessor> processors = new HashSet<>();
    processors.add(new BasicScreenCaptureProcessor());

    try {
      capture.process(processors);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void failed(Throwable e, Description description) {
    super.failed(e, description);
    takeScreenshot(
        description.getTestClass().getSimpleName()
            + "-"
            + description.getMethodName()
            + "_FAILURE");
  }
}
