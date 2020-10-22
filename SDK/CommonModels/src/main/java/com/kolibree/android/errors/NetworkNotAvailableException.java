/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.errors;

import androidx.annotation.Keep;

/** Created by miguelaragues on 22/1/18. */
/*
Warning: Don't include this in the javadoc !!!

This class has been Keep-annotated because its name has been chosen to be the xor key to decrypt
the hardcoded AES key value.
Please refer to the .utils.KolibreeGuard class for more information
 */
@Keep
public class NetworkNotAvailableException extends Exception {}
