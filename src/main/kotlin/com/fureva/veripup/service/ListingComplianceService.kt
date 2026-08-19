package com.fureva.veripup.service

/**
 * Content-level guardrails for public listing copy, independent of any single field's data
 * completeness. These check phrasing rules called out explicitly in the platform's business
 * rules: no unverified USDA claims, and no blanket "health certified" language.
 */
class ListingComplianceService {

    private val usdaClaimPattern = Regex("usda[\\s-]*approved", RegexOption.IGNORE_CASE)
    private val healthCertifiedPattern = Regex("health[\\s-]*certified", RegexOption.IGNORE_CASE)

    private val prohibitedSpecies = setOf(
        "wolf", "wolf-dog", "wolfdog", "fox", "coyote", "bear", "big cat", "exotic"
    )

    fun violatesUsdaClaim(text: String, licenseVerified: Boolean): Boolean =
        !licenseVerified && usdaClaimPattern.containsMatchIn(text)

    fun violatesHealthCertifiedClaim(text: String): Boolean = healthCertifiedPattern.containsMatchIn(text)

    fun isProhibitedSpecies(breedOrSpecies: String): Boolean =
        prohibitedSpecies.any { breedOrSpecies.contains(it, ignoreCase = true) }

    /** Only a city or region may be shown publicly; never a street address. */
    fun publicLocationDisplay(city: String, region: String?): String =
        if (region.isNullOrBlank()) city else "$city, $region"

    fun complianceIssues(listingText: String, breed: String, usdaLicenseVerified: Boolean): List<String> {
        val issues = mutableListOf<String>()
        if (violatesUsdaClaim(listingText, usdaLicenseVerified)) {
            issues += "Cannot claim USDA approval without a verified license"
        }
        if (violatesHealthCertifiedClaim(listingText)) {
            issues += "Cannot use 'health certified'; use specific document-backed statements instead"
        }
        if (isProhibitedSpecies(breed)) {
            issues += "Listing references a prohibited wildlife or exotic species"
        }
        return issues
    }
}
