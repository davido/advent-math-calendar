package org.ostrovsky.advent.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class FrontendController {
    @GetMapping("/calendar")
    fun calendar(): String = "forward:/"

    @GetMapping("/door/{day}")
    fun door(
        @PathVariable day: Int,
    ): String = "forward:/"

    @GetMapping("/momo")
    fun leon(): String = "forward:/"

    @GetMapping("/trixi")
    fun linda(): String = "forward:/"

    @GetMapping("/lulu")
    fun tony(): String = "forward:/"

    @GetMapping("/momo/door/{day}")
    fun momoDoor(
        @PathVariable day: Int,
    ): String = "forward:/"

    @GetMapping("/trixi/door/{day}")
    fun trixiDoor(
        @PathVariable day: Int,
    ): String = "forward:/"

    @GetMapping("/lulu/door/{day}")
    fun luluDoor(
        @PathVariable day: Int,
    ): String = "forward:/"
}
