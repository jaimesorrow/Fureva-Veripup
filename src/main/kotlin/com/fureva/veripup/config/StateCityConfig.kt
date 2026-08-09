package com.fureva.veripup.config

object StateCityConfig {
    val topCitiesByState: Map<String, List<String>> = mapOf(
        "AK" to listOf("Anchorage", "Fairbanks", "Juneau", "Bethel", "Utqiagvik")
    )

    fun citiesFor(stateCode: String): List<String> = topCitiesByState[stateCode.uppercase()] ?: emptyList()
}
