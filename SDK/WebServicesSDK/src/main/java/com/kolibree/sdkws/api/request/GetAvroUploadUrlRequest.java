package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.networking.RequestMethod;
import java.util.Locale;

/**
 * Created by aurelien on 19/07/17.
 *
 * <p>Get Amazon S3 AVRO data file upload url request
 */
public final class GetAvroUploadUrlRequest extends Request {
  public GetAvroUploadUrlRequest(long accountId) {
    super(
        RequestMethod.POST, String.format(Locale.US, Constants.SERVICE_UPLOAD_RAW_DATA, accountId));
  }
}
