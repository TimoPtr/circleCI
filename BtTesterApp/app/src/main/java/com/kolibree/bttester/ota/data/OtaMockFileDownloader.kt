/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.ota.data

import android.content.Context
import com.kolibree.android.network.utils.FileDownloader
import com.kolibree.android.utils.assets.AssetLoader
import java.io.File
import java.net.URL
import javax.inject.Inject

class OtaMockFileDownloader @Inject constructor(
    context: Context
) : FileDownloader(context) {

    private val assetLoader = AssetLoader(context)

    override fun download(url: String): File {
        return assetLoader.loadFile(url)
    }

    override fun download(url: URL): File {
        return assetLoader.loadFile(url.toString())
    }

    override fun download(url: String, filename: String): File {
        return assetLoader.loadFile(filename)
    }

    override fun download(url: URL, filename: String): File {
        return assetLoader.loadFile(filename)
    }
}
