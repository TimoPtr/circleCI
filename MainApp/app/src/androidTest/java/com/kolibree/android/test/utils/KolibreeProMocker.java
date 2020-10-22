/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.kolibree.android.app.App;
import com.kolibree.android.app.dagger.EspressoAppComponent;
import com.kolibree.sdkws.core.KolibreeConnectorListener;
import com.kolibree.sdkws.core.ProfileWrapper;
import com.kolibree.sdkws.data.model.Practitioner;
import io.reactivex.Single;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * Responsible for mocking the behavior of KolibreePro related content
 *
 * <p>Created by miguelaragues on 5/2/18.
 */
public class KolibreeProMocker {

  private boolean shouldShowPopup = false;
  private String practitionerName, practitionerToken;
  private boolean hasMultipleDoctors;
  private boolean shouldThrowError;

  private KolibreeProMocker() {}

  public static KolibreeProMocker create() {
    return new KolibreeProMocker();
  }

  public KolibreeProMocker withShouldShowReminderThrowsError() {
    this.shouldThrowError = true;

    return this;
  }

  public KolibreeProMocker withShouldShowPopup(boolean shouldShowPopup) {
    this.shouldShowPopup = shouldShowPopup;

    return this;
  }

  public KolibreeProMocker withPractitionerToken(String practitionerToken) {
    this.practitionerToken = practitionerToken;

    return this;
  }

  public KolibreeProMocker withPractitionerName(String practitionerName) {
    this.practitionerName = practitionerName;

    return this;
  }

  public KolibreeProMocker withHasMultipleDoctors(boolean hasMultipleDoctors) {
    this.hasMultipleDoctors = hasMultipleDoctors;

    return this;
  }

  @SuppressWarnings("unchecked")
  public void mock() {
    if (shouldThrowError) {
      when(component().kolibreeProReminders().shouldShowReminder())
          .thenReturn(Single.error(new Exception("")));
    } else {
      when(component().kolibreeProReminders().shouldShowReminder())
          .thenReturn(Single.just(shouldShowPopup));
    }

    when(component().kolibreeProReminders().hasMultiplePractitionersNeedingConsent())
        .thenReturn(hasMultipleDoctors);

    if (practitionerName != null) {
      when(component().kolibreeProReminders().practitionerName()).thenReturn(practitionerName);

      ProfileWrapper profileWrapper = component().kolibreeConnector().withCurrentProfile();
      doAnswer(
              (Answer<Practitioner[]>)
                  invocation -> {
                    Practitioner practitioner = Mockito.mock(Practitioner.class);
                    when(practitioner.getFirstName()).thenReturn(practitionerName);
                    when(practitioner.getToken()).thenReturn(practitionerToken);
                    Practitioner[] response = new Practitioner[] {practitioner};

                    ((KolibreeConnectorListener) invocation.getArgument(0)).onSuccess(response);
                    return null;
                  })
          .when(profileWrapper)
          .getPractitioners(any(KolibreeConnectorListener.class));
    }

    if (practitionerToken != null) {
      when(component().kolibreeProReminders().practitionerToken()).thenReturn(practitionerToken);
    }
  }

  private EspressoAppComponent component() {
    return (EspressoAppComponent) App.appComponent;
  }
}
