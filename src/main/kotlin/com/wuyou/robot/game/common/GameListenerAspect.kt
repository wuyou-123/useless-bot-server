package com.wuyou.robot.game.common

import com.wuyou.robot.annotation.RobotListen
import com.wuyou.robot.common.logger
import com.wuyou.robot.common.send
import com.wuyou.robot.game.common.exception.GameException
import love.forte.simbot.event.MessageEvent
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.boot.logging.LogLevel
import org.springframework.stereotype.Component

/**
 * 游戏监听器拦截,用于捕获异常
 * @author wuyou
 */

@Component
@Aspect
class GameListenerAspect {

    /**
     * 拦截监听器方法
     */
    @Around("execution(* com.wuyou.robot.game..*(..)) && @annotation(com.wuyou.robot.annotation.RobotListen) && @annotation(annotation))")
    fun ProceedingJoinPoint.doAroundAdvice(annotation: RobotListen): Any? {
        return try {
            proceed()
        } catch (e: GameException) {
            logger(LogLevel.ERROR) {
                e.message
            }
            e.event?.let {
                if (it is MessageEvent) {
                    it.send(e.message)
                }
            }
            //            throw e
            null
        }
    }
}

