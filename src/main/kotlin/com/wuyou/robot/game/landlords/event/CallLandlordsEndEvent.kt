package com.wuyou.robot.game.landlords.event

import com.wuyou.robot.game.common.exception.GameException
import com.wuyou.robot.game.common.interfaces.GameArg
import com.wuyou.robot.game.common.interfaces.GameEvent
import com.wuyou.robot.game.landlords.LandlordsGame
import com.wuyou.robot.game.landlords.LandlordsPlayer
import com.wuyou.robot.game.landlords.LandlordsRoom
import com.wuyou.robot.game.landlords.common.PokerUtil
import com.wuyou.robot.game.landlords.enums.PlayerType
import org.springframework.stereotype.Component

/**
 * 抢地主结束事件
 */
@Component
class CallLandlordsEndEvent : GameEvent<LandlordsGame, LandlordsRoom, LandlordsPlayer>() {
    override fun invoke(room: LandlordsRoom, gameArg: GameArg) {
        room.landlordsPlayer ?: throw GameException("没有地主玩家")
        room.landlordsPoker ?: throw GameException("额外牌为空")
        room.playerList.forEach {
            if (it == room.landlordsPlayer) {
                // 当前玩家是地主
                it.type = PlayerType.LANDLORDS
                it.pokerList.addAll(room.landlordsPoker!!)
                it.pokerList.sortWith(PokerUtil.POKER_COMPARATOR)
                return@forEach
            }
            it.type = PlayerType.FARMER
        }
        room.currentPlayer = room.landlordsPlayer!!
        room.send("#player 成为了地主并获得了额外的三张牌!")
        room.send(PokerUtil.getPoker(room.landlordsPoker!!))
        room.go(PlayPokerEvent::class, gameArg)
    }
}
