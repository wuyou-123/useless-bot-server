package com.wuyou.robot.config

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.wuyou.robot.common.logger
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.logging.LogLevel
import org.springframework.context.annotation.Configuration

/**
 * @author wuyou
 */
@Configuration
@EnableConfigurationProperties(SshProperties::class)
class SshConfiguration(private val sshProperties: SshProperties) : BeanPostProcessor {
    private var session: Session? = null

    init {
        if (sshProperties.host != null) {
            try {
                reconnect()
            } catch (e: JSchException) {
                logger(LogLevel.ERROR, e) { "Ssh ${sshProperties.host} failed." }
//                Sender.sendPrivateMsg("1097810498", "Ssh ${sshProperties.host} failed.")
            }
        }
    }

    final fun reconnect() {
        if (session?.isConnected == true) return
        logger { "reconnect ssh..." }
        session = JSch().getSession(sshProperties.username, sshProperties.host, sshProperties.port!!).apply {
            setConfig("StrictHostKeyChecking", "no")
            setPassword(sshProperties.password)
            serverAliveInterval = 2000
            logger { "Connecting ssh..." }
            connect(10000)
            sshProperties.forward?.let {
                setPortForwardingL(it.fromHost, it.fromPort!!, it.toHost, it.toPort!!)
                logger {
                    "Forward database success! ${it.fromHost}:${it.fromPort} -> ${it.toHost}:${it.toPort}"
                }
            }
        }
    }
}
