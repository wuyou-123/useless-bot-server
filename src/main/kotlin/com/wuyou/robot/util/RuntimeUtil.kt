package com.wuyou.robot.util

import com.wuyou.robot.common.logger
import org.springframework.boot.logging.LogLevel

/**
 * 命令行工具类
 *
 * @author wuyou
 */
object RuntimeUtil {

    private fun exec(args: List<String>): List<String> {
        val list = mutableListOf<String>()
        val proc = Runtime.getRuntime().exec(args.toTypedArray())
        proc.waitFor()
        proc.inputStream.reader().readLines().apply { list.addAll(this) }.apply {
            if (isNotEmpty()) {
                forEach { line ->
                    logger { line }
                }
            }
        }
        proc.errorStream.reader().readText().apply {
            if (isNotEmpty()) {
                logger(LogLevel.ERROR) { this }
            }
        }
        return list
    }

    fun exec(cmd: String, endArgs: Collection<String>, vararg args: String): List<String> {
        val list = mutableListOf<String>()
        list.add(cmd)
        list.addAll(listOf(*args))
        list.addAll(endArgs)
        return exec(list)
    }
}
