package com.wuyou.robot.game.landlords.event

import com.wuyou.robot.game.common.interfaces.GameArg
import com.wuyou.robot.game.common.interfaces.GameEvent
import com.wuyou.robot.game.landlords.LandlordsGame
import com.wuyou.robot.game.landlords.LandlordsPlayer
import com.wuyou.robot.game.landlords.LandlordsRoom
import com.wuyou.robot.game.landlords.common.MessageCmd
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

/**
 * 抢地主事件
 */
@Component
class CallLandlordsEvent : GameEvent<LandlordsGame, LandlordsRoom, LandlordsPlayer>() {
    override fun invoke(room: LandlordsRoom, gameArg: GameArg) {
        val currentPlayer = room.currentPlayer
        room.otherPlayer.forEach {
            it.send("现在轮到 $currentPlayer 抢地主了, 请耐心等待对方选择")
        }
        val message = runBlocking {
            currentPlayer.sendAndWaitNext(
                "现在轮到你了, 请在30秒内确认你是否抢地主?",
            ) {
                mutableListOf<String>().apply {
                    addAll(MessageCmd.CALL_LANDLORDS_CMD_LIST)
                    addAll(MessageCmd.NOT_CALL_LANDLORDS_CMD_LIST)
                }.contains(it.messageContent.plainText)
            }
        }
        val isFinal = room.playerList.filter { it.calledLandlords != null }.size == 3
        if (message == null || MessageCmd.NOT_CALL_LANDLORDS_CMD_LIST.contains(message.plainText)) {
            // 超时或者不抢
            room.send((if (message == null) "等待超时! " else "") + "#player 没有抢地主")
            currentPlayer.calledLandlords = false
        } else {
            room.send("#player 选择了抢地主")
            currentPlayer.calledLandlords = true
            room.landlordsPlayer = currentPlayer
        }
        // 想成为地主的列表
        val wantList = room.playerList.filter { it.calledLandlords == true }
        // 不想成为地主的列表
        val notWantList = room.playerList.filter { it.calledLandlords == false }

        // 如果两个人都不抢,一个人想要地主,那么就结算地主
        if (isFinal || (notWantList.size == 2 && wantList.size == 1)) {
            // 跳转到成为地主事件
            room.go(CallLandlordsEndEvent::class, gameArg)
            return
        }

        // 计算下一个玩家
        if (room.nextPlayer.calledLandlords == null) {
            // 如果下一个玩家没选择地主,那么跳转到下一个玩家
            room.currentPlayer = room.nextPlayer
        } else if (room.nextPlayer.calledLandlords!!) {
            // 如果下家选择过,说明三个人都选择过地主,那么看看下一个有没有抢过
            room.currentPlayer = room.nextPlayer
        } else if (room.prePlayer.calledLandlords!!) {
            // 下一个没有抢过那个看看上一个有没有抢过地主
            room.currentPlayer = room.prePlayer
        } else {
            // 三个人都不抢,重新发牌
            gameArg.map["rePoker"] = (gameArg.map["rePoker"]?.let { (it as Int) + 1 }) ?: 1
            room.go(StartGameEvent::class, gameArg)
            return
        }
        room.go(CallLandlordsEvent::class, gameArg)
    }

}
