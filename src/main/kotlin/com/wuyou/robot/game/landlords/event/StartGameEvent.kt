package com.wuyou.robot.game.landlords.event

import com.wuyou.robot.game.common.exception.GameException
import com.wuyou.robot.game.common.interfaces.GameArg
import com.wuyou.robot.game.common.interfaces.GameEvent
import com.wuyou.robot.game.landlords.LandlordsGame
import com.wuyou.robot.game.landlords.LandlordsPlayer
import com.wuyou.robot.game.landlords.LandlordsRoom
import com.wuyou.robot.game.landlords.common.PokerUtil
import org.springframework.stereotype.Component

/**
 * 开说游戏事件,分配扑克牌,确定第一个玩家
 * @author wuyou
 */
@Component
class StartGameEvent : GameEvent<LandlordsGame, LandlordsRoom, LandlordsPlayer>() {
    override fun invoke(room: LandlordsRoom, gameArg: GameArg) {
        gameArg.map["rePoker"]?.let {
            if (it as Int == 3) {
                room.send("超过三轮都没有人选择地主, 解散房间!")
                room.destroyRoom()
                return
            }
            room.send("没有人选择地主, 重新发牌!")
        }
        val pokerList = PokerUtil.initPoker()
        room.playerList.forEachIndexed { i, player ->
            player.pokerList = pokerList[i].toMutableList()
            player.calledLandlords = null
        }
        room.landlordsPoker = pokerList[3]
        if (room.currentPlayerIndex == -1) {
            room.currentPlayerIndex = (0..2).random()
        }
        room.landlordsPlayer = null
        room.playerList.forEach {
            it.send("游戏开始! 您的牌如下: ")
            it.send(PokerUtil.getPoker(it.pokerList) ?: throw GameException("生成扑克牌失败"))
        }
        room.go(CallLandlordsEvent::class, gameArg)
    }
}
