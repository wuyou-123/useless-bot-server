package com.wuyou.robot.game.landlords

import com.wuyou.robot.common.Timer
import com.wuyou.robot.common.logger
import com.wuyou.robot.game.common.exception.GameException
import com.wuyou.robot.game.common.interfaces.GameArg
import com.wuyou.robot.game.common.interfaces.Room
import com.wuyou.robot.game.landlords.common.Poker
import com.wuyou.robot.game.landlords.event.StartGameEvent
import love.forte.simbot.event.FriendMessageEvent
import java.util.concurrent.TimeUnit

/**
 * 斗地主房间
 * @author wuyou
 */
class LandlordsRoom(
    override val id: Int,
    override val name: String,
    override val game: LandlordsGame,
) : Room<LandlordsGame, LandlordsPlayer, LandlordsRoom>(id, name) {
    private val waitTime = 30L
    private var timer: Timer<*>? = null

    /**
     * 地主牌
     */
    var landlordsPoker: List<Poker>? = null

    /**
     * 地主玩家
     */
    var landlordsPlayer: LandlordsPlayer? = null

    var currentPlayerIndex = -1
    var currentPlayer: LandlordsPlayer
        get() = playerList[currentPlayerIndex]
        set(value) {
            if (!playerList.contains(value)) throw GameException("$value 不在房间 $id 中")
            currentPlayerIndex = playerList.indexOf(value)
        }
    val otherPlayer: List<LandlordsPlayer>
        get() = playerList.filterIndexed { index, _ -> index != currentPlayerIndex }
    val nextPlayer: LandlordsPlayer
        get() = if (currentPlayerIndex + 1 == playerList.size) playerList[0]
        else playerList[currentPlayerIndex + 1]
    val prePlayer: LandlordsPlayer
        get() = if (currentPlayerIndex == 0) playerList[playerList.size - 1]
        else playerList[currentPlayerIndex - 1]

    /**
     * 发送消息
     */
    override fun send(messages: Any, separator: String) {
        if (currentPlayerIndex == -1) {
            return super.send(messages, separator)
        }
        if (messages is String) {
            playerList.forEach {
                if (it == currentPlayer) it.send(messages.replace("#player", "您"))
                else it.send(messages.replace("#player", currentPlayer.toString()))
            }
        } else if (messages is Collection<*>) {
            val list = mutableListOf<Any>()
            val cList = mutableListOf<Any>()
            messages.forEach {
                if (it is String) {
                    list += it.replace("#player", currentPlayer.toString())
                    cList += it.replace("#player", "您")
                } else if (it != null) {
                    list += it
                    cList += it
                }
            }
            playerList.forEach {
                if (it == currentPlayer) it.send(cList, separator)
                else it.send(list, separator)
            }
        }
    }

    override fun onCreate(args: GameArg) {
        send("创建房间[${id}]成功! 当前人数${playerList.size}/${game.maxPlayerCount}, ${waitTime}秒内如果没有玩家加入将解散房间")
        timer = Timer(waitTime, TimeUnit.SECONDS, args, true) {
            onFinish {
                destroyRoom()
            }
        }
    }

    override fun onTryJoin(player: LandlordsPlayer, args: GameArg): Boolean {
//        println("玩家 $player 尝试加入房间")
        return true
    }

    override fun onJoin(player: LandlordsPlayer, args: GameArg) {
        if (playerList.size == 1) return
        val p = "加入房间[${id}]成功, "
        val p0 = "玩家 $player 加入了房间, "
        val msg = mutableListOf("当前人数${playerList.size}/${game.maxPlayerCount}")
        if (!isFull()) {
            msg += ", ${waitTime}秒内如果没有其他玩家加入将解散房间"
            timer?.cancel()
            timer = Timer(waitTime, TimeUnit.SECONDS, args, true) {
                onFinish {
                    destroyRoom()
                }
            }
        }
        player.send(mutableListOf(p, *msg.toTypedArray()))
        playerList.forEach {
            if (it != player) {
                it.send(mutableListOf(p0, *msg.toTypedArray()))
            }
        }
    }

    override fun beforeDestroy() {
        send("房间已解散")
    }

    override fun onDestroy() {
        println("玩家为空,房间已自动销毁")
    }

    override fun onTryLeave(player: LandlordsPlayer): Boolean {
        println("玩家 $player 尝试离开房间")
        return true
    }

    override fun onLeave(player: LandlordsPlayer) {
        println("玩家 $player 离开了房间")
        send("玩家 $player 离开了房间")
    }

    override fun onPlayerFull() {
        logger { "[${game.name}] 房间${id}玩家已满" }
        timer?.cancel()
        go(StartGameEvent::class)
    }

    override fun onOtherMessage(player: LandlordsPlayer, event: FriendMessageEvent) {
        println("收到其他消息${event.messageContent.plainText}")
    }

    override fun toString(): String {
        return "$id: [$name](${playerList.size}/${game.maxPlayerCount})"
    }

}
