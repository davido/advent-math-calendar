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

// Profile: ordnet URL-Slug einem Schwierigkeits-Ordner zu
enum class AdventProfile(
    val slug: String, // z.B. "momo"
    val folder: String, // z.B. "light"
) {
    MOMO("momo", "light"),
    TRIXY("trixi", "medium"),
    LULU("lulu", "hard"),
    ;

    companion object {
        fun fromSlug(slug: String): AdventProfile? = entries.firstOrNull { it.slug.equals(slug, ignoreCase = true) }
    }
}

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

        // 👉 API-Endpunkte – mit Profil-Slug
        private const val TASK_URL_PATTERN = "/api/advent/%s/days/%d/task"
        private const val SOLUTION_URL_PATTERN = "/api/advent/%s/days/%d/solution"

        // 👉 Datei-Pfade (profil-spezifisch)
        // Erwartete Struktur:
        //  advent/files/light/aufgabe-1.pdf
        //  advent/files/medium/aufgabe-1.pdf
        //  advent/files/hard/aufgabe-1.pdf
        private const val TASK_FILE_PATTERN = "advent/files/%s/aufgabe-%d.pdf"
        private const val SOLUTION_FILE_PATTERN = "advent/files/%s/loesung-%d.pdf"

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

    private fun taskUrlFor(
        profile: AdventProfile,
        day: Int,
    ): String = TASK_URL_PATTERN.format(profile.slug, day)

    private fun solutionUrlFor(
        profile: AdventProfile,
        day: Int,
    ): String = SOLUTION_URL_PATTERN.format(profile.slug, day)

    private fun titleFor(day: Int): String = "$TITLE_PREFIX$day"

    private fun taskFilePath(
        profile: AdventProfile,
        day: Int,
    ): String = TASK_FILE_PATTERN.format(profile.folder, day)

    private fun solutionFilePath(
        profile: AdventProfile,
        day: Int,
    ): String = SOLUTION_FILE_PATTERN.format(profile.folder, day)

    // ---------- Liste aller Tage für ein Profil ----------

    // GET /api/advent/{profileSlug}/days
    @GetMapping("/{profileSlug}/days")
    fun days(
        @PathVariable profileSlug: String,
    ): Any {
        val profile =
            AdventProfile.fromSlug(profileSlug)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf(ERROR_KEY to ERROR_NOT_FOUND))

        val today = currentDate()
        val days =
            (FIRST_DAY..LAST_DAY).map { d ->
                val date = LocalDate.of(ADVENT_YEAR, ADVENT_MONTH, d)
                AdventDayDto(
                    day = d,
                    date = date,
                    unlocked = !today.isBefore(date),
                    solutionUnlocked = today.isAfter(date),
                    title = titleFor(d),
                    taskUrl = taskUrlFor(profile, d),
                    solutionUrl = solutionUrlFor(profile, d),
                    imageUrl = imageUrlFor(d),
                )
            }

        return days
    }

    // ---------- Einzelnes Türchen für ein Profil ----------

    // GET /api/advent/{profileSlug}/days/{day}
    @GetMapping("/{profileSlug}/days/{day}")
    fun one(
        @PathVariable profileSlug: String,
        @PathVariable day: Int,
    ): Any {
        val profile =
            AdventProfile.fromSlug(profileSlug)
                ?: return mapOf(ERROR_KEY to ERROR_NOT_FOUND)

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
            taskUrl = taskUrlFor(profile, day),
            solutionUrl = solutionUrlFor(profile, day),
            imageUrl = imageUrlFor(day),
        )
    }

    // ---------- Geschützter Download: Aufgabe (profil-spezifisch) ----------

    // GET /api/advent/{profileSlug}/days/{day}/task
    @GetMapping("/{profileSlug}/days/{day}/task", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun downloadTask(
        @PathVariable profileSlug: String,
        @PathVariable day: Int,
    ): ResponseEntity<Resource> {
        val profile =
            AdventProfile.fromSlug(profileSlug)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (day !in FIRST_DAY..LAST_DAY) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        val today = currentDate()
        val date = LocalDate.of(ADVENT_YEAR, ADVENT_MONTH, day)

        // Aufgabe ist erst ab diesem Datum freigeschaltet
        if (today.isBefore(date)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val resource = ClassPathResource(taskFilePath(profile, day))
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"aufgabe-${profile.slug}-$day.pdf\"",
            )
            .body(resource)
    }

    // ---------- Geschützter Download: Lösung (profil-spezifisch) ----------

    // GET /api/advent/{profileSlug}/days/{day}/solution
    @GetMapping("/{profileSlug}/days/{day}/solution", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun downloadSolution(
        @PathVariable profileSlug: String,
        @PathVariable day: Int,
    ): ResponseEntity<Resource> {
        val profile =
            AdventProfile.fromSlug(profileSlug)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (day !in FIRST_DAY..LAST_DAY) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        val today = currentDate()
        val date = LocalDate.of(ADVENT_YEAR, ADVENT_MONTH, day)

        // Lösung erst ab dem FOLGETAG
        if (!today.isAfter(date)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val resource = ClassPathResource(solutionFilePath(profile, day))
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"loesung-${profile.slug}-$day.pdf\"",
            )
            .body(resource)
    }
}
