package com.wuyou.robot.game.common.exception

import com.wuyou.robot.exception.RobotException
import love.forte.simbot.event.Event

open class GameException(override val message: String, val event: Event? = null) : RobotException(message)
