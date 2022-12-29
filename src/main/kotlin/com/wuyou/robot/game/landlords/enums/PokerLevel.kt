package com.wuyou.robot.game.landlords.enums

/**
 * 扑克牌
 * @author wuyou
 */
enum class PokerLevel(val level: Int, val pokerName: String, private val alias: Array<Char>) {
    /**
     * 所有的牌
     */
    LEVEL_3(3, "3", arrayOf('3', '三')),
    LEVEL_4(4, "4", arrayOf('4', '四')),
    LEVEL_5(5, "5", arrayOf('5', '五')),
    LEVEL_6(6, "6", arrayOf('6', '六')),
    LEVEL_7(7, "7", arrayOf('7', '七')),
    LEVEL_8(8, "8", arrayOf('8', '八')),
    LEVEL_9(9, "9", arrayOf('9', '九')),
    LEVEL_10(10, "10", arrayOf('T', 't', '0', '十')),
    LEVEL_J(11, "J", arrayOf('J', 'j')),
    LEVEL_Q(12, "Q", arrayOf('Q', 'q')),
    LEVEL_K(13, "K", arrayOf('K', 'k')),
    LEVEL_A(14, "A", arrayOf('A', 'a', '1')),
    LEVEL_2(15, "2", arrayOf('2', '二')),

    /**
     * 小王
     */
    LEVEL_SMALL_KING(16, "小王", arrayOf('S', 's')),

    /**
     * 大王
     */
    LEVEL_BIG_KING(17, "大王", arrayOf('X', 'x'));

    companion object {
        private val ALIAS_SET: MutableSet<Char> = HashSet()

        init {
            values().forEach {
                ALIAS_SET.addAll(it.alias)
            }
        }
    }
}
