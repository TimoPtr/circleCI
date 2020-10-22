/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.coachplus.mvi.SoundInteractorImpl
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SoundInteractorTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun onCreateInternal_init_transitionSound() {
        val interactor = SoundInteractorImpl(context())

        assertNull(interactor.transitionSound)

        interactor.onCreateInternal(null)

        assertNotNull(interactor.transitionSound)
    }

    @Test
    fun onDestroyInternal_call_release_on_ambientSound_and_transitionSound() {
        val interactor = SoundInteractorImpl(context())
        val mp1 = StubMediaPlayer()
        val mp2 = StubMediaPlayer()

        interactor.transitionSound = mp1
        interactor.ambientSound = mp2

        interactor.onDestroyInternal()

        assertTrue(mp1.isReleaseCalled)
        assertTrue(mp2.isReleaseCalled)
    }

    @Test
    fun prepare_with_UriEmpty_do_nothing() {
        val interactor = SoundInteractorImpl(context())
        interactor.prepare(Uri.EMPTY)
        assertNull(interactor.uri)
        assertNull(interactor.ambientSound)
    }

    @Test
    fun prepare_with_uri_instantiate_ambient_sound() {
        val interactor = SoundInteractorImpl(context())
        val uri = Uri.parse("android.resource://" + context().packageName + "/raw/bouton_50")
        interactor.prepare(uri)

        assertEquals(uri, interactor.uri)
        assertNotNull(interactor.ambientSound)
    }

    @Test
    fun prepare_with_uri_and_ambient_already_set_reset_ambient_and_set_new_uri() {
        val interactor = SoundInteractorImpl(context())
        val uri = Uri.parse("android.resource://" + context().packageName + "/raw/bouton_50")
        val mp1 = StubMediaPlayer()
        interactor.ambientSound = mp1

        interactor.prepare(uri)

        assertTrue(mp1.isResetCalled)

        assertEquals(uri, interactor.uri)
        assertNotNull(interactor.ambientSound)
    }

    @Test
    fun resetAmbientSound_reset_previous_ambient_mp() {
        val interactor = SoundInteractorImpl(context())
        val uri = Uri.parse("android.resource://" + context().packageName + "/raw/bouton_50")
        val mp1 = StubMediaPlayer()
        interactor.ambientSound = mp1

        interactor.resetAmbientSound(uri)

        assertTrue(mp1.isResetCalled)

        assertEquals(uri, interactor.uri)
        assertNotNull(interactor.ambientSound)
    }

    @Test
    fun playTransitionSound_invokes_transition_sound_start() {
        val interactor = SoundInteractorImpl(context())
        val stub = StubMediaPlayer()
        interactor.transitionSound = stub

        interactor.playTransitionSound()

        assertTrue(stub.isStartCalled)
    }

    @Test
    fun playAmbientSound_invokes_ambient_sound_start() {
        val interactor = SoundInteractorImpl(context())
        val stub = StubMediaPlayer()
        interactor.ambientSound = stub

        interactor.playAmbientSound()

        assertTrue(stub.isStartCalled)
    }

    @Test
    fun pauseAmbientSound_does_not_invoke_ambient_sound_pause_while_not_playing() {
        val interactor = SoundInteractorImpl(context())
        val stub = StubMediaPlayer()
        interactor.ambientSound = stub

        interactor.pauseAmbientSound()

        assertFalse(stub.isPauseCalled)
    }

    @Test
    fun replayAmbientSound_invokes_ambient_sound_seek_to_0() {
        val interactor = SoundInteractorImpl(context())
        val stub = StubMediaPlayer()
        interactor.ambientSound = stub

        interactor.replayAmbientSound()

        assertTrue(stub.isSeekToCalled)
    }

    class StubMediaPlayer : MediaPlayer() {
        var isReleaseCalled = false
            private set

        var isResetCalled = false
            private set

        var isStartCalled = false
            private set

        var isPauseCalled = false
            private set

        var isSeekToCalled = false
            private set

        override fun start() {
            super.start()
            isStartCalled = true
        }

        override fun pause() {
            super.pause()
            isPauseCalled = true
        }

        override fun seekTo(msec: Int) {
            super.seekTo(msec)
            isSeekToCalled = true
        }

        override fun release() {
            isReleaseCalled = true
            super.release()
        }

        override fun reset() {
            isResetCalled = true
            super.reset()
        }
    }
}
