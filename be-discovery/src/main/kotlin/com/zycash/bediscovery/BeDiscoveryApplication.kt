package com.zycash.bediscovery

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@EnableEurekaServer
@SpringBootApplication
class BeDiscoveryApplication

fun main(args: Array<String>) {
	runApplication<BeDiscoveryApplication>(*args)
}
