/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import android.os.Handler
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.widget.ShadeView
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.test.utils.failearly.executeRunnablesImmediately
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class SmilesCounterVisibilityUseCaseTest : BaseUnitTest() {
    private val handler: Handler = mock()

    private val lifecycle: Lifecycle = mock()

    private val useCase = SmilesCounterVisibilityUseCase(lifecycle = lifecycle, handler = handler)

    override fun setup() {
        super.setup()

        handler.executeRunnablesImmediately()
    }

    @Test
    fun `useCase registers itself as lifecycle observer`() {
        verify(lifecycle).addObserver(useCase)
    }

    @Test
    fun `onceAndStream initial value is false`() {
        useCase.onceAndStream.test().assertValue(false)
    }

    /*
    setView
     */

    @Test
    fun `when setView is invoked, useCase registers itself as observer`() {
        val view: ShadeView = mock()
        useCase.setView(view)

        verify(view).setOnExpandedListener(useCase)
    }

    /*
    onResume
     */

    @Test
    fun `when lifecycle is resumed and onFullyExpanded was invoked, useCase emits true`() {
        val observer = useCase.onceAndStream.test()

        useCase.onFullyExpanded()

        useCase.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        observer.assertLastValue(true)
    }

    @Test
    fun `when lifecycle is resumed and onNotFullyExpanded was invoked, useCase emits true`() {
        val observer = useCase.onceAndStream.test()

        useCase.onNotFullyExpanded()

        useCase.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        observer.assertLastValue(false)
    }

    @Test
    fun `when lifecycle is resumed and onFullyExpanded is invoked, useCase emits true`() {
        val observer = useCase.onceAndStream.test()

        useCase.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        observer.assertLastValue(false)

        useCase.onFullyExpanded()

        observer.assertLastValue(true)
    }

    @Test
    fun `when lifecycle is resumed and onNotFullyExpanded is invoked, useCase emits true`() {
        val observer = useCase.onceAndStream.test()

        useCase.onFullyExpanded()

        useCase.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        observer.assertLastValue(true)

        useCase.onNotFullyExpanded()

        observer.assertLastValue(false)
    }

    /*
    onPause
     */

    @Test
    fun `when lifecycle is paused and onFullyExpanded was invoked, useCase emits false`() {
        val observer = useCase.onceAndStream.test()

        useCase.onFullyExpanded()

        useCase.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        observer.assertLastValue(true)

        useCase.pushLifecycleTo(Lifecycle.Event.ON_PAUSE)

        observer.assertLastValue(false)
    }

    @Test
    fun `when lifecycle is anything different than ON_RESUME, any combination of collapsed events don't emit new values`() {
        val observer = useCase.onceAndStream.test()
        Lifecycle.Event.values()
            .filterNot { it == Lifecycle.Event.ON_RESUME }
            .forEach { state ->
                useCase.pushLifecycleTo(state)

                observer.assertLastValue(false)

                val nbOfValues = observer.valueCount()

                useCase.onFullyExpanded()
                useCase.onNotFullyExpanded()
                useCase.onNotFullyExpanded()
                useCase.onNotFullyExpanded()
                useCase.onFullyExpanded()
                useCase.onFullyExpanded()

                observer.assertValueCount(nbOfValues)
            }
    }
}
