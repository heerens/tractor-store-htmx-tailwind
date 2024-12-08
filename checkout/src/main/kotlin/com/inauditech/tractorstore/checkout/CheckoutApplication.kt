package com.inauditech.tractorstore.checkout

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.inauditech.tractorstore"])
@ConfigurationPropertiesScan("com.inauditech.tractorstore")
class CheckoutApplication

fun main(args: Array<String>) {
    runApplication<CheckoutApplication>(*args)
}
