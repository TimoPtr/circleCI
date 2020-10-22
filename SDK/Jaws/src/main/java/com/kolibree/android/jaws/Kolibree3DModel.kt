package com.kolibree.android.jaws

import androidx.annotation.Keep

/**
 * Kolibree 3D models definition
 *
 * The precomputed values of the vertex, face, normals counts etc... let us skip this step during
 * loading (These values are required for memory allocations)
 */
@Keep
@SuppressWarnings("MagicNumber")
enum class Kolibree3DModel(
    val scalingFactor: Float,
    val vertexShaderCode: String
) {

    TOOTHBRUSH(
        scalingFactor = 5.3f,
        vertexShaderCode = "uniform mat4 u_MVPMatrix;\n" +
            "attribute vec4 a_Position;\n" +
            "uniform mat4 u_MVMatrix;\n" +
            "vec3 u_LightPos[4];\n" +
            "attribute vec4 a_Color;\n" +
            "attribute vec3 a_Normal;\n" +
            "varying vec4 v_Color;\n" +
            "void main() {\n" +
            "  u_LightPos[0] = vec3(-8.0, -6.0, 10.0);\n" +
            "  u_LightPos[1] = vec3(-8.0, 6.0, 10.0);\n" +
            "  u_LightPos[2] = vec3(8.0, -6.0, 10.0);\n" +
            "  u_LightPos[3] = vec3(8.0, 6.0, 10.0);\n" +
            "  float lightSum = 0.0;\n" +
            "  vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n" +
            "  vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n" +
            "  for(int i=0; i<4; i++) {\n" +
            "    vec3 lightVector = normalize(u_LightPos[i] - modelViewVertex);\n" +
            "    float diff = max(dot(modelViewNormal, lightVector), 0.1);\n" +
            "    float distance = length(u_LightPos[i] - modelViewVertex);\n" +
            "    lightSum += diff * (6.0 / (1.0 + (0.05 * distance * distance)));\n" +
            "  }\n" +
            "  lightSum += 0.7;\n" + // Additional ambient light
            "  v_Color = a_Color * min(lightSum, 1.0);\n" +
            "  v_Color[3] = a_Color[3];\n" +
            "  gl_Position = u_MVPMatrix * a_Position;\n" +
            "  gl_PointSize = 2.5;\n" +
            "}"
    ),

    PLAQLESS(
        scalingFactor = 5.3f,
        vertexShaderCode = "uniform mat4 u_MVPMatrix;\n" +
            "attribute vec4 a_Position;\n" +
            "uniform mat4 u_MVMatrix;\n" +
            "vec3 u_LightPos[4];\n" +
            "attribute vec4 a_Color;\n" +
            "attribute vec3 a_Normal;\n" +
            "varying vec4 v_Color;\n" +
            "void main() {\n" +
            "  u_LightPos[0] = vec3(-8.0, -6.0, 10.0);\n" +
            "  u_LightPos[1] = vec3(-8.0, 6.0, 10.0);\n" +
            "  u_LightPos[2] = vec3(8.0, -6.0, 10.0);\n" +
            "  u_LightPos[3] = vec3(8.0, 6.0, 10.0);\n" +
            "  float lightSum = 0.0;\n" +
            "  vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n" +
            "  vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n" +
            "  for(int i=0; i<4; i++) {\n" +
            "    vec3 lightVector = normalize(u_LightPos[i] - modelViewVertex);\n" +
            "    float diff = max(dot(modelViewNormal, lightVector), 0.1);\n" +
            "    float distance = length(u_LightPos[i] - modelViewVertex);\n" +
            "    lightSum += diff * (6.0 / (1.0 + (0.05 * distance * distance)));\n" +
            "  }\n" +
            "  lightSum += 0.7;\n" + // Additional ambient light
            "  v_Color = a_Color * min(lightSum, 1.0);\n" +
            "  v_Color[3] = a_Color[3];\n" +
            "  gl_Position = u_MVPMatrix * a_Position;\n" +
            "  gl_PointSize = 2.5;\n" +
            "}"
    ),

    UPPER_JAW(
        scalingFactor = 1.3f,
        vertexShaderCode = "uniform mat4 u_MVPMatrix;\n" +
            "attribute vec4 a_Position;\n" +
            "uniform mat4 u_MVMatrix;\n" +
            "vec3 u_LightPos[1];\n" +
            "attribute vec4 a_Color;\n" +
            "attribute vec3 a_Normal;\n" +
            "varying vec4 v_Color;\n" +
            "void main() {\n" +
            "  u_LightPos[0] = vec3(0.0, 0.0, 100.0);\n" +
            "  float lightSum = 0.0;\n" +
            "  vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n" +
            "  vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n" +
            "  vec3 lightVector = normalize(u_LightPos[0] - modelViewVertex);\n" +
            "  float diff = max(dot(modelViewNormal, lightVector), 0.1);\n" +
            "  float distance = length(u_LightPos[0] - modelViewVertex);\n" +
            "  lightSum += diff * (400.0 / (1.0 + (0.033 * distance * distance)));\n" +
            "  lightSum += 0.47;\n" + // Additional ambient light

            "  v_Color = a_Color * min(lightSum, 0.97);\n" +
            "  v_Color[3] = a_Color[3];\n" +
            "  gl_Position = u_MVPMatrix * a_Position;\n" +
            "  gl_PointSize = 2.5;\n" +
            "}"
    ),

    LOWER_JAW(
        scalingFactor = 1.0f,
        vertexShaderCode = "uniform mat4 u_MVPMatrix;\n" +
            "attribute vec4 a_Position;\n" +
            "uniform mat4 u_MVMatrix;\n" +
            "vec3 u_LightPos[1];\n" +
            "attribute vec4 a_Color;\n" +
            "attribute vec3 a_Normal;\n" +
            "varying vec4 v_Color;\n" +
            "void main() {\n" +
            "  u_LightPos[0] = vec3(0.0, 0.0, 100.0);\n" +
            "  float lightSum = 0.0;\n" +
            "  vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n" +
            "  vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n" +
            "  vec3 lightVector = normalize(u_LightPos[0] - modelViewVertex);\n" +
            "  float diff = max(dot(modelViewNormal, lightVector), 0.1);\n" +
            "  float distance = length(u_LightPos[0] - modelViewVertex);\n" +
            "  lightSum += diff * (500.0 / (1.0 + (0.03 * distance * distance)));\n" +
            "  lightSum += 0.47;\n" + // Additional ambient light

            "  v_Color = a_Color * min(lightSum, 0.97);\n" +
            "  v_Color[3] = a_Color[3];\n" +
            "  gl_Position = u_MVPMatrix * a_Position;\n" +
            "  gl_PointSize = 2.5;\n" +
            "}"
    ),

    HUM_LOWER_JAW(
        scalingFactor = 1.3f,
        vertexShaderCode = """
uniform mat4 u_MVPMatrix;
attribute vec4 a_Position;
uniform mat4 u_MVMatrix;
uniform vec3 u_LightPos;
attribute vec4 a_Color;
attribute vec3 a_Normal;
varying vec4 v_Color;
void main() {
    vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);
    vec3 lightVector = normalize(u_LightPos - modelViewVertex);
    vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
    float diffuse = max(dot(modelViewNormal, lightVector), 0.1);
    float distance = length(u_LightPos - modelViewVertex);
    diffuse = diffuse * (1.0 / (1.3 + (0.04 * distance * distance)));
    diffuse = diffuse + 0.55;
    v_Color = a_Color * diffuse;
    v_Color[3] = a_Color[3];
    gl_Position = u_MVPMatrix * a_Position;
    gl_PointSize = 2.5;  
}
"""
    ),

    HUM_UPPER_JAW(
        scalingFactor = 1.3f,
        vertexShaderCode = """
uniform mat4 u_MVPMatrix;
attribute vec4 a_Position;
uniform mat4 u_MVMatrix;
uniform vec3 u_LightPos;
attribute vec4 a_Color;
attribute vec3 a_Normal;
varying vec4 v_Color;
void main() {
    vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);
    vec3 lightVector = normalize(u_LightPos - modelViewVertex);
    vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
    float diffuse = max(dot(modelViewNormal, lightVector), 0.1);
    float distance = length(u_LightPos - modelViewVertex);
    diffuse = diffuse * (1.0 / (1.3 + (0.04 * distance * distance)));
    diffuse = diffuse + 0.55;
    v_Color = a_Color * diffuse;
    v_Color[3] = a_Color[3];
    gl_Position = u_MVPMatrix * a_Position;
    gl_PointSize = 2.5;  
}
"""
    )
}
