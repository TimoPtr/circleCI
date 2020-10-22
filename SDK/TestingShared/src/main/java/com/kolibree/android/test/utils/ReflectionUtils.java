package com.kolibree.android.test.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

  public static <T> void setPrivateField(
      Class<T> clazz, T instance, String fieldName, Object newValue) {
    try {
      Field f = clazz.getDeclaredField(fieldName);

      f.setAccessible(true);

      f.set(instance, f.getType().cast(newValue));
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  @Nullable
  public static void invokeProtectedVoidMethod(Object object, String methodName) {
    invokeProtectedMethod(object, methodName, new Class<?>[] {});
  }

  @Nullable
  public static <T> T invokeProtectedMethod(Object object, String methodName) {
    return invokeProtectedMethod(object, methodName, new Class<?>[] {});
  }

  @Nullable
  public static <T> T invokeProtectedMethod(
      Object object, String methodName, Class<?>... parameterTypes) {
    try {
      Method m = findMethod(object, methodName, parameterTypes);

      m.setAccessible(true);

      return (T) m.invoke(object, (Object[]) parameterTypes);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    return null;
  }

  @NonNull
  private static Method findMethod(Object object, String methodName, Class<?>... parameterTypes)
      throws NoSuchMethodException {
    Method m = null;
    Class<?> clazz = object.getClass();
    while (clazz != null) {
      try {
        m = clazz.getDeclaredMethod(methodName, parameterTypes);
      } catch (NoSuchMethodException e) {
        // ignore
      }

      clazz = clazz.getSuperclass();
    }

    if (m == null) {
      throw new NoSuchMethodException(methodName);
    }

    return m;
  }
}
