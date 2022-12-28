package com.wuyou.robot.common

import com.wuyou.robot.entity.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import love.forte.simboot.annotation.Listener
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.bot.Bot
import love.forte.simbot.definition.Contact
import love.forte.simbot.definition.GroupMember
import love.forte.simbot.event.ChangedEvent
import love.forte.simbot.tryToLongID
import love.forte.simbot.utils.item.toList
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * @author wuyou
 */
@Component
class AccountManager : CommandLineRunner {
    private val members = mutableMapOf<LongID, MutableList<GroupMember>>()
    private val groupMembers = mutableMapOf<LongID, List<GroupMember>>()
    private val contacts = mutableMapOf<LongID, Contact>()


    override fun run(vararg args: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            getMembers(getBot())
        }
    }

    @Listener
    fun ChangedEvent.change() {
        CoroutineScope(Dispatchers.IO).launch {
            getMembers(bot)
        }
    }

    suspend fun getMembers(bot: Bot) {
        val mCache = mutableMapOf<LongID, MutableList<GroupMember>>()
        val gCache = mutableMapOf<LongID, List<GroupMember>>()
        val cCache = mutableMapOf<LongID, Contact>()
        mCache[bot.id.tryToLongID()] = bot.groups.toList().map { it.member(bot.id)!! }.toMutableList()
        bot.groups.toList().forEach { group ->
            gCache[group.id.tryToLongID()] = group.members.toList().onEach { member ->
                mCache.putIfAbsent(member.id.tryToLongID(), mutableListOf(member))?.also { it += member }
            }
        }
        bot.contacts.toList().forEach {
            cCache[it.id.tryToLongID()] = it
        }
        members.run { clear(); putAll(mCache) }
        contacts.run { clear(); putAll(cCache) }
        groupMembers.run { clear(); putAll(gCache) }
        logger {
            -"Statistics finished"
            -"- Contact count: ${contacts.size}"
            -"- Group count: ${groupMembers.size}"
            -"- All member count: ${members.size}"
        }
    }

    fun getGroupMembers(groupId: LongID) = groupMembers[groupId] ?: emptyList()

    fun getAccountGroups(id: ID) = members[id.tryToLongID()] ?: emptyList()

    fun getAccount(id: ID): Account? {
        val contact = contacts[id.tryToLongID()]
        return if (contact != null) Account(contact)
        else members[id.tryToLongID()]?.firstOrNull()?.let { Account(it) }
    }
}

