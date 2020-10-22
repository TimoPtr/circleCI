package no.nordicsemi.android.dfu

import java.util.UUID

/*
See SecureDfuImpl.DEFAULT_DFU_SERVICE_UUID

For some reason, we can't access it after proguarding, even tho there's a rule to keep everything.

This value hasn't changed in 3 years, so I assume it's fixed
 */
@SuppressWarnings("MagicNumber")
val dfuServiceUUID = UUID(0x0000FE5900001000L, -0x7fffff7fa064cb05L)
