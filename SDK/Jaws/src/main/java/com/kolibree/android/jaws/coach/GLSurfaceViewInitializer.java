/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach;

import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import timber.log.Timber;

class GLSurfaceViewInitializer {

  public static void initEGLConfig(GLSurfaceView surfaceView, AndroidConfigChooser chooser) {
    EGL10 egl = (EGL10) EGLContext.getEGL();
    EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
    int[] version = new int[2];
    if (egl.eglInitialize(display, version)) {
      Timber.v("Display EGL Version: %d.%d", version[0], version[1]);
    }

    try {
      // Init chooser
      if (!chooser.findConfig(egl, display)) {
        Timber.e("Unable to find suitable EGL config");
        return;
      }

      int clientOpenGLESVersion = chooser.getClientOpenGLESVersion();
      if (clientOpenGLESVersion < 2) {
        Timber.e("OpenGL ES 2.0 is not supported on this device");
        return;
      }
      // Requesting client version from GLSurfaceView which is extended by
      // AndroidInput.
      surfaceView.setEGLContextClientVersion(clientOpenGLESVersion);
      surfaceView.setEGLConfigChooser(chooser);
    } finally {
      if (display != null) {
        egl.eglTerminate(display);
      }
    }
  }
}
