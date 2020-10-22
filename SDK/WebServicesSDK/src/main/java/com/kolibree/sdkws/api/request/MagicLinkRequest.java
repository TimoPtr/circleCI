package com.kolibree.sdkws.api.request;

import androidx.annotation.NonNull;
import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.data.model.EmailData;
import com.kolibree.sdkws.networking.RequestMethod;

/** Magic link email request */
public class MagicLinkRequest extends Request {

  public MagicLinkRequest(@NonNull String email) {
    super(RequestMethod.POST, Constants.SERVICE_MAGIC_LINK_REQUEST);
    setData(new EmailData(email));
  }
}
