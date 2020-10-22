/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils;

import android.util.Patterns;
import androidx.annotation.Keep;
import java.util.regex.Pattern;
import javax.inject.Inject;

@Keep
public class EmailVerifierImpl extends EmailVerifier {

  @Inject
  EmailVerifierImpl() {}

  @Override
  public Pattern getEmailAddressPattern() {
    return Patterns.EMAIL_ADDRESS;
  }
}
