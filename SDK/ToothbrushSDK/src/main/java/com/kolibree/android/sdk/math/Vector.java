package com.kolibree.android.sdk.math;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import java.util.Arrays;

/**
 * Created by aurelien on 21/07/17.
 *
 * <p>Vector class used for raw data manipulation
 *
 * <p>This object is mutable Operation methods can be chained
 */
@Keep
public final class Vector {

  protected final float[] data;

  public Vector(float... data) {
    if (data.length != 3) {
      throw new IllegalArgumentException("Vectors have 3 axis");
    }

    this.data = data;
  }

  /**
   * Get the value of a specific axis
   *
   * @param axis Axis
   * @return T axis value
   */
  public float get(@NonNull Axis axis) {
    return data[axis.ordinal()];
  }

  /**
   * Set vector values
   *
   * @param x x axis value
   * @param y y axis value
   * @param z z axis value
   * @return this instance to chain operations
   */
  public @NonNull Vector set(float x, float y, float z) {
    data[0] = x;
    data[1] = y;
    data[2] = z;

    return this;
  }

  /**
   * Scalar product operation
   *
   * @param scalar float scalar number
   * @return this instance to chain operations
   */
  public @NonNull Vector scalar(float scalar) {
    data[0] *= scalar;
    data[1] *= scalar;
    data[2] *= scalar;

    return this;
  }

  /**
   * Subtract a vector to this one
   *
   * @param v non null Vector
   * @return this instance to chain operations
   */
  public @NonNull Vector subtract(@NonNull Vector v) {
    data[0] -= v.get(Axis.X);
    data[1] -= v.get(Axis.Y);
    data[2] -= v.get(Axis.Z);

    return this;
  }

  /**
   * Vector and Matrix product
   *
   * @param matrix non null Matrix
   * @return this instance to chain operations
   */
  public @NonNull Vector product(@NonNull Matrix matrix) {
    data[0] = data[0] * matrix.get(0, 0) + data[1] * matrix.get(0, 1) + data[2] * matrix.get(0, 2);
    data[1] = data[0] * matrix.get(1, 0) + data[1] * matrix.get(1, 1) + data[2] * matrix.get(1, 2);
    data[2] = data[0] * matrix.get(2, 0) + data[1] * matrix.get(2, 1) + data[2] * matrix.get(2, 2);

    return this;
  }

  @Override
  public String toString() {
    return "Vector{" + "data=" + Arrays.toString(data) + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Vector)) return false;
    Vector vector = (Vector) o;
    return Arrays.equals(data, vector.data);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }
}
