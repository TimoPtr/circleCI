/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import android.graphics.Bitmap
import android.net.Uri
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.utils.AvatarDataStore
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.utils.ApiSDKUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.io.File
import org.junit.Test

internal class SelectAvatarUseCaseTest : BaseUnitTest() {

    private val context: ApplicationContext = mock()
    private val apiSDKUtils: ApiSDKUtils = mock()
    private val galleryFacade: GalleryFacade = mock()
    private val avatarDataStore: AvatarDataStore = mock()

    private val useCase =
        SelectAvatarUseCase(
            context = context,
            apiSDKUtils = apiSDKUtils,
            galleryFacade = galleryFacade,
            avatarDataStore = avatarDataStore
        )

    /*
    onUriSelected
     */
    @Test
    fun `onUriSelected emits result error if uri couldn't be loaded from Gallery`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        val uri = uri()
        whenever(galleryFacade.loadFromGallery(uri)).thenReturn(null)

        useCase.onUriSelected(uri)

        resultObservable.assertLastValueWithPredicate { result ->
            result is StoreAvatarResult.Error
        }
    }

    @Test
    fun `onUriSelected emits result error if bitmap from Gallery couldn't be kolibrized`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        val uri = uri()
        val galleryBitmap = mock<Bitmap>()
        whenever(galleryFacade.loadFromGallery(uri)).thenReturn(galleryBitmap)

        whenever(apiSDKUtils.kolibrizeAvatar(galleryBitmap)).thenReturn(null)

        useCase.onUriSelected(uri)

        resultObservable.assertLastValueWithPredicate { result ->
            result is StoreAvatarResult.Error
        }
    }

    @Test
    fun `onUriSelected emits result error if bitmap from Gallery couldn't be saved to storage`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        val uri = uri()
        val bitmap = mock<Bitmap>()
        whenever(galleryFacade.loadFromGallery(uri)).thenReturn(bitmap)

        whenever(apiSDKUtils.kolibrizeAvatar(bitmap)).thenReturn(bitmap)

        whenever(avatarDataStore.saveToStorage(context, bitmap)).thenReturn(null)

        useCase.onUriSelected(uri)

        resultObservable.assertLastValueWithPredicate { result ->
            result is StoreAvatarResult.Error
        }
    }

    @Test
    fun `onUriSelected emits result success if bitmap was properly processed`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        val uri = uri()
        val bitmap = mock<Bitmap>()
        whenever(galleryFacade.loadFromGallery(uri)).thenReturn(bitmap)

        mockSuccessfulBitmapProcess(bitmap)

        useCase.onUriSelected(uri)

        resultObservable.assertLastValueWithPredicate { result ->
            (result is StoreAvatarResult.Success) && result.avatarPath == DEFAULT_PATH
        }
    }

    @Test
    fun `stream does not terminate if gallery bitmap process throws error`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        val uri = uri()
        val bitmap = mock<Bitmap>()
        whenever(galleryFacade.loadFromGallery(uri)).thenReturn(bitmap)

        mockFailureBitmapProcess(bitmap)

        useCase.onUriSelected(uri)

        resultObservable.assertLastValueWithPredicate { result ->
            result is StoreAvatarResult.Error
        }

        resultObservable.assertNotComplete()
    }

    /*
    onPictureTaken
     */
    @Test
    fun `onPictureTaken emits result error if bitmap from Picture couldn't be kolibrized`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        val pictureBitmap = mock<Bitmap>()

        whenever(apiSDKUtils.kolibrizeAvatar(pictureBitmap)).thenReturn(null)

        useCase.onPictureTaken(pictureBitmap)

        resultObservable.assertLastValueWithPredicate { result ->
            result is StoreAvatarResult.Error
        }
    }

    @Test
    fun `onPictureTaken emits result error if bitmap from Picture couldn't be saved to storage`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        val pictureBitmap = mock<Bitmap>()

        whenever(apiSDKUtils.kolibrizeAvatar(pictureBitmap)).thenReturn(pictureBitmap)

        whenever(avatarDataStore.saveToStorage(context, pictureBitmap)).thenReturn(null)

        useCase.onPictureTaken(pictureBitmap)

        resultObservable.assertLastValueWithPredicate { result ->
            result is StoreAvatarResult.Error
        }
    }

    @Test
    fun `onPictureTaken emits result success if bitmap was properly processed`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        val pictureBitmap = mock<Bitmap>()

        mockSuccessfulBitmapProcess(pictureBitmap)

        useCase.onPictureTaken(pictureBitmap)

        resultObservable.assertLastValueWithPredicate { result ->
            (result is StoreAvatarResult.Success) && result.avatarPath == DEFAULT_PATH
        }
    }

    @Test
    fun `stream does not terminate if picture taken bitmap process throws error`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        val pictureBitmap = mock<Bitmap>()

        mockFailureBitmapProcess(pictureBitmap)

        useCase.onPictureTaken(pictureBitmap)

        resultObservable.assertLastValueWithPredicate { result ->
            result is StoreAvatarResult.Error
        }

        resultObservable.assertNotComplete()
    }

    /*
    onTakePictureNotOk
     */
    @Test
    fun `onTakePictureNotOk emits error`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        useCase.onTakePictureNotOk()

        resultObservable.assertLastValueWithPredicate { result ->
            result is StoreAvatarResult.Error
        }
    }

    /*
    onChooseFromGalleryNotOk()
     */
    @Test
    fun `onChooseFromGalleryNotOk emits error`() {
        val resultObservable = useCase.avatarResultStream().test().assertEmpty()

        useCase.onChooseFromGalleryNotOk()

        resultObservable.assertLastValueWithPredicate { result ->
            result is StoreAvatarResult.Error
        }
    }

    /*
    Utils
     */

    private fun uri(): Uri = mock()

    private fun mockSuccessfulBitmapProcess(bitmap: Bitmap) {
        whenever(apiSDKUtils.kolibrizeAvatar(bitmap)).thenReturn(bitmap)

        val storageFile = mockFileWithPath()
        whenever(avatarDataStore.saveToStorage(context, bitmap)).thenReturn(storageFile)
    }

    private fun mockFailureBitmapProcess(bitmap: Bitmap) {
        whenever(apiSDKUtils.kolibrizeAvatar(bitmap)).thenReturn(null)
    }

    private fun mockFileWithPath(): File =
        mock<File>().apply { whenever(absolutePath).thenReturn(DEFAULT_PATH) }
}

private const val DEFAULT_PATH = "PATH"

internal fun CurrentProfileProvider.mockCurrentProfileSingle(
    profile: Profile = ProfileBuilder.create().build()
): Profile {
    whenever(currentProfileSingle()).thenReturn(Single.just(profile))

    return profile
}
