package com.kolibree.android.coachplus.sounds

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.kolibree.android.app.ui.activity.BaseHomeAsUpActivity
import com.kolibree.android.coachplus.R
import com.kolibree.android.coachplus.sounds.CoachSoundsViewState.CoachSoundSettingsState
import com.kolibree.android.coachplus.utils.MusicHintProvider
import com.kolibree.android.coachplus.utils.MusicUtils
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.translationssupport.TranslationContext
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_coach_sounds_settings.*
import timber.log.Timber

internal class CoachSoundsSettingsActivity : BaseHomeAsUpActivity() {

    private val disposables = CompositeDisposable()

    @Inject
    internal
    lateinit var viewModelFactory: CoachSoundsSettingsViewModel.Factory

    @Inject
    internal
    lateinit var musicHintProvider: MusicHintProvider

    internal lateinit var viewModel: CoachSoundsSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coach_sounds_settings)

        showToolbarUpButton(toolbar)
        initViewModel()
        initListeners()
        subscribeViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(CoachSoundsSettingsViewModel::class.java)
        lifecycle.addObserver(viewModel)
    }

    private fun subscribeViewModel() {
        disposables.addSafely(
            viewModel
                .viewStateObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::render, Throwable::printStackTrace)
        )
    }

    private fun render(settingsViewState: CoachSoundsViewState) {
        enableMusic(settingsViewState.enableMusic)
        enableShuffle(settingsViewState.enableShuffle)
        enableTransitionSounds(settingsViewState.enableTransitionSounds)
        musicFile.text = MusicUtils.getCoachMusicFileInfo(
            this,
            musicHintProvider,
            settingsViewState.musicURI
        )
        when (settingsViewState.actions) {
            CoachSoundSettingsState.ACTION_CHOOSE_MUSIC -> chooseMusic()
            else -> Timber.d("Argument not available")
        }
    }

    private fun enableMusic(enable: Boolean) {
        coachSoundsSettingsEnableMusic.isChecked = enable
    }

    private fun enableShuffle(enable: Boolean) {
        coachSoundsSettingsEnableShuffle.isChecked = enable
    }

    private fun enableTransitionSounds(enable: Boolean) {
        coachSoundsSettingsEnableTransitionSounds.isChecked = enable
    }

    private fun chooseMusic() {
        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT,
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            )
        intent.type = "audio/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        startActivityForResult(intent, MUSIC_REQUEST_CODE)
    }

    private fun initListeners() {
        coachSoundsSettingsEnableMusic.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onMusicChecked(
                isChecked
            )
        }
        coachSoundsSettingsEnableShuffle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onShuffleChecked(
                isChecked
            )
        }
        coachSoundsSettingsEnableTransitionSounds.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onTransitionSoundsChecked(
                isChecked
            )
        }
        llOpenMusic.setOnClickListener { viewModel.chooseMusic() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MUSIC_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val selectedImageUri = data?.data
                selectedImageUri?.let {
                    viewModel.onMusicChosen(selectedImageUri)
                }
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(TranslationContext(newBase))
    }

    companion object {
        internal fun createIntent(
            context: Context,
            toothbrushMac: String?
        ) = Intent(context, CoachSoundsSettingsActivity::class.java).apply {
            putExtra(INTENT_TOOTHBRUSH_MAC, toothbrushMac)
        }

        const val MUSIC_REQUEST_CODE = 403
    }
}
