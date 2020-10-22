package com.kolibree.android.jaws.opengl;

import android.opengl.GLES20;
import io.reactivex.annotations.NonNull;
import timber.log.Timber;

/** OpenGL shader utilities */
// TODO remove https://kolibree.atlassian.net/browse/KLTB002-8364
final class ShaderUtils {

  private ShaderUtils() {}

  /**
   * Helper function to compile and link a shader program.
   *
   * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader
   * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader
   * @param attributes Attributes that need to be bound to the program.
   * @return An OpenGL handle to the program.
   */
  static int createAndLinkProgram(
      int vertexShaderHandle, int fragmentShaderHandle, @NonNull String[] attributes) {

    final int programHandle = GLES20.glCreateProgram();

    if (programHandle != 0) {
      // Bind the vertex shader to the program.
      GLES20.glAttachShader(programHandle, vertexShaderHandle);

      // Bind the fragment shader to the program.
      GLES20.glAttachShader(programHandle, fragmentShaderHandle);

      // Bind attributes
      if (attributes != null) {
        final int size = attributes.length;
        for (int i = 0; i < size; i++) {
          GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
        }
      }

      // Link the two shaders together into a program.
      GLES20.glLinkProgram(programHandle);

      // Get the link status.
      final int[] linkStatus = new int[1];
      GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

      // If the link failed, delete the program.
      if (linkStatus[0] == 0) {
        Timber.e("Error compiling program: %s", GLES20.glGetProgramInfoLog(programHandle));
        GLES20.glDeleteProgram(programHandle);
        throw new RuntimeException("Error creating program");
      }
    }

    return programHandle;
  }

  /**
   * Utility method for compiling an OpenGL shader
   *
   * @param type Vertex (GLES20.GL_VERTEX_SHADER) or fragment (GLES20.GL_FRAGMENT_SHADER) type
   * @param shaderCode String containing the shader code
   * @return compiled shader ID
   */
  static int loadShader(int type, @NonNull String shaderCode) {

    // Create a vertex shader type (GLES20.GL_VERTEX_SHADER) or a fragment shader type
    // (GLES20.GL_FRAGMENT_SHADER)
    int shader = GLES20.glCreateShader(type);

    // add the source code to the shader and compile it
    GLES20.glShaderSource(shader, shaderCode);
    GLES20.glCompileShader(shader);

    int[] compiled = new int[1];
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
    if (compiled[0] == 0) {
      GLES20.glDeleteShader(shader);
      Timber.e(
          "Could not compile program: " + GLES20.glGetShaderInfoLog(shader) + " | " + shaderCode);
    }

    return shader;
  }
}
