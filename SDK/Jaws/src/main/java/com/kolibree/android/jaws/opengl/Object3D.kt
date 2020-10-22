package com.kolibree.android.jaws.opengl

// TODO remove https://kolibree.atlassian.net/browse/KLTB002-8364
internal interface Object3D {

    fun draw(vbo: OptimizedVbo, pMatrix: FloatArray, vMatrix: FloatArray)

    companion object {

        // number of coordinates per vertex in this array
        const val COORDS_PER_VERTEX = 3
        const val VERTEX_STRIDE = COORDS_PER_VERTEX * 4
    }
}
