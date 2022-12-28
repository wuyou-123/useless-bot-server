package com.wuyou.robot.game.common.interfaces

import com.wuyou.robot.common.Sender

/**
 * @author wuyou
 */
abstract class Player<G : Game<G, R, P>, R : Room<G, P, R>, P : Player<G, R, P>>(
    open var id: String,
    open var name: String,
    open var room: R,
) {
    var isPlaying = false
    var pre: P? = null
    var next: P? = null

    fun getRoomId() = room.id

    fun isInRoom(roomId: Int): Boolean = room.id == roomId
    fun send(messages: Any, separator: String = "") = Sender.sendPrivateMsg(id, messages, separator)
    override fun toString(): String = "${name}[$id]"
    open fun getStatus() = GameStatus("")
}

