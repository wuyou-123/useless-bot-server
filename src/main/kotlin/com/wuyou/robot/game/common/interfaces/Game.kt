package com.wuyou.robot.game.common.interfaces

import com.wuyou.robot.common.RobotCore
import com.wuyou.robot.common.isNull
import com.wuyou.robot.common.send
import com.wuyou.robot.exception.RobotException
import love.forte.simbot.event.FriendMessageEvent
import org.springframework.core.io.ClassPathResource
import java.io.File
import kotlin.reflect.KClass

/**
 * @author wuyou
 */
abstract class Game<G : Game<G, R, P>, R : Room<G, P, R>, P : Player<G, R, P>> {

    /**
     * 游戏id,只能存在一个,不建议使用中文
     */
    abstract val id: String

    /**
     * 游戏名
     */
    abstract val name: String

    /**
     * 事件Map<[GameEvent],List<[GameEvent]>>
     */
    val eventMap: EventMap<G, R, P> = EventMap()

    /**
     * 等待玩家列表
     * 当调用[GameEvent.sendPlayerAndWaitPlayerNext]时,会等待该玩家的下一条消息,此时不执行事件监听器
     */
    val waitPlayerList: MutableList<P> = mutableListOf()

    /**
     * 该游戏的所有房间列表
     */
    val roomList: MutableList<R> = mutableListOf()

    /**
     * 最少成员数
     */
    abstract val minPlayerCount: Int

    /**
     * 最大成员数
     */
    abstract val maxPlayerCount: Int

    open val gameArgs: List<String> = listOf()

    /**
     * 事件流转,执行目标事件
     */
    fun go(event: KClass<out GameEvent<G, R, P>>, room: R, gameArg: GameArg = GameArg()) {
        val list = eventMap.values.reduce { acc, gameEvents -> gameEvents.also { it.addAll(acc) } }
        list.find { it::class == event }?.also { it.invoke(room, gameArg) }.isNull {
            throw RobotException("game event is not found!")
        }
    }

    /**
     * 载入游戏时执行的事件
     */
    open fun load() {}

    /**
     * 复制当前游戏资源文件夹到缓存目录
     */
    fun copyResources(): Boolean {
        this::class.java.classLoader.getResourceAsStream("game" + File.separator + id)
        try {
            val resource = ClassPathResource(File.separator + "game" + File.separator + id)
            if (!resource.exists()) {
                return false
            }
            resource.file.copyRecursively(File(getTempPath()), true)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 获取当前游戏的临时路径
     */
    fun getTempPath(): String {
        return RobotCore.TEMP_PATH + id + File.separator
    }
}

/**
 * 事件Map
 */
class EventMap<G : Game<G, R, P>, R : Room<G, P, R>, P : Player<G, R, P>> :
    HashMap<GameStatus, MutableList<GameEvent<G, R, P>>>() {
    override operator fun get(key: GameStatus): MutableList<GameEvent<G, R, P>>? = super.get(key).isNull {
        return keys.find { it.status == key.status }?.let { super.get(it) }
    }
}

/**
 * 游戏中的传递参数
 */
class GameArg() {
    val map: MutableMap<String, Any> = mutableMapOf()
    val event by lazy { map["event"] as FriendMessageEvent? }

    constructor(event: FriendMessageEvent, map: MutableMap<String, Any> = mutableMapOf()) : this() {
        this.map.putAll(map)
        this.map["event"] = event
    }

    operator fun get(key: String) = map[key]
    operator fun set(key: String, arg: Any) {
        map[key] = arg
    }

    fun send(messages: Any, separator: String = "") = event?.send(messages, separator)
}
