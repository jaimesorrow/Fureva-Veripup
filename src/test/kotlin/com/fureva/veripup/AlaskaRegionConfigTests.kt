package com.fureva.veripup

import com.fureva.veripup.config.AlaskaRegionConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AlaskaRegionConfigTests {

    @Test
    fun anchorageRegionContainsAnchorage() {
        assertTrue(AlaskaRegionConfig.citiesFor("Anchorage").contains("Anchorage"))
    }

    @Test
    fun matSuRegionContainsPalmer() {
        assertTrue(AlaskaRegionConfig.citiesFor("Mat-Su").contains("Palmer"))
    }

    @Test
    fun kenaiPeninsulaContainsKenai() {
        assertTrue(AlaskaRegionConfig.citiesFor("Kenai Peninsula").contains("Kenai"))
    }

    @Test
    fun fairbanksRegionContainsFairbanks() {
        assertTrue(AlaskaRegionConfig.citiesFor("Fairbanks North Star").contains("Fairbanks"))
    }

    @Test
    fun citiesForLookupIsCaseInsensitive() {
        assertEquals(AlaskaRegionConfig.citiesFor("anchorage"), AlaskaRegionConfig.citiesFor("Anchorage"))
    }

    @Test
    fun citiesForUnknownRegionReturnsEmptyList() {
        assertEquals(emptyList<String>(), AlaskaRegionConfig.citiesFor("Nowhere"))
    }

    @Test
    fun eachRegionHasAtLeastOneCity() {
        for ((region, cities) in AlaskaRegionConfig.citiesByRegion) {
            assertTrue(cities.isNotEmpty(), "Region $region has no cities")
        }
    }

    // ── regionFor ─────────────────────────────────────────────────────────────

    @Test
    fun regionForPalmerIsMatSu() {
        assertEquals("Mat-Su", AlaskaRegionConfig.regionFor("Palmer"))
    }

    @Test
    fun regionForKenaiIsKenaiPeninsula() {
        assertEquals("Kenai Peninsula", AlaskaRegionConfig.regionFor("Kenai"))
    }

    @Test
    fun regionForUnknownCityReturnsNull() {
        assertNull(AlaskaRegionConfig.regionFor("Nowhere"))
    }

    @Test
    fun regionForIsCaseInsensitive() {
        assertEquals("Anchorage", AlaskaRegionConfig.regionFor("anchorage"))
    }
}
