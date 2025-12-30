package com.zycash.beconfigserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.config.server.EnableConfigServer

@EnableConfigServer
@SpringBootApplication
class BeConfigServerApplication

fun main(args: Array<String>) {
	runApplication<BeConfigServerApplication>(*args)
}
