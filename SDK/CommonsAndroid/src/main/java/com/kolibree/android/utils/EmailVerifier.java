/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import java.util.regex.Pattern;

@Keep
public abstract class EmailVerifier {

  public abstract Pattern getEmailAddressPattern();

  public boolean isEmpty(@Nullable String email) {
    return email == null || email.isEmpty();
  }

  public boolean isValid(@Nullable String email) {
    return !isEmpty(email) && isMatchingEmailPattern(email);
  }

  /**
   * Checks if the email parameter is valid.
   *
   * @param email the string to check
   * @return true if it's a valid email, false otherwise
   */
  public boolean isMatchingEmailPattern(@Nullable String email) {
    if (email == null) {
      email = "";
    }
    return getEmailAddressPattern().matcher(email).matches();
  }
}
