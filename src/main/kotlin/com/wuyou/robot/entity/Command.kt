package com.wuyou.robot.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gitee.sunchenbin.mybatis.actable.annotation.*
import org.apache.ibatis.annotations.Mapper
import org.springframework.stereotype.Service

@Table(value = "command", comment = "命令列表")
data class Command(
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @IsKey
    @IsAutoIncrement
    @Column(isNull = false)
    var id: Long? = null,
    @Column(comment = "群号", length = 50)
    var groupCode: String,
    @Unique(columns = ["group_code", "command_key"])
    @Column(comment = "命令的key", isNull = false)
    var commandKey: String,
    @Column(comment = "命令", isNull = false)
    var command: String
) : SuperEntity()

@Mapper
interface CommandMapper : BaseMapper<Command>

@Service
class CommandService : ServiceImpl<CommandMapper, Command>()
