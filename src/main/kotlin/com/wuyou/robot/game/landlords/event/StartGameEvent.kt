package com.wuyou.robot.game.landlords.event

import com.wuyou.robot.game.common.interfaces.GameArg
import com.wuyou.robot.game.common.interfaces.GameEvent
import com.wuyou.robot.game.landlords.LandlordsGame
import com.wuyou.robot.game.landlords.LandlordsPlayer
import com.wuyou.robot.game.landlords.LandlordsRoom
import org.springframework.stereotype.Component

/**
 * @author wuyou
 */
@Component
class StartGameEvent : GameEvent<LandlordsGame, LandlordsRoom, LandlordsPlayer>() {
    override fun invoke(room: LandlordsRoom, gameArg: GameArg) {
        room.send("开始发牌")
    }
}
