package com.wuyou.robot.game.common.listener

import com.wuyou.robot.annotation.RobotListen
import com.wuyou.robot.common.isNull
import com.wuyou.robot.common.send
import com.wuyou.robot.game.common.gameManager
import com.wuyou.robot.util.MessageUtil.authorId
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simbot.event.FriendMessageEvent

@Beans
class GameListener {
    @RobotListen(isBoot = true)
    @Filter(".*房间列表")
    suspend fun FriendMessageEvent.roomList() {
        val name = messageContent.plainText.substringBefore("房间列表")
        if (name.isNotBlank()) {
            gameManager().getGameByName(name).isNull {
                send("没有找到名为\"$name\"的游戏")
                return
            }
        }
        val msg = mutableListOf<Any>()
        gameManager().getRoomListByGame(name)?.also {
            msg += "房间列表:"
            it.forEachIndexed { _, room ->
                msg += room.getDesc(authorId())
            }
            msg += "发送\"加入[房间编号]\"来加入房间"
        }.isNull {
            msg += "当前暂无房间"
        }
        send(msg, "\n")
    }

    @RobotListen(isBoot = true)
    @Filter("游戏列表")
    suspend fun FriendMessageEvent.gameList() {
        val msg = mutableListOf<Any>()
        gameManager().gameSet.forEachIndexed { index, game ->
            msg += "${index + 1}. ${game.name}"
        }
        msg += "发送\"<游戏名>房间列表\"可以选择房间加入"
        msg += "发送\"创建[游戏名]房间\"可以创建房间"
        send(msg, "\n")
    }

    @RobotListen(isBoot = true)
    @Filter("创建{{game}}房间")
    suspend fun FriendMessageEvent.createGame(@FilterValue("game") name: String) {
        gameManager().createRoomByGameName(this, name)
    }

    @RobotListen(isBoot = true)
    @Filter("加入{{num,\\d{6}}}")
    suspend fun FriendMessageEvent.joinGame(@FilterValue("num") num: Int) {
        gameManager().joinRoomById(this, num)
    }

    @RobotListen(isBoot = true)
    @Filter("退出房间|离开房间")
    suspend fun FriendMessageEvent.leaveGame() {
        gameManager().leaveRoom(this)
    }

}
