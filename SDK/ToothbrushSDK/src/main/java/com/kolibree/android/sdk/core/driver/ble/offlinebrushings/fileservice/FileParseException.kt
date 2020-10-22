package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

internal open class FileParseException(message: String) : Exception(message)

internal class FileHeaderParseException(message: String) : FileParseException(message)
internal class FileCrcParseException(message: String) : FileParseException(message)
internal class FileInvalidCrcException(message: String) : FileParseException(message)
internal class BrushingHeaderParseException(message: String) : FileParseException(message)
internal class BrushingSampleRecordParseException(message: String) : FileParseException(message)
internal class BrushingSampleParseException(message: String) : FileParseException(message)
