package com.wuyou.robot.common

import com.wuyou.robot.common.CommandUtil.Companion.COMMAND_MAP
import com.wuyou.robot.entity.Command
import com.wuyou.robot.entity.CommandService
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class CommandUtil(private val commandService: CommandService) {
    @PostConstruct
    fun init() {
        val list = commandService.list()
        list.forEach {
            COMMAND_MAP[getMapKey(it.groupCode, it.commandKey)] = it
        }
    }

    companion object {
        val COMMAND_MAP = mutableMapOf<String, Command>()
    }

}

private fun getMapKey(groupCode: String, key: String) = "${groupCode}---${key}"

fun getCommand(groupCode: String, key: String, default: String): String =
    COMMAND_MAP[getMapKey(groupCode, key)]?.command ?: default

fun getCommandOrNull(groupCode: String, key: String): String? =
    COMMAND_MAP[getMapKey(groupCode, key)]?.command

fun setCommand(groupCode: String, key: String, value: String) {
    (COMMAND_MAP[getMapKey(groupCode, key)] ?: Command(null, groupCode, key, value)).apply {
        command = value
        getBean(CommandService::class.java).saveOrUpdate(this)
        COMMAND_MAP[getMapKey(groupCode, key)] = this
    }
}


