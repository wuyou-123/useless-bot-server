package com.wuyou.robot.game.landlords

import com.wuyou.robot.game.common.interfaces.Game
import org.springframework.stereotype.Component

/**
 * 斗地主游戏
 * @author wuyou
 */
@Component
class LandlordsGame : Game<LandlordsGame, LandlordsRoom, LandlordsPlayer>() {
    override val id = "landlords"
    override val name = "斗地主"
    override val minPlayerCount = 3
    override val maxPlayerCount = 3
    override val gameArgs: List<String> = listOf()

}
