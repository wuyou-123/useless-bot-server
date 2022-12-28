package com.wuyou.robot.game.common

import com.wuyou.robot.common.*
import com.wuyou.robot.game.common.exception.GameException
import com.wuyou.robot.game.common.interfaces.Game
import com.wuyou.robot.game.common.interfaces.GameArg
import com.wuyou.robot.game.common.interfaces.Player
import com.wuyou.robot.game.common.interfaces.Room
import com.wuyou.robot.util.MessageUtil.authorId
import kotlinx.coroutines.runBlocking
import love.forte.simbot.definition.Friend
import love.forte.simbot.event.FriendMessageEvent
import org.springframework.stereotype.Component
import java.lang.reflect.ParameterizedType
import javax.annotation.PostConstruct
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * @author wuyou
 */
@Suppress("UNCHECKED_CAST")
@Component
class GameManager<G : Game<G, R, P>, R : Room<G, P, R>, P : Player<G, R, P>> {
    val gameSet = HashSet<G>()
    val gameNameMap = mutableMapOf<String, G>()
    val roomList = mutableListOf<R>()
    val playerList = mutableListOf<P>()

    @PostConstruct
    fun init() {
        RobotCore.applicationContext.getBeansOfType(Game::class.java).let { map ->
            val nameList = mutableListOf<String>()
            map.forEach {
                logger { "registering game ${it.value.name}(${it.value.id})..." }
                (it.value as G).let { game ->
                    gameSet += game
                    gameNameMap[game.name]?.let {
                        throw GameException("已存在游戏名为${game.name}的游戏!")
                    }
                    if (nameList.contains(game.id)) {
                        throw GameException("已存在id为${game.id}的游戏!")
                    }
                    nameList += game.id
                    gameNameMap[game.name] = game
                    if (game.minPlayerCount > game.maxPlayerCount) {
                        throw GameException("maxPlayerCount(${game.maxPlayerCount}) must greater than minPlayerCount(${game.minPlayerCount})")
                    }
                    if (game.minPlayerCount <= 0 || game.maxPlayerCount <= 0) {
                        throw GameException("maxPlayerCount(${game.maxPlayerCount}) and minPlayerCount(${game.minPlayerCount}) must greater than 1")
                    }
                    game.copyResources()
                    game.load()
                }
                logger { "register game ${it.value.name}(${it.value.id}) success!" }
            }
        }
    }

    /**
     * 根据游戏名创建房间
     * @param name 游戏名
     * @param event 消息事件
     */
    suspend fun createRoomByGameName(
        event: FriendMessageEvent,
        name: String,
    ): R {
        return synchronized(playerList) {
            runBlocking {
                getPlayerByEvent(event)?.let {
                    throw GameException("你已经在房间[${it.room.name}]中了", event)
                }
                val game = getGameByName(name) ?: throw GameException("没有找到游戏$name", event)
                return@runBlocking instanceRoom(game, event)?.also { room ->
                    val args = GameArg(event)
                    getInstance().roomList += room
                    game.roomList += room
                    instancePlayer(room, event.friend())?.also { player ->
                        if (!getInstance().playerList.contains(player)) {
                            getInstance().playerList += player
                        }
                        room.playerList += player
                        room.onCreate(args)
                        logger { "[${game.name}] 玩家${event.authorId()}创建了房间${room.id}" }
                        room.onJoin(player, args)
                        logger { "[${game.name}] 玩家${event.authorId()}加入了房间${room.id}" }
                    } ?: throw GameException("初始化玩家失败!", event)
                } ?: throw GameException("初始化房间失败!", event)
            }
        }
    }

    /**
     * 根据编号加入房间
     * @param event 消息事件
     * @param num 房间编号
     */
    suspend fun joinRoomById(
        event: FriendMessageEvent,
        num: Int
    ) {
        getPlayerByEvent(event)?.let {
            throw GameException("你已经在房间[${it.room.name}]中了", event)
        }
        val room = getRoomById(num) ?: throw GameException("没有找到编号为${num}的房间", event)
        val player = instancePlayer(room, event.friend()) ?: throw GameException("初始化玩家失败!", event)
        getInstance().playerList += player
        room.playerList += player
        room.onJoin(player, GameArg(event))
        logger { "[${room.game.name}] 玩家${event.authorId()}加入了房间${room.id}" }
        if (room.isFull()) {
            room.onPlayerFull()
        }
    }

    /**
     * 离开房间
     */
    suspend fun leaveRoom(event: FriendMessageEvent) {
        val player = getPlayerByEvent(event) ?: return
        val room = player.room
        if (room.onTryLeave(player)) {
            room.onLeave(player)
            if (room.playerList.size == 1) {
                room.beforeDestroy()
                room.playerList.remove(player)
                room.onDestroy()
                room.game.roomList.remove(room)
                getInstance().roomList.remove(room)
            } else {
                room.playerList.remove(player)
            }
            getInstance().playerList.remove(player)
        }
    }

    /**
     * 根据游戏名获取游戏对象
     */
    fun getGameByName(name: String): G? = getInstance().gameNameMap[name]

    /**
     * 根据游戏名获取房间列表
     */
    fun getRoomListByGame(name: String): List<R>? {
        val list =
            if (name.isBlank()) getInstance().roomList else getInstance().roomList.filter { it.game.name == name }
        return list.let { it.ifEmpty { null } }
    }

    /**
     * 根据房间id获取房间
     */
    fun getRoomById(num: Int): R? {
        getInstance().roomList.find { it.id == num.toString() }?.let {
            return it
        }
        return null
    }

    suspend fun getPlayerByEvent(event: FriendMessageEvent): P? {
        getInstance().playerList.find { it.id == event.authorId() }?.let {
            return it
        }
        return null
    }

    /**
     * 实例化房间
     */
    private suspend fun instanceRoom(
        game: G,
        event: FriendMessageEvent,
    ): R? {
        try {
            @Suppress("UNCHECKED_CAST") val room =
                (game.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.find {
                    (it as Class<*>).superclass == Room::class.java
                } as Class<R>
            room.kotlin.constructors.find { it.parameters.size == 3 }?.let {
                return it.call(
                    "" + Random.nextInt(100000..999999),
                    "${event.friend().username}的房间(${game.name})",
                    game
                )
            }
        } catch (e: Exception) {
            Sender.sendPrivateMsg(RobotCore.ADMINISTRATOR[0], e.message ?: e.stackTraceToString())
        }
        return null
    }


    /**
     * 实例化玩家
     */
    private fun instancePlayer(
        room: R,
        qq: Friend,
    ): P? {
        try {
            @Suppress("UNCHECKED_CAST") val player =
                (room.game.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.find {
                    (it as Class<*>).superclass == Player::class.java
                } as Class<P>
            player.kotlin.constructors.find { it.parameters.size == 3 }?.let {
                return it.call(qq.id.toString(), qq.username, room)
            }
        } catch (e: Exception) {
            Sender.sendPrivateMsg(RobotCore.ADMINISTRATOR[0], e.message ?: e.stackTraceToString())
        }
        return null
    }

    private fun getInstance(): GameManager<G, R, P> = instance as GameManager<G, R, P>

    companion object {
        @get:JvmName("instance")
        private val instance by lazy {
            getBean(GameManager::class.java)
        }

        fun getInstance() = instance.getInstance()
    }
}

fun gameManager() = GameManager.getInstance()
