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
private fun currentDateForTest(): LocalDate {
            val fixed = env.getProperty("advent.fixed-today")
            return if (!fixed.isNullOrBlank()) {
                LocalDate.parse(fixed)
            } else {
                LocalDate.now(ZoneId.of("Europe/Berlin"))
            }
        }

        @Test
        fun `GET days returns 24 items from 1 to 24`() {
            val result =
                mockMvc.perform(get("/api/advent/days"))
                    .andExpect(status().isOk)
                    .andReturn()

            val json = result.response.contentAsString
            val days: List<AdventDayDto> = jsonMapper.readValue(json)

            assertEquals(24, days.size, "es sollten 24 Türchen zurückkommen")
            assertEquals((1..24).toList(), days.map { it.day }, "Tage sollten 1..24 sein")
        }

        @Test
        fun `days have correct dates and titles`() {
            val result =
                mockMvc.perform(get("/api/advent/days"))
                    .andExpect(status().isOk)
                    .andReturn()

            val days: List<AdventDayDto> = jsonMapper.readValue(result.response.contentAsString)

            days.forEach { dto ->
                assertEquals(2025, dto.date.year)
                assertEquals(12, dto.date.monthValue)
                assertEquals(dto.day, dto.date.dayOfMonth)

                assertEquals("Türchen ${dto.day}", dto.title)
            }
        }

        @Test
        fun `days unlocked flag follows the business rule`() {
            val result =
                mockMvc.perform(get("/api/advent/days"))
                    .andExpect(status().isOk)
                    .andReturn()

            val days: List<AdventDayDto> = jsonMapper.readValue(result.response.contentAsString)

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
            val result =
                mockMvc.perform(get("/api/advent/days"))
                    .andExpect(status().isOk)
                    .andReturn()

            val days: List<AdventDayDto> = jsonMapper.readValue(result.response.contentAsString)

            val today = currentDateForTest() // wie vorher schon gebaut: liest advent.fixed-today oder LocalDate.now

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

            // nur testen, wenn wir vor dem 24.12. sind
            val date = LocalDate.of(2025, 12, 1)
            if (today.isBefore(date) || today.isEqual(date)) {
                // am Tag selbst: Lösung noch gesperrt
                val result =
                    mockMvc.perform(get("/api/advent/days/1"))
                        .andExpect(status().isOk)
                        .andReturn()

                val dto: AdventDayDto = jsonMapper.readValue(result.response.contentAsString)
                assertTrue(dto.unlocked)
                assertFalse(dto.solutionUnlocked)
            }
        }

        @Test
        fun `imageUrl uses per-day door icons`() {
            val result =
                mockMvc.perform(get("/api/advent/days"))
                    .andExpect(status().isOk)
                    .andReturn()

            val days: List<AdventDayDto> = jsonMapper.readValue(result.response.contentAsString)

            days.forEach { dto ->
                // Erwarteter Pfad: /images/doors/day-XX.png
                val expected = "/images/doors/day-%02d.png".format(dto.day)
                assertEquals(
                    expected,
                    dto.imageUrl,
                    "imageUrl stimmt nicht für Tag ${dto.day}",
                )
            }
        }

        @Test
        fun `imageUrl has correct door path pattern`() {
            val result =
                mockMvc.perform(get("/api/advent/days"))
                    .andExpect(status().isOk)
                    .andReturn()

            val days: List<AdventDayDto> = jsonMapper.readValue(result.response.contentAsString)

            val regex = Regex("""^/images/doors/day-\d{2}\.png$""")

            days.forEach { dto ->
                assertTrue(
                    dto.imageUrl != null && regex.matches(dto.imageUrl!!),
                    "imageUrl hat nicht das erwartete Format für Tag ${dto.day}: ${dto.imageUrl}",
                )
            }
        }

        @Test
        fun `GET one returns not-found error for invalid day`() {
            mockMvc.perform(get("/api/advent/days/0"))
                .andExpect(status().isOk) // Controller liefert Map, kein HTTP-404
                .andExpect(jsonPath("$.error").value("not found"))

            mockMvc.perform(get("/api/advent/days/25"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.error").value("not found"))
        }

        @Test
        fun `GET one for day 1 returns either locked error or AdventDayDto`() {
            val result =
                mockMvc.perform(get("/api/advent/days/1"))
                    .andExpect(status().isOk)
                    .andReturn()

            val body = result.response.contentAsString

            // Gibt es ein error-Feld?
            if (body.contains("\"error\"")) {
                val node = jsonMapper.readTree(body)
                assertEquals("locked", node["error"].asText(), "Tag 1 darf nur 'locked' als Fehler haben")
            } else {
                // Dann muss es ein valides AdventDayDto sein
                val dto: AdventDayDto = jsonMapper.readValue(body)
                assertEquals(1, dto.day)
                assertEquals("Türchen 1", dto.title)
            }
        }

        @Test
        fun `GET one future day returns locked error (solange vor 24_12_2025 ausgeführt)`() {
            val today = LocalDate.now(ZoneId.of("Europe/Berlin"))
            val futureDate = LocalDate.of(2025, 12, 24)

            // nur sinnvoll, solange wir VOR dem 24.12.2025 sind
            if (today.isBefore(futureDate)) {
                mockMvc.perform(get("/api/advent/days/24"))
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.error").value("locked"))
            }
        }
    }
