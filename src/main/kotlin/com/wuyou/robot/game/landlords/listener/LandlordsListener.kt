package com.wuyou.robot.game.landlords.listener

import com.wuyou.robot.common.isNull
import com.wuyou.robot.game.common.GameManager
import com.wuyou.robot.game.common.annotation.GameListen
import com.wuyou.robot.game.common.interfaces.GameArg
import com.wuyou.robot.game.landlords.LandlordsPlayer
import com.wuyou.robot.game.landlords.common.MessageCmd
import com.wuyou.robot.game.landlords.common.PokerUtil
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.AnnotationEventFilterFactory
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.Filters
import love.forte.simboot.listener.ParameterBinder
import love.forte.simboot.listener.ParameterBinderFactory
import love.forte.simboot.listener.ParameterBinderResult
import love.forte.simbot.MutableAttributeMap
import love.forte.simbot.attribute
import love.forte.simbot.event.*
import kotlin.reflect.full.findAnnotation

@Beans
class LandlordsListener {

    @GameListen("扑克牌监听")
    @Filter(by = PokerFilterFactory::class)
    fun FriendMessageEvent.poker(@GameAttr("player") player: LandlordsPlayer) {
        if (player.timer != null) {
            if (MessageCmd.PASS_CMD_LIST.contains(messageContent.plainText)) {
                // 玩家不出牌
                if (player == player.room.lastPlayPlayer) {
                    player.send("不可以不出牌!")
                    return
                }
                player.timer!!.cancel()
                player.timer = null
                return
            }
            PokerUtil.parsePoker(messageContent.plainText)?.let {
                // 玩家出牌
                PokerUtil.filterPokerByMessage(messageContent.plainText, player.pokerList)?.also {
                    player.timer!!.arg["pokerList"] = it
                    player.timer!!.cancel()
                    player.timer = null
                }.isNull {
                    player.send("不能出这副牌!")
                }
                return
            }
        } else {
            // 其他消息事件
            player.room.game.eventMap[player.getStatus()]?.forEach {
                it.invoke(player.room, GameArg(this))
            }
        }
    }
}


annotation class GameAttr(val value: String)

@Beans
class GameParameterBinderFactory : ParameterBinderFactory {
    override fun resolveToBinder(context: ParameterBinderFactory.Context): ParameterBinderResult {
        val attrAnnotation = context.parameter.findAnnotation<GameAttr>() ?: return ParameterBinderResult.empty()
        return ParameterBinderResult.normal(AttrBinder(attrAnnotation.value))
    }

    private class AttrBinder(attrName: String) : ParameterBinder {
        private val attr = attribute<Any>(attrName)
        override suspend fun arg(context: EventListenerProcessingContext): Result<Any?> {
            return kotlin.runCatching { context[attr] }
        }
    }
}


class PokerFilterFactory : AnnotationEventFilterFactory {
    override fun resolveFilter(
        listener: EventListener,
        listenerAttributes: MutableAttributeMap,
        filter: Filter,
        filters: Filters,
    ) = PokerFilter()
}

class PokerFilter : EventFilter {
    override suspend fun test(context: EventListenerProcessingContext): Boolean {
        if (context.event is FriendMessageEvent) {
            GameManager.getInstance().getPlayerByEvent(context.event as FriendMessageEvent)?.let { player ->
                if (player is LandlordsPlayer) {
                    context[EventProcessingContext]?.put(attribute("player"), player)
                    return true
                }
            }
        }
        return false
    }

}
