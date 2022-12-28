package com.wuyou.robot.game.common.interfaces

import com.wuyou.robot.common.Sender
import com.wuyou.robot.common.getBean
import com.wuyou.robot.common.logger
import love.forte.simbot.event.ContinuousSessionEventMatcher
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.MessageContent
import org.springframework.boot.logging.LogLevel
import java.lang.reflect.ParameterizedType
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

/**
 * @author wuyou
 */
abstract class GameEvent<G : Game<G, R, P>, R : Room<G, P, R>, P : Player<G, R, P>> {
    /**
     * 匹配方法
     */
    open val matcher: GameEventMatcher = GameEventMatcher { _, _ -> false }

    /**
     * 事件方法
     */
    abstract fun invoke(room: R, gameArg: GameArg)

    /**
     * 绑定的游戏状态
     */
    open fun getStatus() = GameStatus("")

    @PostConstruct
    fun init() {
        @Suppress("UNCHECKED_CAST") val game =
            getBean((this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.find {
                (it as Class<*>).superclass == Game::class.java
            } as Class<G>)
        when (val gameEvents = game.eventMap[getStatus()]) {
            null -> game.eventMap[getStatus()] = mutableListOf(this)
            else -> gameEvents += this
        }
        logger(LogLevel.DEBUG) { "init game event [${game.name}] ${this@GameEvent::class.simpleName}" }
    }

    /**
     * 向房间内发送消息并等待玩家的下一条消息
     * @param player 玩家对象
     * @param message 消息内容
     * @param timeout 超时时间,单位[timeUnit]
     * @param timeUnit 超时时间单位
     * @param eventMatcher 收到消息时的匹配方法,只返回匹配通过时的消息
     */
    open suspend fun sendRoomAndWaitPlayerNext(
        player: P,
        message: String,
        timeout: Long = 1,
        timeUnit: TimeUnit = TimeUnit.MINUTES,
        eventMatcher: ContinuousSessionEventMatcher<MessageEvent> = ContinuousSessionEventMatcher,
    ): MessageContent? {
        player.room.game.waitPlayerList += player
        return Sender.sendGroupAndWait(player.room.id, player.id, message, "", timeout, timeUnit, eventMatcher).also {
            player.room.game.waitPlayerList.remove(player)
        }
    }

//    /**
//     * 发送消息
//     */
//    open suspend fun send(messages: Any, separator: String = "") = Sender.sendGroupMsg(room.id, message)

}

/**
 * 游戏状态
 */
class GameStatus(val status: String) {
    override fun toString(): String {
        return status
    }
}

/**
 * 事件匹配器
 */
fun interface GameEventMatcher {
    /**
     * 执行匹配方法
     * @param msg 收到的消息
     * @param gameArg 游戏参数
     */
    suspend operator fun invoke(msg: MessageContent, gameArg: GameArg): Boolean
}
