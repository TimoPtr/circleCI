/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.persistence;

import com.kolibree.android.commons.interfaces.Truncable;
import com.kolibree.android.offlinebrushings.OrphanBrushing;
import java.util.List;

/** Created by miguelaragues on 28/11/17. */
public interface OrphanBrushingRepository extends SDKOrphanBrushingRepository, Truncable {

  long insert(OrphanBrushing orphanBrushing);

  void delete(List<OrphanBrushing> orphanBrushing);

  void update(List<OrphanBrushing> orphanBrushing);

  void delete(OrphanBrushing... orphanBrushings);
}
