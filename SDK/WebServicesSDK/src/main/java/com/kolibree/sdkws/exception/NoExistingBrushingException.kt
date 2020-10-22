package com.kolibree.sdkws.exception

data class NoExistingBrushingException(val profileId: Long) :
    Throwable("The user with the profile $profileId does not have any brushing")
