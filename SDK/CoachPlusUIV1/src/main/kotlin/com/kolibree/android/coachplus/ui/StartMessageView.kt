/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.activity.BaseActivity
import com.kolibree.android.coachplus.R
import com.kolibree.android.coachplus.utils.StartMessageTypeProvider
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.setOnDebouncedClickListener

@VisibleForApp
class StartMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @Keep
    interface StartMessageNavigator {
        fun navigateToSettingsScreen()
    }

    private val modelImageView: ImageView
    private val modelTextView: TextView
    private val messageTypeProvider = StartMessageTypeProvider()

    init {
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.start_message_view, this, true)

        modelImageView = root.findViewById(R.id.model_preview)
        modelTextView = root.findViewById(R.id.model_description)

        initToolbar(root)
    }

    private fun initToolbar(root: View) {
        val toolbar = root.findViewById<Toolbar>(R.id.toolbar)
        toolbar.navigationIcon = ContextCompat.getDrawable(context,
            R.drawable.ic_arrow_back
        )
        toolbar.setNavigationOnClickListener { _ ->
            val parentActivity = context as BaseActivity?
            parentActivity?.onBackPressed()
        }
        val settings = toolbar.findViewById<View>(R.id.settings)
        settings.setOnDebouncedClickListener {
            val navigator = context as? StartMessageNavigator
            navigator?.navigateToSettingsScreen()
        }
    }

    fun hide() {
        animate().alpha(0f).withEndAction {
            visibility = View.GONE
        }
    }

    fun show(modelName: String) {
        alpha = 0f
        visibility = View.VISIBLE
        animate().alpha(1f)

        ToothbrushModel.getModelByCommercialName(modelName)?.apply {
            renderModel(this)
            renderDescription(this)
        }
    }

    private fun renderDescription(model: ToothbrushModel) {
        val descriptionRes = messageTypeProvider.descriptionResource(model)
        modelTextView.setText(descriptionRes)
    }

    private fun renderModel(model: ToothbrushModel) {
        val modelRaw = messageTypeProvider.modelResource(model)
        Glide.with(context)
            .load(modelRaw)
            .into(modelImageView)
    }
}
