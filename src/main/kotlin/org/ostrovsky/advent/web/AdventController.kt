package org.ostrovsky.advent.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.ZoneId

data class AdventDayDto(
    val day: Int,
    val date: LocalDate,
    val unlocked: Boolean,
    val solutionUnlocked: Boolean,
    val title: String,
    val taskUrl: String,
    val solutionUrl: String,
    val imageUrl: String? = null,
)

@RestController
@RequestMapping("/api/advent")
class AdventController(
    @param:Value("\${advent.fixed-today:}") private val fixedTodayRaw: String?,
) {
    private val zoneId: ZoneId = ZoneId.of("Europe/Berlin")

    private fun currentDate(): LocalDate =
        fixedTodayRaw?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) }
            ?: LocalDate.now(zoneId)

    private fun imageUrlFor(day: Int): String = "/images/doors/day-%02d.png".format(day)

    @GetMapping("/days")
    fun days(): List<AdventDayDto> {
        val today = currentDate()
        val year = 2025
        return (1..24).map { d ->
            val date = LocalDate.of(year, 12, d)
            AdventDayDto(
                day = d,
                date = date,
                unlocked = !today.isBefore(date),
                solutionUnlocked = today.isAfter(date),
                title = "Türchen $d",
                taskUrl = "/files/aufgabe-$d.pdf",
                solutionUrl = "/files/loesung-$d.pdf",
                imageUrl = imageUrlFor(d),
            )
        }
    }

    @GetMapping("/days/{day}")
    fun one(
        @PathVariable day: Int,
    ): Any {
        val today = currentDate()
        if (day !in 1..24) return mapOf("error" to "not found")
        val date = LocalDate.of(2025, 12, day)
        if (today.isBefore(date)) return mapOf("error" to "locked")

        return AdventDayDto(
            day = day,
            date = date,
            unlocked = true,
            solutionUnlocked = today.isAfter(date),
            title = "Türchen $day",
            taskUrl = "/files/aufgabe-$day.pdf",
            solutionUrl = "/files/loesung-$day.pdf",
            imageUrl = imageUrlFor(day),
        )
    }
}
