/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.mvi

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.coachplus.logic.R
import com.kolibree.android.interactor.LifecycleAwareInteractor
import java.io.IOException
import javax.inject.Inject
import timber.log.Timber

@Keep
abstract class SoundInteractor : LifecycleAwareInteractor<SoundInteractor>() {
    abstract fun prepare(musicUri: Uri)
    abstract fun playTransitionSound()
    abstract fun playAmbientSound()
    abstract fun pauseAmbientSound()
    abstract fun replayAmbientSound()
}

internal class SoundInteractorImpl @Inject constructor(context: Context) :
    SoundInteractor() {

    private val appContext = context.applicationContext
    @VisibleForTesting
    var ambientSound: MediaPlayer? = null
    @VisibleForTesting
    var transitionSound: MediaPlayer? = null
    @VisibleForTesting
    var uri: Uri? = null

    override fun onCreateInternal(savedInstanceState: Bundle?) {
        super.onCreateInternal(savedInstanceState)
        transitionSound = MediaPlayer.create(appContext, R.raw.bouton_50)
    }

    override fun onDestroyInternal() {
        ambientSound?.release()
        transitionSound?.release()
        super.onDestroyInternal()
    }

    override fun prepare(musicUri: Uri) {
        if (Uri.EMPTY != musicUri) {
            if (ambientSound != null) {
                resetAmbientSound(musicUri)
            } else {
                uri = musicUri
                ambientSound = MediaPlayer.create(appContext, musicUri)
            }
        }
    }

    @VisibleForTesting
    fun resetAmbientSound(musicUri: Uri) {
        ambientSound?.let { ambientSound ->
            if (uri != musicUri) {
                ambientSound.reset()

                try {
                    ambientSound.setDataSource(appContext, musicUri)
                    ambientSound.prepare()
                    uri = musicUri
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }
        }
    }

    override fun playTransitionSound() {
        transitionSound?.start()
    }

    override fun playAmbientSound() {
        ambientSound?.start()
    }

    override fun pauseAmbientSound() {
        ambientSound?.apply {
            if (isPlaying) {
                pause()
            }
        }
    }

    override fun replayAmbientSound() {
        ambientSound?.seekTo(0)
    }
}
