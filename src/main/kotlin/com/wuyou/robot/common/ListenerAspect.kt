package com.wuyou.robot.common

import com.wuyou.robot.annotation.RobotListen
import com.wuyou.robot.config.SshConfiguration
import com.wuyou.robot.enums.RobotPermission
import kotlinx.coroutines.runBlocking
import love.forte.simbot.event.Event
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.utils.item.toList
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

/**
 * 拦截监听器
 * @author wuyou
 */

@Component
@Aspect
class ListenerAspect(val sshConfiguration: SshConfiguration) {

    /**
     * 拦截监听器方法
     */
    @Around("@annotation(com.wuyou.robot.annotation.RobotListen) && @annotation(annotation))")
    fun ProceedingJoinPoint.doAroundAdvice(annotation: RobotListen): Any? {
        val start = System.currentTimeMillis()
        val event = args.find { it is Event } ?: return proceed()

        fun proceedFailed(tip: String, group: String) {
            logger {
                -"群${group}执行监听器${signature.name}(${annotation.desc})失败, $tip"
                -("执行拦截器耗时: " + (System.currentTimeMillis() - start))
            }
            return
        }

        if (event is GroupMessageEvent) {
            val group = runBlocking { event.group() }
            val author = runBlocking { event.author() }
            val role = runBlocking { author.roles.toList()[0] }
            // 判断是否开机
            if (annotation.isBoot && !RobotCore.BOOT_MAP.getOrDefault(group.id.toString(), false)) {
                if (signature.name == "down") {
                    return null
                }
                return proceedFailed("当前群未开机", group.id.toString())
            }
            // 判断是否过滤
            if (annotation.commandKey.isNotBlank() &&
                getCommand(
                    group.id.toString(), annotation.commandKey, annotation.commandDefault
                ) != event.messageContent.plainText
            ) {
                return null
            }
            // 判断是否有权限
            if (
                annotation.permission != RobotPermission.MEMBER &&
                annotation.permission > role && !isBotAdministrator(author.id.toString())
            ) {
                if (annotation.noPermissionTip.isNotBlank()) {
                    Sender.sendGroupMsg(group, annotation.noPermissionTip)
                }
                return proceedFailed("权限不足", group.id.toString())
            }
            logger {
                -"群${group.id}执行了监听器${signature.name}(${annotation.desc})"
                -("执行拦截器耗时: " + (System.currentTimeMillis() - start))
            }
            sshConfiguration.reconnect()
            return proceed()
        }

        logger {
            -"执行了监听器${signature.name}(${annotation.desc})"
            -("执行拦截器耗时: " + (System.currentTimeMillis() - start))
        }
        sshConfiguration.reconnect()
        return proceed()
    }
}

