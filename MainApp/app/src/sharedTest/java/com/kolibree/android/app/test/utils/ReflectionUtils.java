package com.kolibree.android.app.test.utils;

import androidx.annotation.Nullable;
import java.lang.reflect.Field;

public final class ReflectionUtils {

  private ReflectionUtils() {}

  @Nullable
  public static Object getPrivateField(Object object, String fieldName) {
    try {
      Field f = object.getClass().getDeclaredField(fieldName);

      f.setAccessible(true);
      return f.get(object);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return null;
  }
}
