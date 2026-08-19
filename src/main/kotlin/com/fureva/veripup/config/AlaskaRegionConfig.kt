package com.fureva.veripup.config

/**
 * Canonical Alaska region -> city list used for onboarding, search, and discovery filters.
 * Fureva Veripup is a statewide Alaska marketplace, so this replaces a generic 50-state list.
 */
object AlaskaRegionConfig {
    val citiesByRegion: Map<String, List<String>> = mapOf(
        "Anchorage" to listOf("Anchorage", "Eagle River", "Chugiak", "Girdwood"),
        "Mat-Su" to listOf("Palmer", "Wasilla", "Big Lake", "Houston", "Willow"),
        "Fairbanks North Star" to listOf("Fairbanks", "North Pole", "Badger", "College", "Ester"),
        "Kenai Peninsula" to listOf("Kenai", "Soldotna", "Homer", "Seward", "Sterling"),
        "Juneau" to listOf("Juneau"),
        "Southeast" to listOf("Sitka", "Ketchikan", "Petersburg", "Wrangell", "Haines"),
        "Interior" to listOf("Delta Junction", "Tok", "Nenana"),
        "Kodiak" to listOf("Kodiak"),
        "Southwest" to listOf("Dillingham", "Bethel"),
        "Northern" to listOf("Utqiagvik", "Nome", "Kotzebue")
    )

    fun citiesFor(region: String): List<String> =
        citiesByRegion.entries.firstOrNull { it.key.equals(region, ignoreCase = true) }?.value ?: emptyList()

    fun regionFor(city: String): String? =
        citiesByRegion.entries.firstOrNull { (_, cities) -> cities.any { it.equals(city, ignoreCase = true) } }?.key
}
