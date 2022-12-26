package com.wuyou.robot.entity

import love.forte.simbot.definition.User

data class Account(val id: String, val username: String, val avatar: String) {

    private var user: User? = null

    constructor(user: User) : this(user.id.toString(), user.username, user.avatar) {
        this.user = user
    }

}
