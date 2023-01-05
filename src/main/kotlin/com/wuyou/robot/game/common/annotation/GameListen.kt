package com.wuyou.robot.game.common.annotation

import com.wuyou.robot.annotation.RobotListen
import org.springframework.core.annotation.AliasFor

/**
 * 游戏事件监听器
 * @author wuyou
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@RobotListen(isBoot = true)
annotation class GameListen(
    /**
     * 描述信息
     */
    @get:AliasFor(attribute = "desc", annotation = RobotListen::class) val desc: String = "",
)
