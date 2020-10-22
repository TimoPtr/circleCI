package com.kolibree.android.test.dagger;

import com.kolibree.android.app.tracker.TestEventTracker;
import com.kolibree.android.tracker.EventTracker;
import com.kolibree.android.tracker.studies.StudiesForProfileUseCase;
import com.kolibree.android.tracker.studies.StudiesForProfileUseCaseImpl;
import com.kolibree.android.tracker.studies.StudiesRepository;
import com.kolibree.android.tracker.studies.StudiesRepositoryImpl;
import dagger.Binds;
import dagger.Module;

/** Created by Kornel on 3/16/2018. */
@Module
public abstract class EspressoEventTrackerModule {

  @Binds
  abstract EventTracker providesEventTracker(TestEventTracker tracker);

  @Binds
  abstract StudiesRepository bindStudiesRepository(StudiesRepositoryImpl impl);

  @Binds
  abstract StudiesForProfileUseCase bindProfileStudiesManager(StudiesForProfileUseCaseImpl impl);
}
