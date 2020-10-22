/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test;

import android.annotation.SuppressLint;
import com.kolibree.android.utils.EmailVerifier;
import java.util.regex.Pattern;

@SuppressLint("DeobfuscatedPublicSdkClass")
public class TestEmailVerifier extends EmailVerifier {

  private Pattern EMAIL_ADDRESS =
      Pattern.compile(
          "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@"
              + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}"
              + "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");

  @Override
  public Pattern getEmailAddressPattern() {
    return EMAIL_ADDRESS;
  }
}
