package com.kolibree.sdkws.api.request;

import static com.kolibree.android.network.NetworkConstants.SERVICE_REQUEST_TOKEN_V2;

import androidx.annotation.NonNull;
import com.kolibree.sdkws.data.model.MagicLinkData;
import com.kolibree.sdkws.networking.RequestMethod;

/** Magic link login request with validated code */
public class MagicLinkLoginRequest extends Request {

  public MagicLinkLoginRequest(@NonNull String code) {
    super(RequestMethod.POST, SERVICE_REQUEST_TOKEN_V2);
    setData(new MagicLinkData(code));
  }
}
