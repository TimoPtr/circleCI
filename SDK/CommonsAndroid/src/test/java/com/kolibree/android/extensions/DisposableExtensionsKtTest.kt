package com.kolibree.android.extensions

import com.kolibree.android.failearly.FailEarly
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import timber.log.Timber

class DisposableExtensionsKtTest {

    @Before
    fun setup() {
        FailEarly.overrideDelegateWith { error, _ ->
            Timber.w(error, "FAILEARLY KICKED IN, no action as this is no-op delegate")
        }
    }

    /*
    FORCE DISPOSE
     */
    @Test
    fun `forceDispose does not crash if instance is Null`() {
        val nullDisposable: Disposable? = null

        nullDisposable.forceDispose()
    }

    @Test
    fun `forceDispose does not invoke dispose if disposable already disposed`() {
        val disposable: Disposable? = mock()
        whenever(disposable!!.isDisposed).thenReturn(true)

        disposable.forceDispose()

        verify(disposable, never()).dispose()
    }

    @Test
    fun `forceDispose invokes dispose if not disposed`() {
        val disposable: Disposable? = mock()
        whenever(disposable!!.isDisposed).thenReturn(false)

        disposable.forceDispose()

        verify(disposable).dispose()
    }

    /*
    ADD SAFELY
     */
    @Test
    fun `add safely does not add a null disposable`() {
        val nullDisposable: Disposable? = null

        val composite = CompositeDisposable()
        composite.addSafely(nullDisposable)

        assertEquals(0, composite.size())
    }

    @Test
    @Ignore("Check if we really want to keep this rule")
    fun `add safely does not add a disposed disposable`() {
        val disposable: Disposable? = mock()
        whenever(disposable!!.isDisposed).thenReturn(true)

        val composite = CompositeDisposable()
        composite.addSafely(disposable)

        assertEquals(0, composite.size())
    }

    @Test
    fun `add safely does not add to a disposed composite`() {
        val disposable: Disposable? = mock()
        whenever(disposable!!.isDisposed).thenReturn(false)

        val composite = CompositeDisposable()
        composite.dispose()

        composite.addSafely(disposable)

        assertEquals(0, composite.size())
    }

    @Test
    fun `add safely adds a disposable`() {
        val disposable: Disposable? = mock()
        whenever(disposable!!.isDisposed).thenReturn(false)

        val composite = CompositeDisposable()

        composite.addSafely(disposable)

        assertEquals(1, composite.size())
    }
}
