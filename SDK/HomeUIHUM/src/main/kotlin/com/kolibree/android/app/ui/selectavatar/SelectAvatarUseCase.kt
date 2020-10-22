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
import androidx.annotation.StringRes
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.utils.AvatarDataStore
import com.kolibree.android.homeui.hum.R
import com.kolibree.sdkws.utils.ApiSDKUtils
import io.reactivex.Flowable
import io.reactivex.functions.Function
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import org.reactivestreams.Publisher
import timber.log.Timber

/**
 * Scoped instance of this class will be used by [SelectAvatarDialogFragment]
 *
 * The first will trigger the operation and dismiss the dialog, while the latter keeps a subscription
 * throughout its whole lifecycle to listen to select avatar results.
 *
 * The reasoning behind this split/scoping is that the DialogFragment is the one to trigger the
 * operation, but for a non-blocking user experience, we want to dismiss it as soon as the user
 * selects an option (gallery/picture). Thus, we can't tie the RxStream to the lifecycle of the
 * DialogFragment.
 */
@ActivityScope
internal class SelectAvatarUseCase
@Inject constructor(
    private val context: ApplicationContext,
    private val apiSDKUtils: ApiSDKUtils,
    private val galleryFacade: GalleryFacade,
    private val avatarDataStore: AvatarDataStore
) : AvatarSelectedUseCase,
    StoreAvatarProducer {

    private val bitmapPublisher = PublishProcessor.create<Bitmap>()
    private val uriPublisher = PublishProcessor.create<Uri>()
    private val errorResultPublisher = PublishProcessor.create<StoreAvatarResult.Error>()

    override fun onUriSelected(uri: Uri) {
        uriPublisher.onNext(uri)
    }

    override fun onPictureTaken(bitmap: Bitmap) {
        bitmapPublisher.onNext(bitmap)
    }

    override fun onTakePictureNotOk() {
        emitError(R.string.change_avatar_error_take_picture_result_not_ok)
    }

    override fun onChooseFromGalleryNotOk() {
        emitError(R.string.change_avatar_error_select_gallery_result_not_ok)
    }

    private fun emitError(@StringRes messageResId: Int) {
        val exception = createStoreAvatarException(messageResId)
        errorResultPublisher.onNext(StoreAvatarResult.Error(exception))
    }

    override fun avatarResultStream(): Flowable<StoreAvatarResult> {
        // Prevent stream termination to avoid Flowable completing and Avatars not processed anymore
        return Flowable.merge(
            errorResultPublisher,
            pictureTakenObservable().preventStreamTermination(),
            gallerySelectedObservable().preventStreamTermination()
        )
    }

    private fun gallerySelectedObservable(): Flowable<StoreAvatarResult> {
        return uriPublisher
            .observeOn(Schedulers.computation())
            .switchMap { uri ->
                val galleryBitmap = galleryFacade.loadFromGallery(uri)

                if (galleryBitmap == null) {
                    Timber.w("Couldn't load bitmap from gallery $galleryBitmap")
                    val exception =
                        createStoreAvatarException(R.string.change_avatar_error_gallery_cant_load)
                    Flowable.just(StoreAvatarResult.Error(exception))
                } else {
                    processBitmapObservable(galleryBitmap)
                }
            }
    }

    private fun pictureTakenObservable(): Flowable<StoreAvatarResult> {
        return bitmapPublisher
            .observeOn(Schedulers.computation())
            .switchMap { bitmap -> processBitmapObservable(bitmap) }
    }

    private fun processBitmapObservable(bitmap: Bitmap): Flowable<out StoreAvatarResult> {
        val formattedBitmap = formatAndSave(bitmap)

        return if (formattedBitmap == null) {
            Timber.w("Couldn't format bitmap $bitmap")
            val exception = createStoreAvatarException(R.string.change_avatar_error_bitmap)
            Flowable.just(StoreAvatarResult.Error(exception))
        } else {
            Flowable.just(
                StoreAvatarResult.Success(
                    formattedBitmap.absolutePath
                )
            )
        }
    }

    // Format bitmap and save to file
    private fun formatAndSave(bitmap: Bitmap?): File? {
        return bitmap?.let { saveBitmapToFile(apiSDKUtils.kolibrizeAvatar(it)) }
    }

    // Save picture locally and save url /!\ recycles source bitmap
    private fun saveBitmapToFile(picture: Bitmap?): File? {
        if (picture != null) {
            // Save to cache dir
            val file = avatarDataStore.saveToStorage(context, picture)
            picture.recycle()
            if (file != null) {
                return file
            }
        }
        return null
    }

    /**
     * Prevent stream termination because of an error
     */
    private fun Flowable<StoreAvatarResult>.preventStreamTermination(): Flowable<StoreAvatarResult> {
        return onErrorResumeNext(Function<Throwable, Publisher<StoreAvatarResult>> { throwable ->
            val exception = createStoreAvatarException(R.string.change_avatar_error_bitmap)
            exception.initCause(throwable)

            Flowable.just(StoreAvatarResult.Error(exception))
        })
    }

    private fun createStoreAvatarException(messageResId: Int) =
        StoreAvatarException(context, messageResId)
}

/**
 * Interface that deals with the user's choice of avatar
 */
internal interface AvatarSelectedUseCase {
    fun onUriSelected(uri: Uri)
    fun onPictureTaken(bitmap: Bitmap)
    fun onTakePictureNotOk()
    fun onChooseFromGalleryNotOk()
}

internal interface StoreAvatarProducer {
    /**
     * Scheduler: Computation scheduler by default
     *
     * @return [Flowable]<[StoreAvatarResult]> that will emit a [StoreAvatarResult] each time the
     * user attempts to change its avatar
     */
    fun avatarResultStream(): Flowable<StoreAvatarResult>
}
