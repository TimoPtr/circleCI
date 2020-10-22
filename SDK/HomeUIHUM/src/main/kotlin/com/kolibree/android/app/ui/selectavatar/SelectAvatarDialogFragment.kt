/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import android.Manifest.permission.CAMERA
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.content.Intent.CATEGORY_OPENABLE
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.ui.dialog.BaseBottomSheetDialogFragment
import com.kolibree.android.app.ui.dialog.showIfNotPresent
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.DialogSelectAvatarBinding
import com.kolibree.databinding.BR
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

internal class SelectAvatarDialogFragment : BaseBottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: SelectAvatarViewModel.Factory

    lateinit var viewModel: SelectAvatarViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                launchCameraIntent()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModel()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(SelectAvatarViewModel::class.java)
        lifecycle.addObserver(viewModel)
        viewModel.prepareImageCapture(activityResultCaller = this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: DialogSelectAvatarBinding =
            inflate(inflater, R.layout.dialog_select_avatar, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.setVariable(BR.viewModel, viewModel)
        binding.setVariable(BR.showCurrentProfile, arguments?.getBoolean(ARG_SHOW_CURRENT_PROFILE) ?: false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        disposeOnPause { listenToActions() }
    }

    private fun listenToActions(): Disposable {
        return viewModel.actionsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                ::onAction,
                Timber::e
            )
    }

    private fun onAction(action: SelectAvatarAction) {
        when (action) {
            SelectAvatarAction.LaunchCameraAction -> launchCamera()
            SelectAvatarAction.ChooseFromGalleryAction -> launchChooseFromGallery()
            SelectAvatarAction.DismissDialog -> dismiss()
        }
    }

    private fun launchCamera() {
        if (isCameraAllowed()) {
            launchCameraIntent()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            }
        }
    }

    // Check if the user allowed external storage reading
    private fun isCameraAllowed(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (ContextCompat.checkSelfPermission(requireContext(), CAMERA) == PERMISSION_GRANTED)
        } else {
            true
        }
    }

    private fun launchCameraIntent() {
        viewModel.acquireImage()
    }

    private fun launchChooseFromGallery() {
        val intent = Intent(ACTION_OPEN_DOCUMENT)
        intent.addCategory(CATEGORY_OPENABLE)
        intent.flags = FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        intent.type = GALLERY_INTENT_TYPE
        startActivityForResult(intent, PICK_PICTURE_REQUEST_CODE)
    }

    companion object {
        private const val FRAGMENT_TAG = "select_avatar_tag"

        fun showIfNotPresent(fragmentManager: FragmentManager, showCurrentProfile: Boolean = true) {
            fragmentManager.showIfNotPresent(FRAGMENT_TAG) {
                SelectAvatarDialogFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(ARG_SHOW_CURRENT_PROFILE, showCurrentProfile)
                    }
                }
            }
        }
    }
}

private const val ARG_SHOW_CURRENT_PROFILE = "arg_show_current_profile"
private const val GALLERY_INTENT_TYPE = "image/*"

internal const val TAKE_PICTURE_REQUEST_CODE = 4020
internal const val PICK_PICTURE_REQUEST_CODE = 4030
private const val REQUEST_CAMERA_PERMISSION = 1
