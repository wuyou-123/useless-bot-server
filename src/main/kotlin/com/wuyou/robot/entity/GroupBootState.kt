package com.wuyou.robot.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gitee.sunchenbin.mybatis.actable.annotation.*
import org.apache.ibatis.annotations.Mapper
import org.springframework.stereotype.Service

@Table(value = "group_boot_state", comment = "群开关机状态")
data class GroupBootState(
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @IsKey
    @IsAutoIncrement
    @Column(isNull = false)
    var id: Long? = null,
    @Column(comment = "群号", length = 50, isNull = false)
    @Unique
    var groupCode: String,
    @Column(comment = "状态", isNull = false)
    var state: Boolean
) : SuperEntity()

@Mapper
interface GroupBootStateMapper : BaseMapper<GroupBootState>

@Service
class GroupBootStateService : ServiceImpl<GroupBootStateMapper, GroupBootState>()
