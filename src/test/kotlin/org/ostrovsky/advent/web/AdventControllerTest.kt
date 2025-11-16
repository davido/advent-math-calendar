package org.ostrovsky.advent.web

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.core.env.Environment
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import java.time.LocalDate
import java.time.ZoneId

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["advent.fixed-today=2025-12-02"])
class AdventControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val jsonMapper: JsonMapper,
        private val env: Environment,
    ) {

        companion object {
            private const val ADVENT_YEAR = 2025
            private const val ADVENT_MONTH_VALUE = 12

            private const val FIRST_DAY = 1
            private const val LAST_DAY = 24

            private const val INVALID_DAY_LOW = 0
            private const val INVALID_DAY_HIGH = 25

            private const val ADVENT_FIRST_DAY = 1
            private const val ADVENT_LAST_DAY = 24

            private const val ZONE_ID_EUROPE_BERLIN = "Europe/Berlin"

            private const val BASE_API_PATH = "/api/advent"
            private const val DAYS_ENDPOINT = "$BASE_API_PATH/days"

            private const val PROP_FIXED_TODAY = "advent.fixed-today"

            private const val ERROR_FIELD = "error"
            private const val ERROR_NOT_FOUND = "not found"
            private const val ERROR_LOCKED = "locked"

            private const val DOOR_IMAGE_PATTERN = "/images/doors/day-%02d.png"
            private val DOOR_IMAGE_REGEX = Regex("""^/images/doors/day-\d{2}\.png$""")

            private const val TITLE_PREFIX = "Türchen "
        }

        // kleines DTO für Fehlerantworten wie {"error":"locked"}
        data class ErrorResponse(val error: String?)

        private fun currentDateForTest(): LocalDate {
            val fixed = env.getProperty(PROP_FIXED_TODAY)
            return if (!fixed.isNullOrBlank()) {
                LocalDate.parse(fixed)
            } else {
                LocalDate.now(ZoneId.of(ZONE_ID_EUROPE_BERLIN))
            }
        }

        private fun getBody(path: String): String =
            mockMvc.perform(get(path))
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString

        @Test
        fun `GET days returns 24 items from 1 to 24`() {
            val json = getBody(DAYS_ENDPOINT)
            val days: List<AdventDayDto> = jsonMapper.readValue(json)

            assertEquals(LAST_DAY, days.size, "es sollten 24 Türchen zurückkommen")
            assertEquals((FIRST_DAY..LAST_DAY).toList(), days.map { it.day }, "Tage sollten 1..24 sein")
        }

        @Test
        fun `days have correct dates and titles`() {
            val json = getBody(DAYS_ENDPOINT)
            val days: List<AdventDayDto> = jsonMapper.readValue(json)

            days.forEach { dto ->
                assertEquals(ADVENT_YEAR, dto.date.year)
                assertEquals(ADVENT_MONTH_VALUE, dto.date.monthValue)
                assertEquals(dto.day, dto.date.dayOfMonth)
                assertEquals("$TITLE_PREFIX${dto.day}", dto.title)
            }
        }

        @Test
        fun `days unlocked flag follows the business rule`() {
            val json = getBody(DAYS_ENDPOINT)
            val days: List<AdventDayDto> = jsonMapper.readValue(json)

            val today = currentDateForTest()

            days.forEach { dto ->
                val expectedUnlocked = !today.isBefore(dto.date)
                assertEquals(
                    expectedUnlocked,
                    dto.unlocked,
                    "Unlocked-Flag stimmt nicht für Tag ${dto.day}",
                )
            }
        }

        @Test
        fun `solutionUnlocked follows the business rule`() {
            val json = getBody(DAYS_ENDPOINT)
            val days: List<AdventDayDto> = jsonMapper.readValue(json)

            val today = currentDateForTest() // liest advent.fixed-today oder LocalDate.now

            days.forEach { dto ->
                val expected = today.isAfter(dto.date)
                assertEquals(
                    expected,
                    dto.solutionUnlocked,
                    "solutionUnlocked stimmt nicht für Tag ${dto.day}",
                )
            }
        }

        @Test
        fun `GET one sets solutionUnlocked only after day`() {
            val today = currentDateForTest()

            // nur testen, wenn wir vor oder am 01.12. sind
            val date = LocalDate.of(ADVENT_YEAR, ADVENT_MONTH_VALUE, FIRST_DAY)
            if (today.isBefore(date) || today.isEqual(date)) {
                val body = getBody("$DAYS_ENDPOINT/$ADVENT_FIRST_DAY")
                val dto: AdventDayDto = jsonMapper.readValue(body)
                assertTrue(dto.unlocked)
                assertFalse(dto.solutionUnlocked)
            }
        }

        @Test
        fun `imageUrl uses per-day door icons`() {
            val json = getBody(DAYS_ENDPOINT)
            val days: List<AdventDayDto> = jsonMapper.readValue(json)

            days.forEach { dto ->
                val expected = DOOR_IMAGE_PATTERN.format(dto.day)
                assertEquals(
                    expected,
                    dto.imageUrl,
                    "imageUrl stimmt nicht für Tag ${dto.day}",
                )
            }
        }

        @Test
        fun `imageUrl has correct door path pattern`() {
            val json = getBody(DAYS_ENDPOINT)
            val days: List<AdventDayDto> = jsonMapper.readValue(json)

            days.forEach { dto ->
                assertTrue(
                    dto.imageUrl != null && DOOR_IMAGE_REGEX.matches(dto.imageUrl!!),
                    "imageUrl hat nicht das erwartete Format für Tag ${dto.day}: ${dto.imageUrl}",
                )
            }
        }

        @Test
        fun `GET one returns not-found error for invalid day`() {
            mockMvc.perform(get("$DAYS_ENDPOINT/$INVALID_DAY_LOW"))
                .andExpect(status().isOk) // Controller liefert Map, kein HTTP-404
                .andExpect(jsonPath("$.${ERROR_FIELD}").value(ERROR_NOT_FOUND))

            mockMvc.perform(get("$DAYS_ENDPOINT/$INVALID_DAY_HIGH"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.${ERROR_FIELD}").value(ERROR_NOT_FOUND))
        }

        @Test
        fun `GET one for day 1 returns either locked error or AdventDayDto`() {
            val body = getBody("$DAYS_ENDPOINT/$ADVENT_FIRST_DAY")

            // Erst versuchen wir, ob es eine Fehler-Response ist
            val errorResponse = runCatching {
                jsonMapper.readValue<ErrorResponse>(body)
            }.getOrNull()

            if (errorResponse?.error != null) {
                assertEquals(
                    ERROR_LOCKED,
                    errorResponse.error,
                    "Tag 1 darf nur 'locked' als Fehler haben",
                )
            } else {
                // Dann muss es ein valides AdventDayDto sein
                val dto: AdventDayDto = jsonMapper.readValue(body)
                assertEquals(ADVENT_FIRST_DAY, dto.day)
                assertEquals("$TITLE_PREFIX$ADVENT_FIRST_DAY", dto.title)
            }
        }

        @Test
        fun `GET one future day returns locked error (solange vor 24_12_2025 ausgeführt)`() {
            val today = LocalDate.now(ZoneId.of(ZONE_ID_EUROPE_BERLIN))
            val futureDate = LocalDate.of(ADVENT_YEAR, ADVENT_MONTH_VALUE, ADVENT_LAST_DAY)

            if (today.isBefore(futureDate)) {
                mockMvc.perform(get("$DAYS_ENDPOINT/$ADVENT_LAST_DAY"))
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.${ERROR_FIELD}").value(ERROR_LOCKED))
            }
        }
    }
