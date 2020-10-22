package com.kolibree.android.jaws.utils

import com.kolibree.android.jaws.opengl.OptimizedVbo
import java.util.concurrent.locks.ReentrantLock

/**
 * 3D Model data holder with locking mechanism
 */
internal class ObjectHolder(val modelData: OptimizedVbo) {

    /**
     * Reentrant lock to make sure that two GL threads won't use the buffers at the same time
     */
    val lock = ReentrantLock()
}
