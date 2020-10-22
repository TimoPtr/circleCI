package com.kolibree.sdkws.api.request;

import androidx.annotation.NonNull;
import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.data.JSONModel;
import com.kolibree.sdkws.networking.RequestMethod;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by miguel on 28/07/17.
 *
 * <p>Get Amazon S3 Video data file upload url request
 */
public final class GetVideoUploadUrlRequest extends Request {

  private static final String URL_PARAM = "url";

  /**
   * Requests a url where we can upload a video for the an avro file located at the specified url
   */
  public GetVideoUploadUrlRequest(long accountId, final String avroAmazonUrl) {
    super(RequestMethod.POST, String.format(Locale.US, Constants.SERVICE_UPLOAD_VIDEO, accountId));

    setData(createJsonModel(avroAmazonUrl));
  }

  @NonNull
  private static JSONModel createJsonModel(final String avroAmazonUrl) {
    return new JSONModel() {
      @NonNull
      @Override
      public String toJsonString() throws JSONException {
        final JSONObject json = new JSONObject();

        json.put(URL_PARAM, avroAmazonUrl);

        return json.toString();
      }
    };
  }
}
