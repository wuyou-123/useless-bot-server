package com.wuyou.robot.game.common.interfaces

import com.wuyou.robot.game.common.gameManager
import love.forte.simbot.event.FriendMessageEvent
import kotlin.reflect.KClass

/**
 * 游戏房间抽象类
 * @author wuyou
 */
abstract class Room<G : Game<G, R, P>, P : Player<G, R, P>, R : Room<G, P, R>>(
    open val id: Int,
    open val name: String
) {


    /**
     * 玩家携带数据
     */
    val playerDataMap: MutableMap<String, MutableMap<String, Any>> = HashMap()

    /**
     * 玩家列表
     */
    val playerList: MutableList<P> = ArrayList()

    /**
     * game实例
     */
    abstract val game: Game<G, R, P>

    /**
     * 判断房间是否已满
     */
    fun isFull(): Boolean = playerList.size >= game.maxPlayerCount

    /**
     * 发送消息
     */
    open fun send(messages: Any, separator: String = "") {
        playerList.forEach {
            it.send(messages, separator)
        }
    }

    /**
     * 执行游戏事件
     */
    @Suppress("UNCHECKED_CAST")
    fun go(
        event: KClass<out GameEvent<G, R, P>>,
        gameArg: GameArg = GameArg()
    ) {
        game.go(event, this as R, gameArg)
    }

    fun destroyRoom() {
        beforeDestroy()
        this.playerList.forEach {
            gameManager().playerList.remove(it)
        }
        onDestroy()
        game.roomList.remove(this)
        gameManager().roomList.remove(this)
    }

    /**
     * 根据QQ号判断玩家是否在房间内
     * @return 玩家是否在房间内
     */
    private fun isInRoom(qq: String): Boolean = getPlayer(qq) != null

    /**
     * 根据QQ号获取玩家对象
     * @return 获取到的玩家对象,如果没有则返回null
     */
    private fun getPlayer(qq: String): P? = playerList.find { it.id == qq }

    /**
     * 创建房间时执行的方法,用于实现类重写, 这时候房间里还没有玩家
     */
    open fun onCreate(args: GameArg) {}

    /**
     * 销毁房间之前执行的方法,用于实现类重写,可以给玩家发送消息
     */
    open fun beforeDestroy() {}

    /**
     * 销毁房间时执行的方法,用于实现类重写
     */
    open fun onDestroy() {}

    /**
     * 尝试加入房间时执行的方法,用于实现类重写
     */
    open fun onTryJoin(player: P, args: GameArg) = true

    /**
     * 加入房间时执行的方法,用于实现类重写,不需要手动添加玩家!
     */
    open fun onJoin(player: P, args: GameArg) {}

    /**
     * 玩家尝试离开房间时执行的方法,用于实现类重写,返回true同意玩家离开
     */
    open fun onTryLeave(player: P) = true

    /**
     * 玩家离开房间时执行的方法,用于实现类重写,不需要手动删除玩家!
     */
    open fun onLeave(player: P) {}

    /**
     * 玩家已满时调用的方法,用于实现类重写
     */
    open fun onPlayerFull() {}

    /**
     * 收到其他消息的处理方法
     */
    open fun onOtherMessage(player: P, event: FriendMessageEvent) {}

    /**
     * 获取房间描述信息
     */
    fun getDesc(qq: String): String = this.toString() + if (isInRoom(qq)) "(你在这里)" else ""

    override fun toString(): String = "$id: ${game.name}[$name](${playerList.size}/${game.maxPlayerCount})"

}
