package com.kolibree.sdkws.api.request;

import static com.kolibree.android.network.NetworkConstants.SERVICE_REQUEST_TOKEN_V2;

import androidx.annotation.NonNull;
import com.kolibree.sdkws.data.model.EmailData;
import com.kolibree.sdkws.networking.RequestMethod;

/**
 * Request token request to check if the provided email matches with an existing password-enabled
 * account or not
 */
public class CheckAccountHasPasswordRequest extends Request {

  public CheckAccountHasPasswordRequest(@NonNull String email) {
    super(RequestMethod.POST, SERVICE_REQUEST_TOKEN_V2);
    setData(new EmailData(email));
  }
}
