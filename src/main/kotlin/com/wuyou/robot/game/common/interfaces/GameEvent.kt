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
     * 向玩家发送消息并等待玩家的下一条消息
     * @param messages 消息内容
     * @param separator [messages]是数组或列表时的消息分隔符
     * @param timeout 超时时间,单位[timeUnit]
     * @param timeUnit 超时时间单位
     * @param eventMatcher 收到消息时的匹配方法,只返回匹配通过时的消息
     */
    open suspend fun P.sendAndWaitNext(
        messages: Any,
        separator: String = "",
        timeout: Long = 30,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        eventMatcher: ContinuousSessionEventMatcher<MessageEvent> = ContinuousSessionEventMatcher,
    ): MessageContent? {
        room.game.waitPlayerList += this
        return Sender.sendPrivateAndWait(id, messages, separator, timeout, timeUnit, eventMatcher)
            .also { room.game.waitPlayerList.remove(this) }
    }

//    /**
//     * 发送消息
//     */
//    open suspend fun send(messages: Any, separator: String = "") = Sender.sendGroupMsg(room.id, message)
}

/**
 * 游戏状态
 */
data class GameStatus(val status: String) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other is GameStatus) return status == other.status
        if (other is String) return status == other
        return false
    }

    override fun hashCode(): Int {
        return status.hashCode()
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
