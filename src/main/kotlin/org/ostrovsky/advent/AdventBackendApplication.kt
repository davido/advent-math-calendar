package org.ostrovsky.advent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AdventBackendApplication

fun main(args: Array<String>) {
    runApplication<AdventBackendApplication>(*args)
}
