package org.ostrovsky.advent.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class FrontendController {
    // Wenn jemand /calendar aufruft, liefere die gleiche Seite wie bei /
    @GetMapping("/calendar")
    fun calendar(): String = "forward:/"

    @GetMapping("/door/{day}")
    fun door(
        @PathVariable day: Int,
    ): String = "forward:/"
}
