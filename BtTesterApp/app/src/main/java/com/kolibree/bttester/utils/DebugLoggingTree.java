package com.kolibree.bttester.utils;

import android.util.Log;
import timber.log.Timber;

/**
 * Logs Info messages to Android debug logging.
 *
 * <p>It will replicate whatever we log to medicomp's log, so we might want to adjust it in the
 * future in order to reduce the noise
 *
 * <p>This class exists so that we don't have to comment/uncomment log calls for unit testing, where
 * there's no access to android's Log.x() functions
 *
 * <p>Created by miguelaragues on 10/3/17.
 */
public class DebugLoggingTree extends Timber.DebugTree {
  @Override
  protected void log(int priority, String tag, String message, Throwable t) {
    switch (priority) {
      case Log.VERBOSE:
        Log.v(tag, message);
        break;
      case Log.DEBUG:
        Log.d(tag, message);
        break;
      case Log.INFO:
        Log.i(tag, message);
        break;
      case Log.WARN:
        Log.w(tag, message);
        break;
      case Log.ERROR:
        Log.e(tag, message);
        break;
    }
  }
}
