package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.networking.RequestMethod;
import java.util.Locale;

/**
 * Created by aurelien on 19/07/17.
 *
 * <p>Get Amazon S3 AVRO data file upload url request
 */
public final class GetInstructionsRequest extends Request {

  public GetInstructionsRequest(long accountId) {
    super(
        RequestMethod.GET,
        String.format(Locale.US, Constants.SERVICE_READ_INSTRUCTIONS, accountId));
  }
}
