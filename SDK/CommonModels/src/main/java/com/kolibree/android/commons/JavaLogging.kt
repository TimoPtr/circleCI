/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.commons

/**
 * Object to centralize logging from pure Java modules
 */
object JavaLogging : JavaLogger {
    override fun error(message: String) {
        logger.error(message)
    }

    override fun error(throwable: Throwable?, message: String) {
        logger.error(throwable, message)
    }

    override fun warning(message: String) {
        logger.warning(message)
    }

    override fun debug(message: String) {
        logger.debug(message)
    }

    private var logger: JavaLogger = NoOpJavaLogger()

    /**
     * Plants a javaLogger, used for java logging
     */
    fun plant(javaLogger: JavaLogger) {
        this.logger = javaLogger
    }
}

/**
 * Interface to allow logging from pure java modules
 *
 * It's not an exhaustive logging interface, it should be expanded as we find the need
 */
interface JavaLogger {
    fun warning(message: String)
    fun debug(message: String)
    fun error(throwable: Throwable?, message: String)
    fun error(message: String)
}

private class NoOpJavaLogger : JavaLogger {

    override fun warning(message: String) {
        // no-op
    }

    override fun debug(message: String) {
        // no-op
    }

    override fun error(throwable: Throwable?, message: String) {
        // no-op
    }

    override fun error(message: String) {
        // no-op
    }
}
