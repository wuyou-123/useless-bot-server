package com.wuyou.robot.game.landlords.common

import com.wuyou.robot.game.landlords.enums.PokerLevel
import com.wuyou.robot.game.landlords.enums.PokerType

/**
 * 扑克牌
 * @author wuyou
 */
data class Poker(
    val level: PokerLevel,
    val type: PokerType
) {

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + level.hashCode()
        result = prime * result + type.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other !is Poker) {
            return false
        }
        return if (level != other.level) false
        else type == other.type
    }

    override fun toString(): String {
        return "$level "
    }
}
