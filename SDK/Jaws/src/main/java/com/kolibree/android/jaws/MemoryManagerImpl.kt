/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.jaws

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import com.kolibree.android.jaws.Kolibree3DModel.HUM_LOWER_JAW
import com.kolibree.android.jaws.Kolibree3DModel.HUM_UPPER_JAW
import com.kolibree.android.jaws.Kolibree3DModel.LOWER_JAW
import com.kolibree.android.jaws.Kolibree3DModel.PLAQLESS
import com.kolibree.android.jaws.Kolibree3DModel.TOOTHBRUSH
import com.kolibree.android.jaws.Kolibree3DModel.UPPER_JAW
import com.kolibree.android.jaws.models.DefaultBrushHeadVbo
import com.kolibree.android.jaws.models.HumLowerJawVbo
import com.kolibree.android.jaws.models.HumUpperJawVbo
import com.kolibree.android.jaws.models.LowerJawVbo
import com.kolibree.android.jaws.models.PlaqlessBrushHeadVbo
import com.kolibree.android.jaws.models.UpperJawVbo
import com.kolibree.android.jaws.opengl.BaseOptimizedVbo
import com.kolibree.android.jaws.opengl.OptimizedVbo
import com.kolibree.android.jaws.utils.ObjectHolder
import io.reactivex.Completable
import io.reactivex.functions.Consumer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.HashMap
import java.util.Locale
import javax.inject.Inject
import timber.log.Timber

/**
 * This class is responsible for keeping 3D model objects loaded in memory It also offers a locking
 * mechanism for these objects, making them reusable across several GL contexts
 */
@AnyThread
internal class MemoryManagerImpl @Inject constructor(context: Context) : MemoryManagerInternal {

    /** 3D objects cache  */
    private val objectHolders = HashMap<Kolibree3DModel, ObjectHolder>()

    private val appContext = context.applicationContext

    override fun preloadFromAssets(model: Kolibree3DModel): Completable =
        Completable.fromAction { safeGetHolder(model) }

    /*
    There are several GL thread that are continuously rendering the same VBO (which owns the
    transformations vectors, position, rotation... And the buffers, vertex and their normal vectors)
    This lock ensures that if several views are rendering the same VBO at the same time
    (like in the checkup's view pager), there won't be two of them mixing colors or transformations.
    This methods opens a window for a GL thread (a renderer instance) to apply transformations and
    coloring, then drawing it.
     */
    @Suppress("TooGenericExceptionCaught")
    @WorkerThread
    override fun lockAndUse(model: Kolibree3DModel, objectConsumer: Consumer<OptimizedVbo>) {
        val holder = safeGetHolder(model)

        holder.lock.lock()

        try {
            objectConsumer.accept(holder.modelData)
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            holder.lock.unlock()
        }
    }

    /**
     * This method ensures that we will never load the model twice at the same time
     *
     *
     * It will block the GL***Views' thread so they will wait until the drawing is possible
     *
     * @param model non null [Kolibree3DModel]
     * @return non null [ObjectHolder]
     */
    @Synchronized
    private fun safeGetHolder(model: Kolibree3DModel): ObjectHolder {
        var holder = objectHolders[model]

        if (holder != null) {
            return holder
        }

        val vbo = loadFromAssets(model)

        holder = ObjectHolder(vbo)
        objectHolders[model] = holder
        return holder
    }

    private fun createVbo(
        model: Kolibree3DModel,
        vertexBuffer: FloatBuffer,
        normalBuffer: FloatBuffer
    ) = when (model) {
        UPPER_JAW -> UpperJawVbo(vertexBuffer, normalBuffer)
        LOWER_JAW -> LowerJawVbo(vertexBuffer, normalBuffer)
        HUM_LOWER_JAW -> HumLowerJawVbo(vertexBuffer, normalBuffer)
        HUM_UPPER_JAW -> HumUpperJawVbo(vertexBuffer, normalBuffer)
        TOOTHBRUSH -> DefaultBrushHeadVbo(vertexBuffer, normalBuffer)
        PLAQLESS -> PlaqlessBrushHeadVbo(vertexBuffer, normalBuffer)
    }

    private fun loadFromAssets(model: Kolibree3DModel): BaseOptimizedVbo<out Any> {
        val fileName = model.name.toLowerCase(Locale.getDefault())
        val vbo = createVbo(
            model,
            loadFloatBufferFromAssets("$fileName.$VERTEX_BUFFER_DUMP_FILE_EXT"),
            loadFloatBufferFromAssets("$fileName.$NORMAL_BUFFER_DUMP_FILE_EXT")
        )
        preScale(model, vbo)
        return vbo
    }

    private fun loadFloatBufferFromAssets(fileName: String): FloatBuffer =
        appContext
            .assets
            .open(fileName)
            .readBytes()
            .let {
                val buffer = ByteBuffer
                    .allocateDirect(it.size)
                    .order(ByteOrder.nativeOrder())
                    .put(it)
                buffer.position(0)
                return@let buffer.asFloatBuffer()
            }

    private fun preScale(model: Kolibree3DModel, vbo: OptimizedVbo) =
        vbo.scaleVector.set(
            model.scalingFactor,
            model.scalingFactor,
            model.scalingFactor
        )

    companion object {

        private const val VERTEX_BUFFER_DUMP_FILE_EXT = "kl3dv"

        private const val NORMAL_BUFFER_DUMP_FILE_EXT = "kl3dn"
    }
}
