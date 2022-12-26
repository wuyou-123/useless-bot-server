package com.wuyou.robot.entity

import com.gitee.sunchenbin.mybatis.actable.annotation.*
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlCharsetConstant
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlEngineConstant
import java.util.*

@TableCharset(MySqlCharsetConstant.UTF8)
@TableEngine(MySqlEngineConstant.InnoDB)
open class SuperEntity

