/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach;

import static com.kolibree.android.jaws.coach.AndroidConfigModule.NamedKey.EXECUTE_GL_CONFIG_CHANGES;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import javax.inject.Inject;
import javax.inject.Named;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import timber.log.Timber;

/**
 * AndroidConfigChooser is used to determine the best suited EGL Config
 *
 * <p>https://android.googlesource.com/platform/external/jmonkeyengine/+/refs/heads/marshmallow-mr1-dev/engine/src/android/com/jme3/system/android/AndroidConfigChooser.java
 *
 * <p>It belong to a game engine, jMonkeyEngine
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
public class AndroidConfigChooser implements EGLConfigChooser {
  private int clientOpenGLESVersion = 0;
  private EGLConfig choosenConfig = null;
  private ConfigType type;
  private int pixelFormat;
  private static final int EGL_OPENGL_ES2_BIT = 4;

  private final boolean executeGlConfigChanges;

  public enum ConfigType {
    /** RGB565, 0 alpha, 16 depth, 0 stencil */
    FASTEST,
    /** RGB???, 0 alpha, >=16 depth, 0 stencil */
    BEST,
    /**
     * Turn off config chooser and use hardcoded setEGLContextClientVersion(2);
     * setEGLConfigChooser(5, 6, 5, 0, 16, 0);
     */
    LEGACY
  }

  @Inject
  public AndroidConfigChooser(@Named(EXECUTE_GL_CONFIG_CHANGES) Boolean executeGlConfigChanges) {
    this.executeGlConfigChanges = executeGlConfigChanges;
    this.type = ConfigType.BEST;
  }
  /** Gets called by the GLSurfaceView class to return the best config */
  @Override
  public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
    Timber.d("GLSurfaceView asks for egl config, returning: ");
    logEGLConfig(choosenConfig, display, egl);
    return choosenConfig;
  }
  /**
   * findConfig is used to locate the best config and init the chooser with
   *
   * @param egl
   * @param display
   * @return true if successfull, false if no config was found
   */
  boolean findConfig(EGL10 egl, EGLDisplay display) {
    if (!executeGlConfigChanges) {
      return false;
    }

    if (type == ConfigType.BEST) {
      ComponentSizeChooser compChooser = new ComponentSizeChooser(8, 8, 8, 8, 32, 0);
      choosenConfig = compChooser.chooseConfig(egl, display);
      if (choosenConfig == null) {
        compChooser = new ComponentSizeChooser(8, 8, 8, 0, 32, 0);
        choosenConfig = compChooser.chooseConfig(egl, display);
        if (choosenConfig == null) {
          compChooser = new ComponentSizeChooser(8, 8, 8, 8, 16, 0);
          choosenConfig = compChooser.chooseConfig(egl, display);
          if (choosenConfig == null) {
            compChooser = new ComponentSizeChooser(8, 8, 8, 0, 16, 0);
            choosenConfig = compChooser.chooseConfig(egl, display);
          }
        }
      }
      Timber.d("Using best EGL configuration available here: ");
    } else {
      ComponentSizeChooser compChooser = new ComponentSizeChooser(5, 6, 5, 0, 16, 0);
      choosenConfig = compChooser.chooseConfig(egl, display);
      Timber.d("Using fastest EGL configuration available here: ");
    }
    if (choosenConfig != null) {
      Timber.d("Using chosen config: ");
      logEGLConfig(choosenConfig, display, egl);
      pixelFormat = getPixelFormat(choosenConfig, display, egl);
      clientOpenGLESVersion = getOpenGLVersion(choosenConfig, display, egl);
      return true;
    } else {
      Timber.d(
          "Unable to get a valid OpenGL ES 2.0 config, nether Fastest nor Best found! Bug. Please report this.");
      clientOpenGLESVersion = 1;
      pixelFormat = PixelFormat.UNKNOWN;
      return false;
    }
  }

  private int getPixelFormat(EGLConfig conf, EGLDisplay display, EGL10 egl) {
    int[] value = new int[1];
    int result = PixelFormat.RGB_565;
    egl.eglGetConfigAttrib(display, conf, EGL10.EGL_RED_SIZE, value);
    if (value[0] == 8) {
      result = PixelFormat.RGBA_8888;
    }
    Timber.v("Using PixelFormat %d", result);
    return PixelFormat.TRANSPARENT;
  }

  private int getOpenGLVersion(EGLConfig conf, EGLDisplay display, EGL10 egl) {
    int[] value = new int[1];
    int result = 1;
    egl.eglGetConfigAttrib(display, conf, EGL10.EGL_RENDERABLE_TYPE, value);
    // Check if conf is OpenGL ES 2.0
    if ((value[0] & EGL_OPENGL_ES2_BIT) != 0) {
      result = 2;
    }
    return result;
  }
  /**
   * log output with egl config details
   *
   * @param conf
   * @param display
   * @param egl
   */
  void logEGLConfig(EGLConfig conf, EGLDisplay display, EGL10 egl) {
    int[] value = new int[1];
    egl.eglGetConfigAttrib(display, conf, EGL10.EGL_RED_SIZE, value);
    Timber.d("EGL_RED_SIZE  = %d", value[0]);
    egl.eglGetConfigAttrib(display, conf, EGL10.EGL_GREEN_SIZE, value);
    Timber.d("EGL_GREEN_SIZE  = %d", value[0]);
    egl.eglGetConfigAttrib(display, conf, EGL10.EGL_BLUE_SIZE, value);
    Timber.d("EGL_BLUE_SIZE  = %d", value[0]);
    egl.eglGetConfigAttrib(display, conf, EGL10.EGL_ALPHA_SIZE, value);
    Timber.d("EGL_ALPHA_SIZE  = %d", value[0]);
    egl.eglGetConfigAttrib(display, conf, EGL10.EGL_DEPTH_SIZE, value);
    Timber.d("EGL_DEPTH_SIZE  = %d", value[0]);
    egl.eglGetConfigAttrib(display, conf, EGL10.EGL_STENCIL_SIZE, value);
    Timber.d("EGL_STENCIL_SIZE  = %d", value[0]);
    egl.eglGetConfigAttrib(display, conf, EGL10.EGL_RENDERABLE_TYPE, value);
    Timber.d("EGL_RENDERABLE_TYPE  = %d", value[0]);
    egl.eglGetConfigAttrib(display, conf, EGL10.EGL_SURFACE_TYPE, value);
    Timber.d("EGL_SURFACE_TYPE  = %d", value[0]);
  }

  int getClientOpenGLESVersion() {
    return clientOpenGLESVersion;
  }

  public void setClientOpenGLESVersion(int clientOpenGLESVersion) {
    this.clientOpenGLESVersion = clientOpenGLESVersion;
  }

  public int getPixelFormat() {
    return pixelFormat;
  }

  abstract class BaseConfigChooser implements EGLConfigChooser {
    private boolean bClientOpenGLESVersionSet;

    BaseConfigChooser(int[] configSpec) {
      bClientOpenGLESVersionSet = false;
      mConfigSpec = filterConfigSpec(configSpec);
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
      int[] num_config = new int[1];
      if (!egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config)) {
        throw new IllegalArgumentException("eglChooseConfig failed");
      }
      int numConfigs = num_config[0];
      if (numConfigs <= 0) {
        return null;
      }
      EGLConfig[] configs = new EGLConfig[numConfigs];
      if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs, num_config)) {
        throw new IllegalArgumentException("eglChooseConfig#2 failed");
      }
      EGLConfig config = chooseConfig(egl, display, configs);
      return config;
    }

    abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs);

    int[] mConfigSpec;

    private int[] filterConfigSpec(int[] configSpec) {
      if (bClientOpenGLESVersionSet) {
        return configSpec;
      }
      /*
       * We know none of the subclasses define EGL_RENDERABLE_TYPE. And we
       * know the configSpec is well formed.
       */
      int len = configSpec.length;
      int[] newConfigSpec = new int[len + 2];
      System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
      newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE;
      newConfigSpec[len] = 4;
      /* EGL_OPENGL_ES2_BIT*/
      newConfigSpec[len + 1] = EGL10.EGL_NONE;
      bClientOpenGLESVersionSet = true;
      return newConfigSpec;
    }
  }
  /**
   * Choose a configuration with exactly the specified r,g,b,a sizes, and at least the specified
   * depth and stencil sizes.
   */
  private class ComponentSizeChooser extends BaseConfigChooser {
    private int[] value;
    // Subclasses can adjust these values:
    int redSize;
    int greenSize;
    int blueSize;
    int alphaSize;
    int depthSize;
    int stencilSize;

    ComponentSizeChooser(
        int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
      super(
          new int[] {
            EGL10.EGL_RED_SIZE, redSize,
            EGL10.EGL_GREEN_SIZE, greenSize,
            EGL10.EGL_BLUE_SIZE, blueSize,
            EGL10.EGL_ALPHA_SIZE, alphaSize,
            EGL10.EGL_DEPTH_SIZE, depthSize,
            EGL10.EGL_STENCIL_SIZE, stencilSize,
            EGL10.EGL_NONE
          });
      value = new int[1];
      this.redSize = redSize;
      this.greenSize = greenSize;
      this.blueSize = blueSize;
      this.alphaSize = alphaSize;
      this.depthSize = depthSize;
      this.stencilSize = stencilSize;
    }

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
      for (EGLConfig config : configs) {
        int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
        int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
        if ((d >= depthSize) && (s >= stencilSize)) {
          int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
          int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
          int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
          int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
          if ((r == redSize) && (g == greenSize) && (b == blueSize) && (a == alphaSize)) {
            return config;
          }
        }
      }
      return null;
    }

    private int findConfigAttrib(
        EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
      if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
        return value[0];
      }
      return defaultValue;
    }
  }
}
