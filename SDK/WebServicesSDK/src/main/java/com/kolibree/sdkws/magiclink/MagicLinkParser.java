/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.magiclink;

import static com.kolibree.android.defensive.Preconditions.*;

import android.net.Uri;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.failearly.FailEarly;
import javax.inject.Inject;

@Keep
@Deprecated
// After introducing AppLinks most of this class is useless
// TODO("Refactor MagicLinkParser https://kolibree.atlassian.net/browse/KLTB002-12873")
public class MagicLinkParser {

  /** Android < 6 Kolibree scheme. */
  @Deprecated // With app links we can only use https scheme
  @VisibleForTesting
  static final String SCHEME_KOLIBREE_COMPAT = "kolibree";
  /** Android < 6 Colgate Connect scheme. Also used in the fallback HTML page */
  @Deprecated // With app links we can only use https scheme
  @VisibleForTesting
  static final String SCHEME_COLGATE_COMPAT = "colgateconnect";
  // Scheme used for HUM
  @Deprecated // With app links we can only use https scheme
  @VisibleForTesting
  static final String SCHEME_HUM_COMPAT = "colgatehum";

  @VisibleForTesting static final String HOST_SUFFIX = "kolibree.com";

  @Inject
  MagicLinkParser() {}

  /** This method has been deprecated, please use parseMagicCode instead */
  @Deprecated
  @NonNull
  public String parse(@NonNull Uri uri) {
    return parseMagicCode(uri).getCode();
  }

  @NonNull
  public MagicCode parseMagicCode(@NonNull Uri uri) {
    try {
      if (uri.getScheme().startsWith(SCHEME_KOLIBREE_COMPAT)
          || uri.getScheme().startsWith(SCHEME_COLGATE_COMPAT)
          || uri.getScheme().startsWith(SCHEME_HUM_COMPAT)) {
        return extractCodeFromCompatUri(uri);
      } else if (uri.getHost().endsWith(HOST_SUFFIX)) {
        return extractCodeFromUri(uri);
      }
    } catch (RuntimeException e) {
      // Report to Firebase when it happens
      FailEarly.fail("Magic link parsing failed for URI " + uri, e);
    }

    throw new IllegalStateException("Invalid magic link");
  }

  @NonNull
  @Deprecated // Uri with custom scheme is no longer supported
  private MagicCode extractCodeFromCompatUri(@NonNull Uri uri) {
    String[] uriSplit = uri.toString().split("magiclink/");
    checkArgument(uriSplit.length >= 2);
    checkArgument(!uriSplit[1].isEmpty());
    return new MagicCode(uriSplit[1], true);
  }

  @NonNull
  private MagicCode extractCodeFromUri(@NonNull Uri uri) {
    String code = uri.getQueryParameter("code");
    checkNotNull(code);
    return new MagicCode(code, false);
  }
}
