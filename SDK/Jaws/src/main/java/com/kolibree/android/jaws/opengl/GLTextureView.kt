/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.opengl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.AttributeSet
import android.view.TextureView
import androidx.annotation.MainThread
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.jaws.R
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.withLock
import timber.log.Timber

/**
 * This class is a replacement for TextureView (overheating) and GLSurfaceView (very bad when
 * scrolled or swiped)
 */
@VisibleForApp
open class GLTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {

    // This comes from gradle sdk build script
    private val shouldUseGlThread: Boolean = !context.resources.getBoolean(R.bool.disable_glthread)

    private val textureListener = object : SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            terminateGlThreadIfAlive() // Will never happen but in case of

            if (shouldUseGlThread) {
                glThread = GLThread(surface)
            }

            glThread?.start()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            glThread?.onWindowResize()
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            glThread?.finish()
            glThread = null
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            // no-op
        }
    }

    private val renderer = AtomicReference<GLSurfaceView.Renderer?>(null)

    // Only accessed from the main thread
    private var glThread: GLThread? = null

    init {
        surfaceTextureListener = textureListener
    }

    // Workaround for https://stackoverflow.com/questions/43671128/surfacetexture-methods-not-always-called-on-android-7-0
    // At this point the EGL surface has been destroyed
    override fun onDetachedFromWindow() {
        terminateGlThreadIfAlive()
        glThread = null
        super.onDetachedFromWindow()
    }

    fun setRenderer(renderer: GLSurfaceView.Renderer) {
        this.renderer.set(renderer)
    }

    @MainThread
    fun requestRender() {
        FailEarly.failIfNotExecutedOnMainThread()
        glThread?.unlock()
    }

    @MainThread
    fun setRenderMode(renderMode: Int) {
        FailEarly.failIfNotExecutedOnMainThread()
        glThread?.setRenderMode(renderMode)
    }

    private fun terminateGlThreadIfAlive() = glThread?.takeIf(Thread::isAlive)?.apply(GLThread::finish)

    /** Optimized GL renderer [Thread] */
    private inner class GLThread(
        private val surface: SurfaceTexture
    ) : Thread() {

        private val dirtyFlagLock = ReentrantLock()
        private val dirtyFlag = dirtyFlagLock.newCondition()

        // The 3 flags below are atomic since they are updated from the main screen and read from
        // the EGL one
        private val renderMode = AtomicInteger(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
        private val finished = AtomicBoolean(false)
        private val sizeChanged = AtomicBoolean(true)

        private lateinit var egl: EGL10
        private lateinit var eglDisplay: EGLDisplay
        private lateinit var eglConfig: EGLConfig
        private lateinit var eglContext: EGLContext
        private lateinit var gl: GL10
        private var eglSurface: EGLSurface? = null

        override fun run() {
            initGL()
            renderer.get()?.onSurfaceCreated(gl, eglConfig)

            while (!finished.get()) {
                if (renderMode.get() == GLSurfaceView.RENDERMODE_WHEN_DIRTY) {
                    dirtyFlagLock.withLock {
                        checkCurrentContextAndDrawFrame(gl)
                        dirtyFlag.await()

                        if (finished.get()) {
                            finishGL()
                            return
                        }
                    }
                }

                checkCurrentContextAndDrawFrame(gl)
            }

            finishGL()
        }

        private fun checkCurrentContextAndDrawFrame(gl10: GL10) {
            checkCurrent()

            renderer.get()?.apply {
                if (sizeChanged.getAndSet(false)) {
                    createSurface()
                    onSurfaceChanged(gl10, width, height)
                }
                onDrawFrame(gl10)
            }

            if (!egl.eglSwapBuffers(eglDisplay, eglSurface)) {
                error("Could not swap buffers")
            }
        }

        fun unlock() = dirtyFlagLock.withLock {
            dirtyFlag.signal()
        }

        fun setRenderMode(renderMode: Int) {
            this.renderMode.set(renderMode)
            unlock()
        }

        private fun destroySurface() {
            if (eglSurface != null && eglSurface !== EGL10.EGL_NO_SURFACE) {
                egl.apply {
                    eglMakeCurrent(
                        eglDisplay,
                        EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT
                    )
                    eglDestroySurface(eglDisplay, eglSurface)
                }

                eglSurface = null
            }
        }

        private fun createSurface(): Boolean {
            destroySurface()

            try {
                eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surface, null)
            } catch (e: IllegalArgumentException) { // Invalid or already torn down surface
                Timber.e(e)
                return false
            }

            if (eglSurface == null || eglSurface === EGL10.EGL_NO_SURFACE) {
                val error = egl.eglGetError()

                if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                    Timber.e("createWindowSurface returned EGL_BAD_NATIVE_WINDOW.")
                }

                return false
            }

            return egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        }

        private fun checkCurrent() {
            if (surfaceHasErrors(egl)) {
                checkEglError()

                if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                    throwEglError("eglMakeCurrent failed")
                }

                checkEglError()
            }
        }

        private fun surfaceHasErrors(gl: EGL10) =
            eglContext != gl.eglGetCurrentContext() ||
                eglSurface != gl.eglGetCurrentSurface(EGL10.EGL_DRAW)

        private fun checkEglError() = egl.eglGetError().let { error ->
            if (error != EGL10.EGL_SUCCESS) {
                Timber.e("EGL error = 0x%s", Integer.toHexString(error))
            }
        }

        private fun finishGL() = egl.apply {
            eglDestroyContext(eglDisplay, eglContext)
            eglTerminate(eglDisplay)
            eglDestroySurface(eglDisplay, eglSurface)
        }

        private fun initGL() {
            egl = EGLContext.getEGL() as EGL10
            eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

            if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
                throwEglError("eglGetDisplay failed")
            }

            val version = IntArray(2)
            if (!egl.eglInitialize(eglDisplay, version)) {
                throwEglError("eglInitialize failed")
            }

            eglConfig = createEglConfig() ?: error("eglConfig not initialized")
            eglContext = createContext(egl, eglDisplay, eglConfig)

            if (!createSurface()) {
                throwEglError("Could not create surface. See errors above")
            }

            gl = eglContext.gl as GL10
        }

        @Suppress("TooGenericExceptionThrown")
        private fun throwEglError(message: String) {
            error("$message  ${GLUtils.getEGLErrorString(egl.eglGetError())}")
        }

        private fun createContext(egl: EGL10, eglDisplay: EGLDisplay, eglConfig: EGLConfig) =
            egl.eglCreateContext(
                eglDisplay,
                eglConfig,
                EGL10.EGL_NO_CONTEXT,
                intArrayOf(
                    EGL_CONTEXT_CLIENT_VERSION,
                    EGL_VERSION,
                    EGL10.EGL_NONE
                )
            )

        private fun createEglConfig(): EGLConfig? {
            val configsCount = IntArray(1)
            val configs = arrayOfNulls<EGLConfig>(1)
            val configSpec = optimizedConfigSpec

            egl.let { gl ->
                require(gl.eglChooseConfig(eglDisplay, configSpec, configs, 1, configsCount)) {
                    "eglChooseConfig failed " + GLUtils.getEGLErrorString(gl.eglGetError())
                }
            }

            return if (configsCount[0] > 0) configs[0] else null
        }

        fun finish() {
            finished.set(true)
            unlock()
        }

        fun onWindowResize() {
            sizeChanged.set(true)
        }
    }

    private companion object {

        private const val EGL_VERSION = 2

        private const val EGL_CONTEXT_CLIENT_VERSION = 0x3098

        private const val EGL_OPENGL_ES2_BIT = 4

        private const val COMPONENT_SIZE = 8

        private const val DEPTH_SIZE = 16

        private const val STENCIL_SIZE = 0

        private val optimizedConfigSpec = intArrayOf(
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_RED_SIZE, COMPONENT_SIZE,
            EGL10.EGL_GREEN_SIZE, COMPONENT_SIZE,
            EGL10.EGL_BLUE_SIZE, COMPONENT_SIZE,
            EGL10.EGL_ALPHA_SIZE, COMPONENT_SIZE,
            EGL10.EGL_DEPTH_SIZE, DEPTH_SIZE,
            EGL10.EGL_STENCIL_SIZE, STENCIL_SIZE,
            EGL10.EGL_NONE
        )
    }
}
