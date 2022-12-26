package com.wuyou.robot.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel

/**
 * 打印日志
 * @author wuyou
 */

val loggerMap = mutableMapOf<String, Logger>()
fun (Log.() -> Any).getLogger() =
    getName().let { loggerMap.getOrPut(it) { LoggerFactory.getLogger(it) } }

fun logger(block: Log.() -> Any) {
    val logger = block.getLogger()
    Log().apply {
        with(block()) {
            if (this !is Unit) logs += this
        }
    }.logs.forEach {
        logger.info(it.toString())
    }
}


fun logger(level: LogLevel, block: Log.() -> Any) {
    val logger = block.getLogger()
    Log().apply {
        with(block()) {
            if (this !is Unit) logs += this
        }
    }.logs.forEach {
        when (level) {
            LogLevel.TRACE -> logger.trace(it.toString())
            LogLevel.DEBUG -> logger.debug(it.toString())
            LogLevel.INFO -> logger.info(it.toString())
            LogLevel.WARN -> logger.warn(it.toString())
            LogLevel.ERROR -> logger.error(it.toString())
            else -> {}
        }
    }
}

fun logger(level: LogLevel, e: Throwable, block: Log.() -> Any = {}) {
    val logger = block.getLogger()
    Log().apply {
        with(block()) {
            if (this !is Unit) logs += this
        }
    }.logs.forEach {
        when (level) {
            LogLevel.TRACE -> logger.trace(it.toString(), e)
            LogLevel.DEBUG -> logger.debug(it.toString(), e)
            LogLevel.INFO -> logger.info(it.toString(), e)
            LogLevel.WARN -> logger.warn(it.toString(), e)
            LogLevel.ERROR -> logger.error(it.toString(), e)
            else -> {}
        }
    }
}

class Log {
    val logs = mutableListOf<Any>()
    operator fun String.unaryMinus() {
        logs += this
    }
}

private fun (Log.() -> Any).getName(): String {
    val name = this.javaClass.name
    return when {
        name.contains("Kt$") -> name.substringBefore("Kt$")
        name.contains("$") -> name.substringBefore("$")
        else -> name
    }
}
