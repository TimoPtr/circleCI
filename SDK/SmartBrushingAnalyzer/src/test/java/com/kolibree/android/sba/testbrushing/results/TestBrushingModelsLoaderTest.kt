package com.kolibree.android.sba.testbrushing.results

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.jaws.Kolibree3DModel.LOWER_JAW
import com.kolibree.android.jaws.Kolibree3DModel.UPPER_JAW
import com.kolibree.android.jaws.MemoryManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Test

class TestBrushingModelsLoaderTest : BaseUnitTest() {

    private val memoryManager = mock<MemoryManager>()

    private lateinit var modelsLoader: TestBrushingModelsLoader

    override fun setup() {
        super.setup()

        modelsLoader = spy(TestBrushingModelsLoader(memoryManager))
    }

    @Test
    fun `onCreate preload UPPER_JAW model`() {
        doReturn(Completable.complete()).whenever(memoryManager).preloadFromAssets(any())

        modelsLoader.onCreate(mock())

        verify(memoryManager).preloadFromAssets(UPPER_JAW)
    }

    @Test
    fun `onCreate preload LOWER_JAW model`() {
        doReturn(Completable.complete()).whenever(memoryManager).preloadFromAssets(any())

        modelsLoader.onCreate(mock())

        verify(memoryManager).preloadFromAssets(LOWER_JAW)
    }

    @Test
    fun `onDestroy invokes forceDispose() on Disposable object`() {
        modelsLoader.disposable = mock {
            whenever(it.isDisposed).thenReturn(false)
            doNothing().whenever(it).dispose()
        }

        modelsLoader.onDestroy(mock())

        verify(modelsLoader.disposable)!!.dispose()
    }
}
