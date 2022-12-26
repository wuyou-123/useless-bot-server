package com.wuyou.robot.listener

import com.wuyou.robot.common.logger
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.Listener
import love.forte.simbot.event.FriendMessageEvent

@Beans
class TestListener {
    @Listener
    fun FriendMessageEvent.test() {
        logger { messageContent.messages }
    }
}
