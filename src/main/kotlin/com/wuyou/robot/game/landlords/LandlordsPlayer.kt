package com.wuyou.robot.game.landlords

import com.wuyou.robot.common.Timer
import com.wuyou.robot.game.common.interfaces.GameArg
import com.wuyou.robot.game.common.interfaces.GameStatus
import com.wuyou.robot.game.common.interfaces.Player
import com.wuyou.robot.game.landlords.common.Poker
import com.wuyou.robot.game.landlords.enums.PlayerType
import java.util.concurrent.TimeUnit

/**
 * @author wuyou
 */
class LandlordsPlayer(
    id: String,
    name: String,
    room: LandlordsRoom,
) : Player<LandlordsGame, LandlordsRoom, LandlordsPlayer>(id, name, room) {
    lateinit var pokerList: MutableList<Poker>
    var type: PlayerType? = null
    var status: String = ""
    var timer: Timer<GameArg>? = null

    /**
     * 是否叫地主,没叫过为null
     */
    var calledLandlords: Boolean? = null
    val next: LandlordsPlayer
        get() = if (getCurrentIndex() + 1 == room.playerList.size) room.playerList[0]
        else room.playerList[getCurrentIndex() + 1]
    val pre: LandlordsPlayer
        get() = if (getCurrentIndex() - 1 < 0) room.playerList[room.playerList.size - 1]
        else room.playerList[getCurrentIndex() - 1]

    private fun getCurrentIndex() = room.playerList.indexOf(this)

    override fun toString(): String {
        return super.toString() + if (type != null) "[${type!!.type}]" else ""
    }

    fun pokerTimer(gameArg: GameArg, block: (Timer<GameArg>) -> Unit) {
        timer = Timer(30, TimeUnit.SECONDS, gameArg, true) {
            block(this)
        }
    }

    override fun getStatus() = GameStatus(status.ifEmpty { "null" })
}
