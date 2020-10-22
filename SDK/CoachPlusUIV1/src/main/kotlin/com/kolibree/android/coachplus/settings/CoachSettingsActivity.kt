/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import androidx.lifecycle.ViewModelProviders
import com.kolibree.android.app.ui.activity.BaseHomeAsUpActivity
import com.kolibree.android.coachplus.R
import com.kolibree.android.coachplus.settings.CoachSettingsViewState.CoachSettingsState
import com.kolibree.android.coachplus.sounds.CoachSoundsSettingsActivity
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.translationssupport.TranslationContext
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_coach_settings.*
import timber.log.Timber

@Keep
class CoachSettingsActivity : BaseHomeAsUpActivity() {

    private val disposables = CompositeDisposable()

    @Inject
    internal
    lateinit var viewModelFactory: CoachSettingsViewModel.Factory

    internal lateinit var viewModel: CoachSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coach_settings)

        showToolbarUpButton(toolbar)
        initListeners()
        initViewModel()
    }

    public override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(CoachSettingsViewModel::class.java)
        lifecycle.addObserver(viewModel)
        subscribeViewModel()
    }

    private fun subscribeViewModel() {
        disposables +=
            viewModel
                .viewStateObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::render, Throwable::printStackTrace)
    }

    private fun render(settingsViewState: CoachSettingsViewState) {
        toggleDisplayBrushingMovement(settingsViewState.enableBrushingMovement)
        toggleDisplayHelpText(settingsViewState.enableHelpText)
        when (settingsViewState.actions) {
            CoachSettingsState.ACTION_OPEN_SOUND_PAGE -> openSoundPage()
            else -> Timber.d("Argument not available")
        }
    }

    private fun toggleDisplayBrushingMovement(enable: Boolean) {
        coachSettingsDisplayBrushingMvnt.isChecked = enable
    }

    private fun toggleDisplayHelpText(enable: Boolean) {
        coachSettingsDisplayHelpTxt.isChecked = enable
    }

    private fun openSoundPage() {
        startActivity(CoachSoundsSettingsActivity.createIntent(this, readMacFromIntent()))
    }

    private fun initListeners() {
        coachSettingsDisplayBrushingMvnt.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onDisplayBrushingMovementChecked(
                isChecked
            )
        }
        coachSettingsDisplayHelpTxt.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onDisplayHelpTextChecked(
                isChecked
            )
        }
        llSounds.setOnClickListener { viewModel.openSoundPage() }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(TranslationContext(newBase))
    }

    companion object {
        @JvmStatic
        fun createIntent(context: Context) = Intent(context, CoachSettingsActivity::class.java)

        @JvmStatic
        fun createBrushingSessionIntent(context: Context, toothbrushMac: String): Intent {
            return createIntent(context).apply {
                putExtra(INTENT_TOOTHBRUSH_MAC, toothbrushMac)
            }
        }
    }
}
