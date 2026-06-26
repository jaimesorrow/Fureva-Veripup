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
    fun citiesForCaliforniaContainsLosAngeles() {
        assertTrue(StateCityConfig.citiesFor("CA").contains("Los Angeles"))
    }

    @Test
    fun citiesForTexasContainsHouston() {
        assertTrue(StateCityConfig.citiesFor("TX").contains("Houston"))
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
    fun allFiftyStatesArePresent() {
        val expectedStateCodes = setOf(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
        )
        assertEquals(expectedStateCodes, StateCityConfig.topCitiesByState.keys)
    }

    @Test
    fun eachStateHasAtLeastOneCity() {
        for ((state, cities) in StateCityConfig.topCitiesByState) {
            assertTrue(cities.isNotEmpty(), "State $state has no cities")
        }
    }

    @Test
    fun eachStateHasAtLeastSevenCities() {
        for ((state, cities) in StateCityConfig.topCitiesByState) {
            assertTrue(cities.size >= 7, "State $state has fewer than 7 cities: ${cities.size}")
        }
    }
}
