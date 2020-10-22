package com.kolibree.android.jaws.opengl;

// http://stackoverflow.com/questions/14607640/rotating-a-vector-in-3d-space

import android.opengl.Matrix;

/*
Class Name:

CCamera.

Created by:

Allen Sherrod (Programming Ace of www.UltimateGameProgramming.com).

Description:

This class represents a camera in a 3D scene.
*/

// TODO make our own https://kolibree.atlassian.net/browse/KLTB002-9173
public class Camera {

  public static final float UP = 0.5f; // Forward speed.

  float[] matrix = new float[16];
  float[] buffer = new float[12 + 12 + 16 + 16];
  float xPos, yPos; // Camera position.
  float zPos;
  float xView, yView, zView; // Look at position.
  float xUp, yUp, zUp; // Up direction.
  private long animationCounter;
  private Object[] lastAction;
  private boolean changed = false;

  Camera() {
    // Initialize variables...
    this(0, 0, 6, 0, 0, -1, 0, 1, 0);
  }

  private Camera(
      float xPos,
      float yPos,
      float zPos,
      float xView,
      float yView,
      float zView,
      float xUp,
      float yUp,
      float zUp) {
    // Here we set the camera to the values sent in to us. This is mostly
    // used to set up a
    // default position.
    this.xPos = xPos;
    this.yPos = yPos;
    this.zPos = zPos;
    this.xView = xView;
    this.yView = yView;
    this.zView = zView;
    this.xUp = xUp;
    this.yUp = yUp;
    this.zUp = zUp;
  }

  public static void createRotationMatrixAroundVector(
      float[] matrix, int offset, float angle, float x, float y, float z) {
    float cos = (float) Math.cos(angle);
    float sin = (float) Math.sin(angle);
    float cos_1 = 1 - cos;

    // @formatter:off
    matrix[offset + 0] = cos_1 * x * x + cos;
    matrix[offset + 1] = cos_1 * x * y - z * sin;
    matrix[offset + 2] = cos_1 * z * x + y * sin;
    matrix[offset + 3] = 0;
    matrix[offset + 4] = cos_1 * x * y + z * sin;
    matrix[offset + 5] = cos_1 * y * y + cos;
    matrix[offset + 6] = cos_1 * y * z - x * sin;
    matrix[offset + 7] = 0;
    matrix[offset + 8] = cos_1 * z * x - y * sin;
    matrix[offset + 9] = cos_1 * y * z + x * sin;
    matrix[offset + 10] = cos_1 * z * z + cos;
    matrix[offset + 11] = 0;
    matrix[offset + 12] = 0;
    matrix[offset + 13] = 0;
    matrix[offset + 14] = 0;
    matrix[offset + 15] = 1;

    // @formatter:on
  }

  private static void multiplyMMV(
      float[] result,
      int retOffset,
      float[] matrix,
      int matOffet,
      float[] vector4Matrix,
      int vecOffset) {
    for (int i = 0; i < vector4Matrix.length / 4; i++) {
      Matrix.multiplyMV(
          result, retOffset + (i * 4), matrix, matOffet, vector4Matrix, vecOffset + (i * 4));
    }
  }

  synchronized void animate() {
    if (lastAction == null || animationCounter == 0) {
      lastAction = null;
      animationCounter = 100;
      return;
    }
    String method = (String) lastAction[0];
    if (method.equals("translate")) {
      float dX = (Float) lastAction[1];
      float dY = (Float) lastAction[2];
      translateCameraImpl(dX * animationCounter / 100, dY * animationCounter / 100);
    }
    animationCounter--;
  }

  public synchronized void moveCameraZ(float direction) {
    if (direction == 0) {
      return;
    }
    MoveCameraZImpl(direction);
    lastAction = new Object[] {"zoom", direction};
  }

  private void MoveCameraZImpl(float direction) {
    // Moving the camera requires a little more then adding 1 to the z or
    // subracting 1.
    // First we need to get the direction at which we are looking.
    float xLookDirection = 0, yLookDirection = 0, zLookDirection = 0;

    // The look direction is the view minus the position (where we are).
    xLookDirection = xView - xPos;
    yLookDirection = yView - yPos;
    zLookDirection = zView - zPos;

    // Normalize the direction.
    float dp = Matrix.length(xLookDirection, yLookDirection, zLookDirection);
    xLookDirection /= dp;
    yLookDirection /= dp;
    zLookDirection /= dp;

    // Call UpdateCamera to move our camera in the direction we want.
    UpdateCamera(xLookDirection, yLookDirection, zLookDirection, direction);
  }

  void UpdateCamera(float xDir, float yDir, float zDir, float dir) {

    Matrix.setIdentityM(matrix, 0);
    Matrix.translateM(matrix, 0, xDir * dir, yDir * dir, zDir * dir);

    Matrix.multiplyMV(buffer, 0, matrix, 0, getLocationVector(), 0);
    Matrix.multiplyMV(buffer, 4, matrix, 0, getLocationViewVector(), 0);
    Matrix.multiplyMV(buffer, 8, matrix, 0, getLocationUpVector(), 0);

    xPos = buffer[0] / buffer[3];
    yPos = buffer[1] / buffer[3];
    zPos = buffer[2] / buffer[3];
    xView = buffer[4] / buffer[7];
    yView = buffer[5] / buffer[7];
    zView = buffer[6] / buffer[7];
    xUp = buffer[8] / buffer[11];
    yUp = buffer[9] / buffer[11];
    zUp = buffer[10] / buffer[11];

    pointViewToOrigin();

    setChanged(true);
  }

  private void pointViewToOrigin() {
    xView = -xPos;
    yView = -yPos;
    zView = -zPos;
    float length = Matrix.length(xView, yView, zView);
    xView /= length;
    yView /= length;
    zView /= length;
  }

  /**
   * Translation is the movement that makes the Earth around the Sun. So in this context,
   * translating the camera means moving the camera around the Zero (0,0,0)
   *
   * <p>This implementation makes uses of 3D Vectors Algebra.
   *
   * <p>The idea behind this implementation is to translate the 2D user vectors (the line in the
   * screen) with the 3D equivalents.
   *
   * <p>In order to to that, we need to calculate the Right and Arriba vectors so we have a match
   * for user 2D vector.
   *
   * @param dX the X component of the user 2D vector, that is, a value between [-1,1]
   * @param dY the Y component of the user 2D vector, that is, a value between [-1,1]
   */
  public synchronized void translateCamera(float dX, float dY) {
    if (dX == 0 && dY == 0) {
      return;
    }
    translateCameraImpl(dX, dY);
    lastAction = new Object[] {"translate", dX, dY};
  }

  private void translateCameraImpl(float dX, float dY) {
    float vlen;

    // Translating the camera requires a directional vector to rotate
    // First we need to get the direction at which we are looking.
    // The look direction is the view minus the position (where we are).
    // Get the Direction of the view.
    float xLook = 0, yLook = 0, zLook = 0;
    xLook = xView - xPos;
    yLook = yView - yPos;
    zLook = zView - zPos;
    vlen = Matrix.length(xLook, yLook, zLook);
    xLook /= vlen;
    yLook /= vlen;
    zLook /= vlen;

    // Arriba is the 3D vector that is **almost** equivalent to the 2D user Y vector
    // Get the direction of the up vector
    float xArriba = 0, yArriba = 0, zArriba = 0;
    xArriba = xUp - xPos;
    yArriba = yUp - yPos;
    zArriba = zUp - zPos;
    // Normalize the Right.
    vlen = Matrix.length(xArriba, yArriba, zArriba);
    xArriba /= vlen;
    yArriba /= vlen;
    zArriba /= vlen;

    // Right is the 3D vector that is equivalent to the 2D user X vector
    // In order to calculate the Right vector, we have to calculate the cross product of the
    // previously calculated vectors...

    // The cross product is defined like:
    // A x B = (a1, a2, a3) x (b1, b2, b3) = (a2 * b3 - b2 * a3 , - a1 * b3 + b1 * a3 , a1 * b2 - b1
    // * a2)
    float xRight = 0, yRight = 0, zRight = 0;
    xRight = (yLook * zArriba) - (zLook * yArriba);
    yRight = (zLook * xArriba) - (xLook * zArriba);
    zRight = (xLook * yArriba) - (yLook * xArriba);
    // Normalize the Right.
    vlen = Matrix.length(xRight, yRight, zRight);
    xRight /= vlen;
    yRight /= vlen;
    zRight /= vlen;

    // Once we have the Look & Right vector, we can recalculate where is the final Arriba vector,
    // so its equivalent to the user 2D Y vector.
    xArriba = (yRight * zLook) - (zRight * yLook);
    yArriba = (zRight * xLook) - (xRight * zLook);
    zArriba = (xRight * yLook) - (yRight * xLook);
    // Normalize the Right.
    vlen = Matrix.length(xArriba, yArriba, zArriba);
    xArriba /= vlen;
    yArriba /= vlen;
    zArriba /= vlen;

    float[] coordinates =
        new float[] {xPos, yPos, zPos, 1, xView, yView, zView, 1, xUp, yUp, zUp, 1};

    if (dX != 0 && dY != 0) {

      // in this case the user is drawing a diagonal line:    \v     ^\    v/     /^
      // so, we have to calculate the perpendicular vector of that diagonal

      // The perpendicular vector is calculated by inverting the X/Y values
      // We multiply the initial Right and Arriba vectors by the User's 2D vector
      xRight *= dY;
      yRight *= dY;
      zRight *= dY;
      xArriba *= dX;
      yArriba *= dX;
      zArriba *= dX;

      // Then we add the 2 affected vectors to the the final rotation vector
      float rotX, rotY, rotZ;
      rotX = xRight + xArriba;
      rotY = yRight + yArriba;
      rotZ = zRight + zArriba;
      vlen = Matrix.length(rotX, rotY, rotZ);
      rotX /= vlen;
      rotY /= vlen;
      rotZ /= vlen;

      // in this case we use the vlen angle because the diagonal is not perpendicular
      // to the initial Right and Arriba vectors
      createRotationMatrixAroundVector(buffer, 24, vlen, rotX, rotY, rotZ);
    } else if (dX != 0) {
      // in this case the user is drawing an horizontal line: <-- ó -->
      createRotationMatrixAroundVector(buffer, 24, dX, xArriba, yArriba, zArriba);
    } else {
      // in this case the user is drawing a vertical line: |^  v|
      createRotationMatrixAroundVector(buffer, 24, dY, xRight, yRight, zRight);
    }
    multiplyMMV(buffer, 0, buffer, 24, coordinates, 0);

    xPos = buffer[0] / buffer[3];
    yPos = buffer[1] / buffer[3];
    zPos = buffer[2] / buffer[3];
    xView = buffer[4 + 0] / buffer[4 + 3];
    yView = buffer[4 + 1] / buffer[4 + 3];
    zView = buffer[4 + 2] / buffer[4 + 3];
    xUp = buffer[8 + 0] / buffer[8 + 3];
    yUp = buffer[8 + 1] / buffer[8 + 3];
    zUp = buffer[8 + 2] / buffer[8 + 3];

    setChanged(true);
  }

  private float[] getLocationVector() {
    return new float[] {xPos, yPos, zPos, 1f};
  }

  private float[] getLocationViewVector() {
    return new float[] {xView, yView, zView, 1f};
  }

  private float[] getLocationUpVector() {
    return new float[] {xUp, yUp, zUp, 1f};
  }

  boolean hasChanged() {
    return changed;
  }

  public void setChanged(boolean changed) {
    this.changed = changed;
  }
}
