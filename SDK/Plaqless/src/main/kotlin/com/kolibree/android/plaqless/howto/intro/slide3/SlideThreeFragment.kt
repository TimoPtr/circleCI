/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide3

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.extensions.appName
import com.kolibree.android.plaqless.R
import com.kolibree.android.plaqless.databinding.FragmentSlideThreeBinding
import com.kolibree.android.tracker.NonTrackableScreen

internal class SlideThreeFragment :
    BaseMVIFragment<SlideThreeViewState,
        SlideThreeActions,
        SlideThreeViewModel.Factory,
        SlideThreeViewModel,
        FragmentSlideThreeBinding>(),
    NonTrackableScreen {

    companion object {
        @JvmStatic
        fun newInstance(): SlideThreeFragment {
            val fragment = SlideThreeFragment()

            return fragment
        }
    }

    private lateinit var player: Player

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = buildPlayer(requireContext())
        binding.slide3Player.player = player
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    override fun getViewModelClass(): Class<SlideThreeViewModel> = SlideThreeViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_slide_three

    override fun execute(action: SlideThreeActions) {
        // no-op
    }

    private fun buildPlayer(context: Context): Player =
        ExoPlayerFactory.newSimpleInstance(context).apply {
            val dataSourceFactory =
                DefaultDataSourceFactory(
                    requireContext(),
                    Util.getUserAgent(context, context.appName())
                )

            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(context.getString(R.string.plaqless_how_to_video_url)))

            prepare(mediaSource)
        }
}
