@file:Suppress("MemberVisibilityCanBePrivate")

package com.wuyou.robot.common

import com.wuyou.robot.entity.GroupBootStateService
import love.forte.simboot.spring.autoconfigure.EnableSimbot
import love.forte.simbot.bot.Bot
import love.forte.simbot.bot.OriginBotManager
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.DependsOn
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.File
import java.io.File.separator
import java.util.*
import javax.annotation.PostConstruct

/**
 * @author wuyou
 */
@Suppress("unused")
@Order(1)
@Component
@EnableSimbot
@DependsOn("startUpHandlerImpl")
class RobotCore(
    private val groupBootStateService: GroupBootStateService,
    private var applicationContext: ApplicationContext,
    private val accountManager: AccountManager,
) {

    @PostConstruct
    fun init() {
        setApplicationContext()
        initGroupBootMap()
    }

    @Synchronized
    private fun setApplicationContext() {
        robotCore = this
        RobotCore.applicationContext = applicationContext
    }

    private fun initGroupBootMap() {
        logger { "Start initGroupBootMap..." }
        val list = groupBootStateService.list()
        list.forEach {
            BOOT_MAP[it.groupCode] = it.state
        }
    }

    val separator = File.separator!!

    companion object {
        lateinit var applicationContext: ApplicationContext

        /**
         * 项目名
         */
        const val PROJECT_NAME: String = "useless-bot"

        /**
         * 项目路径
         */
        val PROJECT_PATH: String = System.getProperty("user.dir") + separator

        /**
         * 临时路径
         */
        val TEMP_PATH: String = "$PROJECT_PATH$PROJECT_NAME${separator}TEMP$separator"

        /**
         * 机器人管理员
         */
        val ADMINISTRATOR: List<String> = listOf("1097810498")

        /**
         * 缓存群开关
         */
        val BOOT_MAP: MutableMap<String?, Boolean> = HashMap()

        var robotCore: RobotCore? = null
    }
}

fun isBotAdministrator(accountCode: String): Boolean {
    return RobotCore.ADMINISTRATOR.contains(accountCode)
}

@Suppress("OPT_IN_USAGE")
fun getBot(): Bot {
    return OriginBotManager.getAnyBot()
}

fun <T> getBean(type: Class<T>): T {
    return RobotCore.applicationContext.getBean(type)
}

inline fun <T> T.isNull(block: () -> Unit): T {
    if (this == null) block()
    return this
}

inline fun Boolean.then(block: () -> Unit) = this.also { if (this) block() }

inline operator fun Boolean.invoke(block: () -> Unit) = this.then(block)

inline fun Boolean?.onElse(block: () -> Unit): Boolean = this.let {
    it?.not()?.then(block).isNull { block() }
    it ?: false
}

inline operator fun Boolean?.minus(block: () -> Unit): Boolean = this.onElse(block)

fun String.substring(prefix: String, suffix: String): String = substringOrElse(prefix, suffix, "")

fun String.substringOrNull(prefix: String, suffix: String): String? =
    substringOrElse(prefix, suffix, "").ifBlank { null }

fun String.substringOrElse(prefix: String, suffix: String, default: String): String =
    Regex("$prefix([\\s\\S]*)$suffix").find(this)?.groups?.let {
        if (it.size > 1) it[1]?.value?.ifBlank { default } else null
    } ?: default

fun String.substring(prefix: String, suffix: String, default: String): String =
    this.substringBefore(prefix).substringAfter(suffix).ifBlank { default }

fun Any.getFieldValue(fieldName: String): Any {
    return javaClass.getDeclaredField(fieldName).let {
        it.isAccessible = true
        it.get(this)
    }
}

fun Any.invoke(methodName: String, vararg args: Any): Any {
    return javaClass.getDeclaredMethod(methodName).let {
        it.isAccessible = true
        if (it.parameterCount > 0) it.invoke(this, args)
        else it.invoke(this)
    }
}
