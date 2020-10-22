package com.kolibree.android.jaws

import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import com.kolibree.android.jaws.opengl.OptimizedVbo
import io.reactivex.Completable
import io.reactivex.functions.Consumer

/**
 * 3D object data memory manager
 */
@Keep
interface MemoryManager {

    /**
     * Preload a 3D model into memory
     *
     * @param model [Kolibree3DModel]
     * @return [Completable]
     */
    fun preloadFromAssets(model: Kolibree3DModel): Completable
}

/** Internal Memory manager that exposes the consumer method */
internal interface MemoryManagerInternal : MemoryManager {

    /**
     * Lock the 3D data to guarantee buffer changes and drawing to be a single atomic operation
     *
     * Please make sure that you only do VBO parameters changing here. It is called on each frame,
     * so doing long operations in the consumer block will drastically reduce the frame rate
     *
     * @param model non null [Kolibree3DModel]
     * @param objectConsumer non null [OptimizedVbo] [Consumer]
     */
    @WorkerThread
    fun lockAndUse(model: Kolibree3DModel, objectConsumer: Consumer<OptimizedVbo>)
}
