package org.ostrovsky.advent.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.Month
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
    companion object {
        private const val ADVENT_YEAR = 2025
        private val ADVENT_MONTH: Month = Month.DECEMBER

        private const val FIRST_DAY = 1
        private const val LAST_DAY = 24

        private const val DOOR_IMAGE_NUMBER_WIDTH = 2
        private const val DOOR_IMAGE_PATTERN =
            "/images/doors/day-%0${DOOR_IMAGE_NUMBER_WIDTH}d.png"

        // 👉 API-Endpunkte
        private const val TASK_URL_PATTERN = "/api/advent/days/%d/task"
        private const val SOLUTION_URL_PATTERN = "/api/advent/days/%d/solution"

        // 👉 Datei-Pfade (nur HIER definiert!)
        private const val TASK_FILE_PATTERN = "advent/files/aufgabe-%d.pdf"
        private const val SOLUTION_FILE_PATTERN = "advent/files/loesung-%d.pdf"

        private const val TITLE_PREFIX = "Türchen "

        private const val ERROR_KEY = "error"
        private const val ERROR_NOT_FOUND = "not found"
        private const val ERROR_LOCKED = "locked"

        private const val ZONE_ID_EUROPE_BERLIN = "Europe/Berlin"
    }

    private val zoneId: ZoneId = ZoneId.of(ZONE_ID_EUROPE_BERLIN)

    private fun currentDate(): LocalDate =
        fixedTodayRaw?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) }
            ?: LocalDate.now(zoneId)

    private fun imageUrlFor(day: Int): String = DOOR_IMAGE_PATTERN.format(day)

    private fun taskUrlFor(day: Int): String = TASK_URL_PATTERN.format(day)

    private fun solutionUrlFor(day: Int): String = SOLUTION_URL_PATTERN.format(day)

    private fun titleFor(day: Int): String = "$TITLE_PREFIX$day"

    @GetMapping("/days")
    fun days(): List<AdventDayDto> {
        val today = currentDate()
        return (FIRST_DAY..LAST_DAY).map { d ->
            val date = LocalDate.of(ADVENT_YEAR, ADVENT_MONTH, d)
            AdventDayDto(
                day = d,
                date = date,
                unlocked = !today.isBefore(date),
                solutionUnlocked = today.isAfter(date),
                title = titleFor(d),
                taskUrl = taskUrlFor(d),
                solutionUrl = solutionUrlFor(d),
                imageUrl = imageUrlFor(d),
            )
        }
    }

    @GetMapping("/days/{day}")
    fun one(
        @PathVariable day: Int,
    ): Any {
        val today = currentDate()

        if (day !in FIRST_DAY..LAST_DAY) {
            return mapOf(ERROR_KEY to ERROR_NOT_FOUND)
        }

        val date = LocalDate.of(ADVENT_YEAR, ADVENT_MONTH, day)
        if (today.isBefore(date)) {
            return mapOf(ERROR_KEY to ERROR_LOCKED)
        }

        return AdventDayDto(
            day = day,
            date = date,
            unlocked = true,
            solutionUnlocked = today.isAfter(date),
            title = titleFor(day),
            taskUrl = taskUrlFor(day),
            solutionUrl = solutionUrlFor(day),
            imageUrl = imageUrlFor(day),
        )
    }

    // ---------- Geschützter Download: Aufgabe ----------

    @GetMapping("/days/{day}/task", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun downloadTask(
        @PathVariable day: Int,
    ): ResponseEntity<Resource> {
        if (day !in FIRST_DAY..LAST_DAY) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        val today = currentDate()
        val date = LocalDate.of(ADVENT_YEAR, ADVENT_MONTH, day)

        // Aufgabe ist erst ab diesem Datum freigeschaltet
        if (today.isBefore(date)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val resource = ClassPathResource(TASK_FILE_PATTERN.format(day))
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"aufgabe-$day.pdf\"",
            )
            .body(resource)
    }

    // ---------- Geschützter Download: Lösung ----------

    @GetMapping("/days/{day}/solution", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun downloadSolution(
        @PathVariable day: Int,
    ): ResponseEntity<Resource> {
        if (day !in FIRST_DAY..LAST_DAY) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        val today = currentDate()
        val date = LocalDate.of(ADVENT_YEAR, ADVENT_MONTH, day)

        // Lösung erst ab dem FOLGETAG
        if (!today.isAfter(date)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val resource = ClassPathResource(SOLUTION_FILE_PATTERN.format(day))
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"loesung-$day.pdf\"",
            )
            .body(resource)
    }
}
