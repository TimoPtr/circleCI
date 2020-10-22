/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.sample.showcase

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.widget.setSeconds
import com.kolibree.android.baseui.hum.R
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_theme_showcase.*
import kotlinx.android.synthetic.main.content_hum_theme_showcase.*
import timber.log.Timber

@VisibleForApp
class ThemeShowcaseActivity : AppCompatActivity() {
    private var timerDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getCurrentTheme().themeRes)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_showcase)
        setSupportActionBar(toolbar)

        // Set error
        findViewById<TextInputLayout>(R.id.error_input).error = "Sample error!"
        initThemeSpinner()
    }

    private fun initThemeSpinner() = with(themeSpinner) {
        adapter =
            ArrayAdapter(context, android.R.layout.simple_spinner_item, themes).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        setSelection(themes.indexOf(getCurrentTheme()), false)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // no-op
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val bundle = Bundle().apply {
                    putParcelable(EXTRA_THEME, themes[position])
                }
                val intent = Intent(
                    this@ThemeShowcaseActivity, ThemeShowcaseActivity::class.java
                ).apply { putExtras(bundle) }

                finish()
                startActivity(intent)
            }
        }
    }

    @SuppressLint("RxDefaultScheduler")
    override fun onResume() {
        super.onResume()

        // Time should be fed from the outside (usually view model) - widget should not track time by its own
        timerDisposable = Observable.interval(1L, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .subscribe({
                runOnUiThread {
                    timerView.setSeconds(it)
                }
            }, Timber::e)
    }

    override fun onPause() {
        timerDisposable?.dispose()
        timerDisposable = null
        super.onPause()
    }

    private fun getCurrentTheme(): AvailableTheme {
        return intent.takeIf { it.hasExtra(EXTRA_THEME) }?.getParcelableExtra(EXTRA_THEME)
            ?: themes.first()
    }

    @VisibleForApp
    companion object {
        const val EXTRA_THEME = "EXTRA_THEME"

        private val themes = arrayOf(
            AvailableTheme("AppTheme", R.style.AppTheme),
            AvailableTheme("AppTheme Inverse", R.style.AppTheme_Inverse),
            AvailableTheme("AppTheme Inverse Red", R.style.AppTheme_Inverse_Red),
            AvailableTheme("AppTheme Inverse Teal", R.style.AppTheme_Inverse_Teal),
            AvailableTheme("AppTheme Inverse Blue", R.style.AppTheme_Inverse_Blue)
        )
    }
}

@Parcelize
internal data class AvailableTheme(val name: String, @StyleRes val themeRes: Int) : Parcelable {
    override fun toString(): String {
        return name
    }
}
