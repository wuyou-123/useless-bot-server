package com.wuyou.robot.game.landlords

import com.wuyou.robot.game.common.interfaces.Player
import com.wuyou.robot.game.landlords.common.Poker
import com.wuyou.robot.game.landlords.enums.PlayerType

/**
 * @author wuyou
 */
class LandlordsPlayer(
    id: String,
    name: String,
    room: LandlordsRoom,
) : Player<LandlordsGame, LandlordsRoom, LandlordsPlayer>(id, name, room) {
    lateinit var pokerList: MutableList<Poker>
    var type = PlayerType.FARMER

    /**
     * 是否叫地主,没叫过为null
     */
    var calledLandlords: Boolean? = null
}
