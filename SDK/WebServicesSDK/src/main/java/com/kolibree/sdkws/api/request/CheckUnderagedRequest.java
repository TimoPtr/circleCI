package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.networking.RequestMethod;
import java.util.Locale;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

public class CheckUnderagedRequest extends Request {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public CheckUnderagedRequest(LocalDate birthday, String country) {
    super(
        RequestMethod.GET,
        String.format(
            Locale.US,
            Constants.SERVICE_CHECK_UNDERAGED,
            DATE_FORMATTER.format(birthday),
            country.toLowerCase()));
  }
}
