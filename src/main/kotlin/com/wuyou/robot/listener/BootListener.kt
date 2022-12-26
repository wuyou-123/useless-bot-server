package com.wuyou.robot.listener

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.wuyou.robot.annotation.RobotListen
import com.wuyou.robot.common.*
import com.wuyou.robot.entity.GroupBootState
import com.wuyou.robot.entity.GroupBootStateService
import com.wuyou.robot.enums.RobotPermission
import com.wuyou.robot.util.MessageUtil.getAtSet
import com.wuyou.robot.util.MessageUtil.groupId
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.Filter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.event.GroupMessageEvent

/**
 * 监听群开关机
 *
 * @author wuyou
 */
@Beans
class BootListener(private val groupBootStateService: GroupBootStateService) {
    companion object {
        private const val bootKey = "group_boot"
        private const val bootAlreadyKey = "group_already_boot"
        private const val downKey = "group_down"
        private const val downAlreadyKey = "group_already_down"
    }

    @RobotListen(
        permission = RobotPermission.ADMINISTRATOR, noPermissionTip = "", commandKey = bootKey, commandDefault = "开机"
    )
    suspend fun GroupMessageEvent.boot() {
        val groupCode = groupId()
        val atSet = getAtSet()
        if (atSet.isEmpty() || atSet.contains(bot.id)) {
            logger { "群${groupCode}开机" }
            bootOrDown(groupCode, true)
            send(getCommand(groupCode, bootAlreadyKey, "已开机"))
        }
    }

    @RobotListen(
        permission = RobotPermission.ADMINISTRATOR, isBoot = true, commandKey = downKey, commandDefault = "关机"
    )
    suspend fun GroupMessageEvent.down() {
        val groupCode = groupId()

        val atSet = getAtSet()
        if (atSet.isEmpty() || atSet.contains(bot.id)) {
            logger { "群${groupCode}关机" }
            bootOrDown(groupCode, false)
            send(getCommand(groupCode, downAlreadyKey, "已关机"))
        }
    }

    @RobotListen(permission = RobotPermission.ADMINISTRATOR)
    @Filter(value = "设置.{5,}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.setBoot() {
        val word = messageContent.plainText.let { it.substring(2, it.length) }
        val key = when {
            word.startsWith("开机命令") -> bootKey
            word.startsWith("关机命令") -> downKey
            word.startsWith("开机提示") -> bootAlreadyKey
            word.startsWith("关机提示") -> downAlreadyKey
            else -> ""
        }
        setCommand(groupId(), key, word.substring(4, word.length))
        send("设置成功")
    }

    private fun bootOrDown(groupCode: String, state: Boolean) {
        RobotCore.BOOT_MAP[groupCode] = state
        val wrapper = KtQueryWrapper(GroupBootState::class.java).eq(GroupBootState::groupCode, groupCode)
        groupBootStateService.getOne(wrapper)?.let {
            if (it.state != state) {
                it.state = state
                groupBootStateService.updateById(it)
            }
        }.isNull {
            groupBootStateService.save(GroupBootState(state = state, groupCode = groupCode))
        }
    }

}
