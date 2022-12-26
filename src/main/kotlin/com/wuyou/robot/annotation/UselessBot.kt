package com.wuyou.robot.annotation

import love.forte.simboot.spring.autoconfigure.*
import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.ComponentScan

@Target(AnnotationTarget.CLASS)
@MapperScan("com.wuyou.robot.*", "com.gitee.sunchenbin.mybatis.actable.dao.*")
@ComponentScan("com.wuyou.robot.*", "com.gitee.sunchenbin.mybatis.actable.manager.*")
annotation class UselessBot
