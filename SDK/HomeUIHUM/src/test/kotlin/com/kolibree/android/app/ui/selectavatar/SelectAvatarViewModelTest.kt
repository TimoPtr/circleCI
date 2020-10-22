/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultCaller
import androidx.lifecycle.Lifecycle
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.test.invokeOnCleared
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.test.utils.randomInt
import com.kolibree.sdkws.core.AvatarCache
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SelectAvatarViewModelTest : BaseUnitTest() {

    private val initialViewState: SelectAvatarViewState = SelectAvatarViewState.initial()
    private val initialActionProvider = SelectAvatarActionInitialProvider()
    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val avatarSelectedUseCase: AvatarSelectedUseCase = mock()
    private val avatarCache: AvatarCache = mock()
    private val imageCaptor: ImageCaptor = mock()
    private val contract: TakeExternalStoragePictureContract = mock()
    private val activityResultCaller: ActivityResultCaller = mock()

    private lateinit var viewModel: SelectAvatarViewModel

    @Test
    fun `initSubscribes emits viewState with updated value from currentProfileProvider`() {
        val originalPictureUrl = "original"
        val expectedPictureUrl = "bla"
        val profile = ProfileBuilder.create().withPictureUrl(originalPictureUrl).build()
        whenever(avatarCache.getAvatarUrl(profile)).thenReturn(expectedPictureUrl)
        initViewModelWithProfile(profile)

        viewModel.viewStateFlowable.test().assertValue(
            initialViewState.copy(
                avatarUrl = expectedPictureUrl,
                profileName = ProfileBuilder.DEFAULT_NAME
            )
        )
    }

    @Test
    fun `subscription to currentProfileProvider is disposed in onCleared`() {
        val subject = SingleSubject.create<Profile>()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(subject)

        initViewModel()

        assertTrue(subject.hasObservers())

        viewModel.invokeOnCleared()

        assertFalse(subject.hasObservers())
    }

    /*
    onTakePictureClicked
     */
    @Test
    fun `onTakePictureClicked pushes LaunchCameraAction`() {
        initViewModelWithProfile()

        val actionObservable = viewModel.actionsObservable.test()

        viewModel.onTakePictureClicked()

        actionObservable.assertValue(SelectAvatarAction.LaunchCameraAction)
    }

    /*
    onChooseFromGalleryClicked
     */
    @Test
    fun `onChooseFromGalleryClicked pushes ChooseFromGalleryAction`() {
        initViewModelWithProfile()

        val actionObservable = viewModel.actionsObservable.test()

        viewModel.onChooseFromGalleryClicked()

        actionObservable.assertValue(SelectAvatarAction.ChooseFromGalleryAction)
    }

    /*
    prepareImageCapture result
     */
    @Test
    fun `image capture callback pushes DismissDialog action when callback receives null bitmap and fragment is resumed`() {
        initViewModelWithProfile()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val testObserver = viewModel.actionsObservable.test()
        val bitmap: Bitmap? = null

        invokePrepareCaptureBitmapCallback(bitmap)

        testObserver.assertValue(SelectAvatarAction.DismissDialog)
        testObserver.dispose()
    }

    @Test
    fun `image capture callback enqueues DismissDialog action when callback receives null bitmap and fragment is not resumed`() {
        initViewModelWithProfile()

        val bitmap: Bitmap? = null

        invokePrepareCaptureBitmapCallback(bitmap)

        assertDismissDialogWillBeSentAsInitialAction()
    }

    @Test
    fun `image capture callback pushes DismissDialog action when callback receives non-null bitmap and fragment is resumed`() {
        initViewModelWithProfile()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val testObserver = viewModel.actionsObservable.test()
        val bitmap: Bitmap = mock()

        invokePrepareCaptureBitmapCallback(bitmap)

        testObserver.assertValue(SelectAvatarAction.DismissDialog)
        testObserver.dispose()
    }

    @Test
    fun `image capture callback enqueues DismissDialog action when callback receives non-null bitmap and fragment is not resumed`() {
        initViewModelWithProfile()

        val bitmap: Bitmap = mock()

        invokePrepareCaptureBitmapCallback(bitmap)

        assertDismissDialogWillBeSentAsInitialAction()
    }

    @Test
    fun `image capture callback invokes onTakePictureNotOk when callback receives null bitmap`() {
        initViewModelWithProfile()

        val bitmap: Bitmap? = null

        invokePrepareCaptureBitmapCallback(bitmap)

        verify(avatarSelectedUseCase).onTakePictureNotOk()

        verify(avatarSelectedUseCase, never()).onPictureTaken(any())
    }

    @Test
    fun `image capture callback invokes onPictureTaken when callback receives non-null bitmap`() {
        initViewModelWithProfile()

        val bitmap: Bitmap = mock()

        invokePrepareCaptureBitmapCallback(bitmap)

        verify(avatarSelectedUseCase).onPictureTaken(bitmap)

        verify(avatarSelectedUseCase, never()).onTakePictureNotOk()
    }

    /*
    onActivityResult PICK_PICTURE_REQUEST_CODE
     */
    @Test
    fun `onActivityResult stores DismissDialog as initial action when request code is PICK_PICTURE_REQUEST_CODE and result is RESULT_OK`() {
        initViewModelWithProfile()

        val requestCode = PICK_PICTURE_REQUEST_CODE
        val responseCode = Activity.RESULT_OK

        val resultIntent = mockChooseFromGalleryResult()

        assertInitialActionIsNull()

        viewModel.onActivityResult(requestCode, responseCode, resultIntent)

        assertDismissDialogWillBeSentAsInitialAction()
    }

    @Test
    fun `onActivityResult invokes onUriSelected when request code is PICK_PICTURE_REQUEST_CODE, result is RESULT_OK and result contains a bitmap`() {
        initViewModelWithProfile()

        val requestCode = PICK_PICTURE_REQUEST_CODE
        val responseCode = Activity.RESULT_OK

        val uri: Uri = mock()
        val resultIntent = mockChooseFromGalleryResult(uri)

        viewModel.onActivityResult(requestCode, responseCode, resultIntent)

        verify(avatarSelectedUseCase).onUriSelected(uri)
    }

    @Test
    fun `onActivityResult never invokes onUriSelected when request code is PICK_PICTURE_REQUEST_CODE, result is RESULT_OK and result doesn't contain a bitmap`() {
        initViewModelWithProfile()

        val requestCode = PICK_PICTURE_REQUEST_CODE
        val responseCode = Activity.RESULT_OK

        val resultIntent = mockChooseFromGalleryResult(uri = null)

        viewModel.onActivityResult(requestCode, responseCode, resultIntent)

        verify(avatarSelectedUseCase, never()).onUriSelected(any())
    }

    @Test
    fun `onActivityResult stores DismissDialog as initial action when request code is PICK_PICTURE_REQUEST_CODE and result is not ok`() {
        initViewModelWithProfile()

        val requestCode = PICK_PICTURE_REQUEST_CODE
        val responseCode = Activity.RESULT_CANCELED

        assertInitialActionIsNull()

        viewModel.onActivityResult(requestCode, responseCode, null)

        assertDismissDialogWillBeSentAsInitialAction()
    }

    @Test
    fun `onActivityResult invokes onTakePictureNotOk when request code is PICK_PICTURE_REQUEST_CODE and result is not ok`() {
        initViewModelWithProfile()

        val requestCode = PICK_PICTURE_REQUEST_CODE
        val responseCode = Activity.RESULT_CANCELED

        viewModel.onActivityResult(requestCode, responseCode, null)

        verify(avatarSelectedUseCase).onChooseFromGalleryNotOk()
    }

    /*
    onActivityResult other code
     */
    @Test
    fun `onActivityResult does nothing if request code is not PICK_PICTURE_REQUEST_CODE or TAKE_PICTURE_REQUEST_CODE`() {
        initViewModelWithProfile()

        assertInitialActionIsNull()

        arrayOf(randomInt(), randomInt(), randomInt())
            .filter { it != PICK_PICTURE_REQUEST_CODE && it != TAKE_PICTURE_REQUEST_CODE }
            .forEach { requestCode ->
                viewModel.onActivityResult(requestCode, Activity.RESULT_OK, null)
            }

        assertInitialActionIsNull()
        verifyNoMoreInteractions(avatarSelectedUseCase)
    }

    /*
    Utils
     */

    private fun mockChooseFromGalleryResult(uri: Uri? = mock()): Intent {
        return mock<Intent>().apply {
            val bundle = mock<Bundle>().apply {
                whenever(data).thenReturn(uri)
            }
            whenever(extras).thenReturn(bundle)
        }
    }

    private fun initViewModelWithProfile(profile: Profile = ProfileBuilder.create().build()) {
        currentProfileProvider.mockCurrentProfileSingle(profile)

        initViewModel()
    }

    /**
     * Use this if you want to control what currentProfileProvider returns
     */
    private fun initViewModel() {
        viewModel =
            SelectAvatarViewModel(
                initialViewState = initialViewState,
                currentProfileProvider = currentProfileProvider,
                avatarSelectedUseCase = avatarSelectedUseCase,
                initialActionProvider = initialActionProvider,
                avatarCache = avatarCache,
                takeExternalStoragePictureContract = contract,
                imageCaptor = imageCaptor
            )
    }

    private fun assertInitialActionIsNull() {
        assertNull(initialActionProvider.action)
    }

    private fun assertDismissDialogWillBeSentAsInitialAction() {
        assertEquals(
            SelectAvatarAction.DismissDialog,
            initialActionProvider.action
        )
    }

    private fun invokePrepareCaptureBitmapCallback(bitmap: Bitmap?) {
        whenever(
            imageCaptor.prepareCaptureBitmap(
                coroutineScope = any(),
                activityResultCaller = eq(activityResultCaller),
                contract = eq(contract),
                callback = any()
            )
        ).thenAnswer {
            (it.getArgument(3) as (Bitmap?) -> Unit).invoke(bitmap)
        }

        viewModel.prepareImageCapture(activityResultCaller = activityResultCaller)
    }
}
