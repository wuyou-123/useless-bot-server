package com.wuyou.robot.annotation

import com.wuyou.robot.enums.RobotPermission
import love.forte.simboot.annotation.ContentTrim
import love.forte.simboot.annotation.Listener
import love.forte.simbot.PriorityConstant
import love.forte.simbot.event.GroupMessageEvent
import org.springframework.core.annotation.AliasFor

/**
 * @author wuyou
 */
@Suppress("OPT_IN_USAGE", "unused")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Listener
@ContentTrim
annotation class RobotListen(
    /**
     * 描述信息
     */
    val desc: String = "",
    /**
     * 执行监听器所需的权限
     */
    val permission: RobotPermission = RobotPermission.MEMBER,
    /**
     * 没有权限时的提示信息
     */
    val noPermissionTip: String = "操作失败,您没有权限",
    /**
     * 此事件的优先级。
     */
    @get:AliasFor(attribute = "priority", annotation = Listener::class) val priority: Int = PriorityConstant.NORMAL,

    @get:AliasFor(attribute = "id", annotation = Listener::class) val id: String = "",

    /**
     * 是否在当前群开机的时候执行,仅当监听类型是[GroupMessageEvent]时有效
     */
    val isBoot: Boolean = false,

    val commandKey: String = "",
    val commandDefault: String = "",

    )
