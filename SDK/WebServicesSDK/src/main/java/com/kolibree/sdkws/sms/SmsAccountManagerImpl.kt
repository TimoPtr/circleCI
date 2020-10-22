package com.kolibree.sdkws.sms

import com.kolibree.sdkws.core.IKolibreeConnector
import javax.inject.Inject

class SmsAccountManagerImpl
@Inject constructor(private val connector: IKolibreeConnector) : SmsAccountManager by connector
