package com.fureva.veripup

import com.fureva.veripup.config.StateCityConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StateCityConfigTests {

    // ── citiesFor ─────────────────────────────────────────────────────────────

    @Test
    fun citiesForKnownStateReturnsNonEmptyList() {
        val cities = StateCityConfig.citiesFor("AK")
        assertTrue(cities.isNotEmpty())
    }

    @Test
    fun citiesForAlaskaContainsAnchorage() {
        assertTrue(StateCityConfig.citiesFor("AK").contains("Anchorage"))
    }

    @Test
    fun alaskaCitiesMatchLargestCityPerRegionList() {
        assertEquals(
            listOf("Anchorage", "Fairbanks", "Juneau", "Bethel", "Utqiagvik"),
            StateCityConfig.citiesFor("AK")
        )
    }

    @Test
    fun nonAlaskaStatesReturnEmptyList() {
        assertEquals(emptyList<String>(), StateCityConfig.citiesFor("CA"))
        assertEquals(emptyList<String>(), StateCityConfig.citiesFor("TX"))
    }

    @Test
    fun citiesForUnknownStateReturnsEmptyList() {
        assertEquals(emptyList<String>(), StateCityConfig.citiesFor("XX"))
    }

    @Test
    fun citiesForLookupIsCaseInsensitive() {
        val upper = StateCityConfig.citiesFor("AK")
        val lower = StateCityConfig.citiesFor("ak")
        assertEquals(upper, lower)
    }

    @Test
    fun citiesForEmptyStringReturnsEmptyList() {
        assertEquals(emptyList<String>(), StateCityConfig.citiesFor(""))
    }

    // ── topCitiesByState coverage ─────────────────────────────────────────────

    @Test
    fun onlyAlaskaIsPresent() {
        assertEquals(setOf("AK"), StateCityConfig.topCitiesByState.keys)
    }

    @Test
    fun eachStateHasAtLeastOneCity() {
        for ((state, cities) in StateCityConfig.topCitiesByState) {
            assertTrue(cities.isNotEmpty(), "State $state has no cities")
        }
    }

    @Test
    fun alaskaHasOneLargestCityPerRegion() {
        for ((state, cities) in StateCityConfig.topCitiesByState) {
            assertEquals(5, cities.size, "State $state should have one city for each Alaska region")
        }
    }
}
