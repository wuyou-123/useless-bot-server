package com.wuyou.robot.util

import kotlinx.coroutines.runBlocking
import love.forte.simbot.definition.FriendInfoContainer
import love.forte.simbot.definition.GroupInfoContainer
import love.forte.simbot.event.*
import love.forte.simbot.message.At
import love.forte.simbot.message.Image.Key.toImage
import love.forte.simbot.resources.Resource.Companion.toResource
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * @author wuyou
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
object MessageUtil {

    fun GroupMessageEvent.getAtList() = messageContent.messages.filter { it.key == At.Key }.map { (it as At).target }

    fun GroupMessageEvent.getAtSet() = HashSet(getAtList())

    fun String.getResMessage() = Path(this).getResMessage()

    fun Path.getResMessage() = runBlocking { this@getResMessage.toResource().toImage() }

    suspend fun ChatRoomMessageEvent.authorId() = author().id.toString()

    suspend fun FriendMessageEvent.authorId() = friend().id.toString()

    suspend fun GroupEvent.groupId() = group().id.toString()

    suspend fun FriendInfoContainer.authorId() = friend().id.toString()

    suspend fun GroupInfoContainer.groupId() = group().id.toString()

    fun Event.botId() = bot.id.toString()
}
