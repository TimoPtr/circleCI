package com.kolibree.android.jaws.opengl;

import android.opengl.GLES20;
import android.opengl.Matrix;
import androidx.annotation.NonNull;
import java.nio.FloatBuffer;

/**
 * Abstract class that implements all calls to opengl to draw objects
 *
 * <p>Subclasses must provide vertex shader and specify whether the shaders supports specific
 * features
 *
 * @author andresoviedo
 */
// TODO remove https://kolibree.atlassian.net/browse/KLTB002-8364
public final class Object3DImpl implements Object3D {

  private static final String FRAGMENT_SHADER_CODE =
      "precision mediump float;\n"
          + "varying vec4 v_Color;\n"
          + "void main() {\n"
          + "  gl_FragColor = v_Color;\n"
          + "}";

  // Transformations
  private final float[] mMatrix = new float[16];
  // mvp matrix
  private final float[] mvMatrix = new float[16];
  private final float[] mvpMatrix = new float[16];
  // OpenGL data
  private final int mProgram;

  public Object3DImpl(@NonNull String vertexShaderCode) {
    // prepare shaders and OpenGL program
    int vertexShader = ShaderUtils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
    int fragmentShader = ShaderUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);
    mProgram =
        ShaderUtils.createAndLinkProgram(
            vertexShader, fragmentShader, new String[] {"a_Position", "a_Color", "a_Normal"});
  }

  @Override
  public void draw(OptimizedVbo vbo, float[] pMatrix, float[] vMatrix) {

    // Add program to OpenGL environment
    GLES20.glUseProgram(mProgram);

    float[] mMatrix = getMMatrix(vbo);
    float[] mvMatrix = getMvMatrix(mMatrix, vMatrix);
    float[] mvpMatrix = getMvpMatrix(mvMatrix, pMatrix);

    setMvpMatrix(mvpMatrix);

    int mPositionHandle = setPosition(vbo);

    int mColorHandle = setColors();

    int mNormalHandle = setNormals(vbo);

    setMvMatrix(mvMatrix);

    drawShape(vbo, mColorHandle);

    // Disable vertex array
    GLES20.glDisableVertexAttribArray(mPositionHandle);
    GLES20.glDisableVertexAttribArray(mColorHandle);
    GLES20.glDisableVertexAttribArray(mNormalHandle);
  }

  /*
  Compute the transformation matrix

  Transformations are applied right to left so in fact the ones in this method will be applied from
  the last to the first
  Knowing that can save you 2 days of work :)
   */
  private float[] getMMatrix(OptimizedVbo vbo) {
    // Reset matrix in an euclidian referential
    Matrix.setIdentityM(mMatrix, 0);

    // Rotate object from world point of view
    Matrix.rotateM(mMatrix, 0, vbo.getRotationVector().getX(), 1f, 0f, 0f);
    Matrix.rotateM(mMatrix, 0, vbo.getRotationVector().getY(), 0, 1f, 0f);
    Matrix.rotateM(mMatrix, 0, vbo.getRotationVector().getZ(), 0, 0, 1f);

    // Apply scaling factor
    Matrix.scaleM(
        mMatrix,
        0,
        vbo.getScaleVector().getX(),
        vbo.getScaleVector().getY(),
        vbo.getScaleVector().getZ());

    // Set object position in the world
    Matrix.translateM(
        mMatrix,
        0,
        vbo.getPositionVector().getX(),
        vbo.getPositionVector().getY(),
        vbo.getPositionVector().getZ());

    // Rotate object from its own point of view
    Matrix.rotateM(mMatrix, 0, vbo.getSelfRotationVector().getX(), 1f, 0f, 0f);
    Matrix.rotateM(mMatrix, 0, vbo.getSelfRotationVector().getY(), 0, 1f, 0f);
    Matrix.rotateM(mMatrix, 0, vbo.getSelfRotationVector().getZ(), 0, 0, 1f);

    return mMatrix;
  }

  private float[] getMvMatrix(float[] mMatrix, float[] vMatrix) {
    Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, mMatrix, 0);
    return mvMatrix;
  }

  private float[] getMvpMatrix(float[] mvMatrix, float[] pMatrix) {
    Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0);
    return mvpMatrix;
  }

  private void setMvpMatrix(float[] mvpMatrix) {

    // get handle to shape's transformation matrix
    int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");

    // Apply the projection and view transformation
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
  }

  private int setColors() {
    return GLES20.glGetAttribLocation(mProgram, "a_Color");
  }

  private int setPosition(OptimizedVbo vbo) {

    // get handle to vertex shader's a_Position member
    int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");

    // Enable a handle to the triangle vertices
    GLES20.glEnableVertexAttribArray(mPositionHandle);

    FloatBuffer vertexBuffer = vbo.getVertexBuffer();
    vertexBuffer.position(0);
    GLES20.glVertexAttribPointer(
        mPositionHandle,
        Object3D.COORDS_PER_VERTEX,
        GLES20.GL_FLOAT,
        false,
        Object3D.VERTEX_STRIDE,
        vertexBuffer);

    return mPositionHandle;
  }

  private int setNormals(OptimizedVbo vbo) {
    int mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");

    GLES20.glEnableVertexAttribArray(mNormalHandle);

    // Pass in the normal information
    FloatBuffer buffer = vbo.getNormalBuffer();
    buffer.position(0);
    GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, buffer);

    return mNormalHandle;
  }

  private void setMvMatrix(float[] mvMatrix) {
    int mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");

    // Pass in the modelview matrix.
    GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);
  }

  private void drawShape(OptimizedVbo vbo, int colorHandle) {
    FloatBuffer vertexBuffer = vbo.getVertexBuffer();
    vertexBuffer.position(0);

    for (OptimizedMaterial material : vbo.getMaterials()) {
      GLES20.glVertexAttrib4fv(colorHandle, material.getColor(), 0);

      for (int[] faceRange : material.getFacesIndexRanges()) {
        int first = faceRange[0] * 3;
        int count = getFaceRangeLength(faceRange) * 3;
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, first, count);
      }
    }
  }

  private int getFaceRangeLength(int[] faceRange) {
    return faceRange[1] - faceRange[0] + 1;
  }
}
