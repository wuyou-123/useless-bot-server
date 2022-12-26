package com.wuyou.robot

import com.wuyou.robot.annotation.UselessBot
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@UselessBot
@SpringBootApplication
class UselessBotServerApplication

fun main(args: Array<String>) {
    runApplication<UselessBotServerApplication>(*args)
}
