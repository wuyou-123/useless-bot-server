package com.wuyou.robot.game.landlords

import com.wuyou.robot.common.Timer
import com.wuyou.robot.common.logger
import com.wuyou.robot.game.common.interfaces.GameArg
import com.wuyou.robot.game.common.interfaces.Room
import com.wuyou.robot.game.landlords.event.StartGameEvent
import love.forte.simbot.event.FriendMessageEvent
import java.util.concurrent.TimeUnit

/**
 * @author wuyou
 */
class LandlordsRoom(
    override var id: String,
    override var name: String,
    override val game: LandlordsGame,
) : Room<LandlordsGame, LandlordsPlayer, LandlordsRoom>() {
    private val waitTime = 10L
    private var timer: Timer<*>? = null
    override fun onCreate(args: GameArg) {
        send("创建房间{${id}}成功! 当前人数${playerList.size}/${game.maxPlayerCount}, ${waitTime}秒内如果没有玩家加入将解散房间")
        timer = Timer(waitTime, TimeUnit.SECONDS, args, true) {
            onFinish {
                destroyRoom()
            }
        }
    }

    override fun onTryJoin(player: LandlordsPlayer, args: GameArg): Boolean {
        println("玩家 $player 尝试加入房间")
        return true
    }

    override fun onJoin(player: LandlordsPlayer, args: GameArg) {
        if (playerList.size == 1) return
        val p = "加入房间{${id}}成功, "
        val p0 = "玩家${player}加入了房间, "
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
        player.send(mutableListOf(p).apply { addAll(msg) })
        playerList.forEach {
            if (it != player) {
                it.send(mutableListOf(p0).apply { addAll(msg) })
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
        send("房间已满,开始游戏!")
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
