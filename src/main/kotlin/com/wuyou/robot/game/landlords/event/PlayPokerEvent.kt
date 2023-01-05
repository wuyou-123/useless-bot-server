package com.wuyou.robot.game.landlords.event

import com.wuyou.robot.game.common.interfaces.GameArg
import com.wuyou.robot.game.common.interfaces.GameEvent
import com.wuyou.robot.game.landlords.LandlordsGame
import com.wuyou.robot.game.landlords.LandlordsPlayer
import com.wuyou.robot.game.landlords.LandlordsRoom
import com.wuyou.robot.game.landlords.common.Poker
import com.wuyou.robot.game.landlords.common.PokerUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

/**
 * 出牌事件
 */
@Component
class PlayPokerEvent : GameEvent<LandlordsGame, LandlordsRoom, LandlordsPlayer>() {
    override fun invoke(room: LandlordsRoom, gameArg: GameArg) {
        room.playerList.forEach {
            it.send(listOf("上家: ${it.pre}", "下家: ${it.next}"), "\n")
            if (it == room.currentPlayer) {
                it.send("现在轮到 您 出牌了, 请在30秒内选择您要出的牌!")
            } else {
                it.send("现在轮到 ${room.currentPlayer} 出牌了, 请耐心等待对方选择")
            }
        }
        val msg = mutableListOf<Any>("这是您的牌:")
        msg += PokerUtil.getPoker(room.currentPlayer.pokerList)

        // 建立通道等待定时器结束
        val channel = Channel<List<Poker>>()

        room.currentPlayer.status = "poker"
        room.currentPlayer.send(msg, "\n")
        room.currentPlayer.pokerTimer(gameArg) { timer ->
            timer.onFinish {
                // 超时没出牌
                room.currentPlayer.send("等待超时,自动出牌!")
                runBlocking {
                    channel.send(listOf(room.currentPlayer.pokerList[0]))
                }
            }
            timer.onCancel {
                // 定时器被取消,表示已经出牌
                @Suppress("UNCHECKED_CAST") runBlocking {
                    channel.send(it["pokerList"] as List<Poker>)
                }
            }
        }
        val pokerList = runBlocking { channel.receive() }
        room.lastPlayPlayer = room.currentPlayer
        room.currentPlayer.pokerList.removeAll(pokerList)
        room.send(listOf("#player 出牌: ", PokerUtil.getPoker(pokerList)), "\n")
        room.currentPlayer = room.currentPlayer.next
        room.go(PlayPokerEvent::class, gameArg)
    }
}
