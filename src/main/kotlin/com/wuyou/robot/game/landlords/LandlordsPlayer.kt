package com.wuyou.robot.game.landlords

import com.wuyou.robot.game.common.interfaces.Player

/**
 * @author wuyou
 */
class LandlordsPlayer(
    id: String,
    name: String,
    room: LandlordsRoom,
) : Player<LandlordsGame, LandlordsRoom, LandlordsPlayer>(id, name, room)
