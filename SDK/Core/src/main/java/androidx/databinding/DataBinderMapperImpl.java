/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package androidx.databinding;

import android.annotation.SuppressLint;
import androidx.annotation.Keep;

@Keep
@SuppressLint("SdkPublicClassInNonKolibreePackage")
public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new com.kolibree.core.DataBinderMapperImpl());
  }
}
