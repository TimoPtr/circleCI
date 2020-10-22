package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;

import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.utils.EmailVerifier;
import com.kolibree.sdkws.utils.ProfileUtils;
import dagger.Module;
import dagger.Provides;
import java.util.regex.Pattern;

@Module
public abstract class EspressoUtilsModule {

  @Provides
  @AppScope
  static ProfileUtils providesProfileUtils() {
    return mock(ProfileUtils.class);
  }

  @Provides
  @AppScope
  static EmailVerifier providesEmailVerifier() {
    return new EmailVerifier() {

      private Pattern EMAIL_ADDRESS =
          Pattern.compile(
              "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@"
                  + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}"
                  + "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");

      @Override
      public Pattern getEmailAddressPattern() {
        return EMAIL_ADDRESS;
      }
    };
  }
}
