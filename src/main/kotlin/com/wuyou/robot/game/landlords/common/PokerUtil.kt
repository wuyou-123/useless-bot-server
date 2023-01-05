package com.wuyou.robot.game.landlords.common

import com.wuyou.robot.game.common.GameManager
import com.wuyou.robot.game.common.exception.GameException
import com.wuyou.robot.game.landlords.LandlordsGame
import com.wuyou.robot.game.landlords.enums.PokerLevel
import com.wuyou.robot.game.landlords.enums.PokerType
import com.wuyou.robot.util.MessageUtil.getResMessage
import com.wuyou.robot.util.RuntimeUtil
import love.forte.simbot.message.ResourceImage
import java.io.File

object PokerUtil {
    /**
     * 所有扑克牌类型
     */
    private val BASE_POKERS: List<Poker>
    val POKER_COMPARATOR: Comparator<Poker> = Comparator.comparingInt { poker: Poker -> poker.level.level }

    init {
        val pokerList = mutableListOf<Poker>()
        val pokerLevels = PokerLevel.values()
        val pokerTypes = PokerType.values().filter { it != PokerType.BLANK }
        pokerLevels.forEach { level ->
            // 如果是大小王,则为空花色
            if (level === PokerLevel.LEVEL_BIG_KING || level === PokerLevel.LEVEL_SMALL_KING) {
                pokerList.add(Poker(level, PokerType.BLANK))
            } else {
                // 遍历四种花色添加到扑克牌中
                pokerTypes.forEach { type ->
                    pokerList.add(Poker(level, type))
                }
            }
        }
        BASE_POKERS = pokerList
    }

    fun initPoker(): List<List<Poker>> {
        val pokers = BASE_POKERS.toMutableList()
        pokers.shuffle()
        val count = 54
        val pokerSize = 17
        val landlordsPokerSize = 3
        val maxPlayerSize = 3
        assert(pokers.size == count)
        val pokersList = mutableListOf<List<Poker>>()
        for (i in 0..maxPlayerSize) {
            ArrayList<Poker>(pokerSize).apply {
                addAll(pokers.subList(
                    if (i == maxPlayerSize) count - landlordsPokerSize else (i * pokerSize),
                    if (i == maxPlayerSize) count else (i + 1) * pokerSize
                ).apply { sortWith(POKER_COMPARATOR) })
                pokersList += this
            }
        }
        return pokersList.toList()
    }

    fun parsePoker(message: String): Array<Char>? {
        var message0 = replaceDoubleMessage(message)
        message0 = replaceKingMessage(message0)
        message0 = message0.replace("10", "0").replace("\\s".toRegex(), "")
        val list = mutableListOf<Char>()
        message0.toCharArray().forEach { c ->
            if (!PokerLevel.aliasContains(c)) return null
            list += c
        }
        return list.toTypedArray()
    }

    private fun replaceDoubleMessage(message: String): String {
        val isMatch = "^对.$".toRegex().matches(message)
        return if (!isMatch) message
        else "" + message[1] + message[1]
    }

    private fun replaceKingMessage(message: String): String {
        return message.replace("大王", "x").replace("小王", "s").replace("王炸", "sx").replace("双王", "sx")
    }

    fun getPoker(pokers: List<Poker>): ResourceImage {
        if (pokers.isEmpty()) {
            throw GameException("扑克牌为空!")
        }
        val pokerList: MutableList<String> = java.util.ArrayList()
        for (poker in pokers) {
            val a = poker.level.pokerName.replace("A", "1").replace("小王", "s").replace("大王", "x")
            val b = when (poker.type) {
                PokerType.SPADE -> "a"
                PokerType.HEART -> "b"
                PokerType.CLUB -> "c"
                PokerType.DIAMOND -> "d"
                else -> "e"
            }
            pokerList.add((b + a).lowercase().replace("10", "0"))
        }
        val sort = charArrayOf('x', 's', '2', '1', 'k', 'q', 'j', '0', '9', '8', '7', '6', '5', '4', '3')
        val sort2 = charArrayOf('e', 'a', 'b', 'c', 'd')
        pokerList.sortWith { a: String, b: String ->
            var aIndex = -1
            var bIndex = -1
            for (i in sort.indices) {
                if (a.toCharArray()[1] == sort[i]) aIndex = i
                if (b.toCharArray()[1] == sort[i]) bIndex = i
            }
            if (aIndex == bIndex) {
                for (i in sort2.indices) {
                    if (a.toCharArray()[0] == sort2[i]) aIndex = i
                    if (b.toCharArray()[0] == sort2[i]) bIndex = i
                }
            }
            aIndex - bIndex
        }
        var pokerStr = pokerList.toString().replace(" ", "").replace(",", "_")
        pokerStr = pokerStr.substring(1, pokerStr.length - 1)
        val game: LandlordsGame = GameManager.getGame()
        val tempPath = game.getTempPath()
        val pokerDir = File(tempPath + "poker_comp")
        if (!pokerDir.exists() && !pokerDir.mkdirs()) {
            throw GameException("Destination '$pokerDir' directory cannot be created")
        }
        val pokerFile = File(pokerDir.toString() + File.separator + pokerStr + ".jpg")
        if (pokerFile.exists()) {
            return pokerFile.toString().getResMessage()
        }
        RuntimeUtil.exec(
            "python",
            pokerList,
            tempPath + "generatePoker.py",
            tempPath + "poker" + File.separator,
            pokerFile.toString()
        )
        return pokerFile.toString().getResMessage()
    }

    fun filterPokerByMessage(message: String, pokerList: List<Poker>): List<Poker>? {
        val list = parsePoker(message) ?: return null
        val pokers0 = mutableListOf<Poker>()
        val pokers1 = ArrayList(pokerList)
        for (c in list) {
            pokers1.filter { it.level.alias.contains(c) }.also {
                if (it.isEmpty()) return null
                pokers0 += it[0]
                pokers1.remove(it[0])
            }
        }
        return pokers0.let {
            if (it.size != list.size) null else it
        }
    }
}
