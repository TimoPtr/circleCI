/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.kolibree.android.app.ui.activity.BaseActivity
import com.kolibree.android.app.ui.activity.getCurrentNavFragment
import com.kolibree.android.brushingquiz.R
import com.kolibree.android.brushingquiz.databinding.ActivityBrushingQuizBinding
import com.kolibree.android.brushingquiz.presentation.quiz.QuizFragment
import com.kolibree.android.extensions.setOnDebouncedClickListener
import com.kolibree.android.tracker.NonTrackableScreen
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

internal class BrushingProgramActivity : BaseActivity(),
    NonTrackableScreen, HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    private lateinit var binding: ActivityBrushingQuizBinding

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = ActivityBrushingQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
    }

    override fun onStart() {
        super.onStart()
        addNavigationListener()
    }

    private fun addNavigationListener() {
        val fragment = getCurrentNavFragment(R.id.nav_host_fragment)!!
        fragment.findNavController()
        val nav = fragment.findNavController()
        nav.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.brushing_quiz_confirmation_fragment) {
                binding.toolbarBack.setImageResource(R.drawable.ic_nav_close)
            } else {
                binding.toolbarBack.setImageResource(R.drawable.ic_back_arrow)
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbarBack.setOnDebouncedClickListener {
            (getCurrentNavFragment(R.id.nav_host_fragment) as? QuizFragment)?.beforeOnClose()
            finish()
        }
    }

    override fun onBackPressed() {
        val backConsumed =
            (getCurrentNavFragment(R.id.nav_host_fragment) as? QuizFragment)?.onBackPressed()
                ?: false
        if (!backConsumed) super.onBackPressed()
    }
}

@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun createBrushingProgramIntent(context: Context): Intent {
    return Intent(context, BrushingProgramActivity::class.java)
}
