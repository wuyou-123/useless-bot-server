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
        val tip = "成为了地主并获得了额外的三张牌!"
        room.landlordsPlayer ?: throw GameException("没有地主玩家")
        room.playerList.forEach {
            if (it == room.landlordsPlayer) {
                it.send("您 $tip")
                it.type = PlayerType.LANDLORDS
                return@forEach
            }
            it.send("${room.landlordsPlayer} $tip")
            it.type = PlayerType.FARMER
        }
        room.landlordsPoker ?: throw GameException("额外牌为空")
        room.send(PokerUtil.getPoker(room.landlordsPoker!!) ?: throw GameException("生成扑克牌失败!"))

    }
}
