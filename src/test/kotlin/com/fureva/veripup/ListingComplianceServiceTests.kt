package com.fureva.veripup

import com.fureva.veripup.service.ListingComplianceService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ListingComplianceServiceTests {

    private val service = ListingComplianceService()

    // ── violatesUsdaClaim ─────────────────────────────────────────────────────

    @Test
    fun violatesUsdaClaimWhenUnlicensedAndClaimed() {
        assertTrue(service.violatesUsdaClaim("We are USDA approved!", licenseVerified = false))
    }

    @Test
    fun doesNotViolateUsdaClaimWhenLicenseVerified() {
        assertFalse(service.violatesUsdaClaim("We are USDA approved!", licenseVerified = true))
    }

    @Test
    fun doesNotViolateUsdaClaimWhenNotMentioned() {
        assertFalse(service.violatesUsdaClaim("Happy, healthy puppies", licenseVerified = false))
    }

    @Test
    fun violatesUsdaClaimIsCaseInsensitive() {
        assertTrue(service.violatesUsdaClaim("usda-approved breeder", licenseVerified = false))
    }

    // ── violatesHealthCertifiedClaim ─────────────────────────────────────────

    @Test
    fun violatesHealthCertifiedClaimWhenPhraseUsed() {
        assertTrue(service.violatesHealthCertifiedClaim("All puppies are health certified"))
    }

    @Test
    fun doesNotViolateWithDocumentBackedLanguage() {
        assertFalse(service.violatesHealthCertifiedClaim("Vet exam record uploaded"))
    }

    // ── isProhibitedSpecies ───────────────────────────────────────────────────

    @Test
    fun flagsProhibitedWildlifeSpecies() {
        assertTrue(service.isProhibitedSpecies("Wolf-dog hybrid"))
    }

    @Test
    fun allowsOrdinaryDogBreeds() {
        assertFalse(service.isProhibitedSpecies("Alaskan Malamute"))
    }

    // ── publicLocationDisplay ─────────────────────────────────────────────────

    @Test
    fun publicLocationDisplayCombinesCityAndRegion() {
        assertEquals("Anchorage, Anchorage", service.publicLocationDisplay("Anchorage", "Anchorage"))
    }

    @Test
    fun publicLocationDisplayFallsBackToCityOnly() {
        assertEquals("Kenai", service.publicLocationDisplay("Kenai", null))
    }

    // ── complianceIssues ──────────────────────────────────────────────────────

    @Test
    fun complianceIssuesAggregatesAllViolations() {
        val issues = service.complianceIssues(
            listingText = "USDA approved and health certified pups",
            breed = "Fox hybrid",
            usdaLicenseVerified = false
        )
        assertEquals(3, issues.size)
    }

    @Test
    fun complianceIssuesEmptyForCleanListing() {
        val issues = service.complianceIssues(
            listingText = "Vet exam record uploaded, genetic test document verified",
            breed = "Golden Retriever",
            usdaLicenseVerified = false
        )
        assertTrue(issues.isEmpty())
    }
}
