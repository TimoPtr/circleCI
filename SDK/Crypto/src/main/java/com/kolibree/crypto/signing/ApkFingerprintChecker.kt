/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto.signing

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.kolibree.crypto.signing.ApkFingerprintConstants.DIGEST_ALGORITHM
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject

internal class ApkFingerprintChecker @Inject constructor(private val context: Context) {

    val fingerprint: ApkFingerprint by lazy {
        try {
            val packageName = context.packageName
            val packageSignatures = getSignatures(context.packageManager, packageName)
            return@lazy packageSignatures
                .asSequence()
                .mapNotNull { calculateDigest(it.toByteArray()) }
                .mapNotNull { digest -> toFingerprint(digest) }
                .first()
        } catch (e: RuntimeException) {
            // Error getting fingerprint, assuming release version!
        }
        return@lazy ApkFingerprint.PRODUCTION
    }

    @VisibleForTesting
    fun getSignatures(packageManager: PackageManager, packageName: String): Array<Signature> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // https://www.blackhat.com/docs/us-14/materials/us-14-Forristal-Android-FakeID-Vulnerability-Walkthrough.pdf
            val signingInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo
            return if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            getLegacyPackageSignatures(packageManager, packageName)
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    @Suppress("Deprecation")
    @VisibleForTesting
    fun getLegacyPackageSignatures(
        packageManager: PackageManager,
        packageName: String
    ): Array<Signature> {
        return packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
    }

    @VisibleForTesting
    fun calculateDigest(certificateBytes: ByteArray): String {
        val messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM)
        val digest = messageDigest.digest(certificateBytes)
        return digest.fold(
            "",
            { str, it -> str + (if (str.isBlank()) "" else ":") + "%02x".format(it) })
            .toUpperCase(
                Locale.getDefault()
            )
    }

    @VisibleForTesting
    fun toFingerprint(fingerPrint: String): ApkFingerprint {
        return when (fingerPrint.toUpperCase(Locale.getDefault())) {
            ApkFingerprintConstants.DEVELOPMENT_DEBUG -> ApkFingerprint.DEBUG
            ApkFingerprintConstants.KOLIBREE_BETA -> ApkFingerprint.BETA
            else -> ApkFingerprint.PRODUCTION
        }
    }
}
