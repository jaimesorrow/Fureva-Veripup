package com.fureva.veripup

import com.fureva.veripup.model.CampaignStage
import com.fureva.veripup.model.Dog
import com.fureva.veripup.model.DogType
import com.fureva.veripup.model.Litter
import com.fureva.veripup.model.LitterStatus
import com.fureva.veripup.model.PuppyStatus
import com.fureva.veripup.service.LitterCampaignService
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LitterCampaignServiceTests {

    private val service = LitterCampaignService()
    private val now = Instant.parse("2026-05-01T00:00:00Z")
    private val today = LocalDate.parse("2026-05-01")

    private fun puppy(
        dob: LocalDate? = today.minusWeeks(9),
        breed: String? = "Golden Retriever",
        price: BigDecimal? = BigDecimal("1800"),
        hasPhoto: Boolean = true,
        status: PuppyStatus? = null
    ) = Dog(
        id = "d1",
        breederProfileId = "bp1",
        litterId = "l1",
        dogType = DogType.PUPPY,
        name = "Biscuit",
        dateOfBirth = dob,
        breed = breed,
        priceUsd = price,
        hasPhoto = hasPhoto,
        status = status,
        createdAt = now,
        updatedAt = now
    )

    private fun litter(
        title: String = "Anchorage Golden Litter",
        breed: String = "Golden Retriever",
        city: String = "Anchorage",
        accuracyConfirmed: Boolean = true
    ) = Litter(
        id = "l1",
        breederProfileId = "bp1",
        title = title,
        slug = "anchorage-golden-litter",
        breed = breed,
        city = city,
        campaignStage = CampaignStage.TAKING_APPLICATIONS,
        accuracyConfirmed = accuracyConfirmed,
        createdAt = now,
        updatedAt = now
    )

    // ── canAdvanceStage ──────────────────────────────────────────────────────

    @Test
    fun canAdvanceToLaterStage() {
        assertTrue(service.canAdvanceStage(CampaignStage.PLANNED, CampaignStage.BORN))
    }

    @Test
    fun cannotAdvanceToEarlierStage() {
        assertFalse(service.canAdvanceStage(CampaignStage.BORN, CampaignStage.PLANNED))
    }

    @Test
    fun cannotAdvanceToSameStage() {
        assertFalse(service.canAdvanceStage(CampaignStage.BORN, CampaignStage.BORN))
    }

    // ── missingPuppyListingFields ────────────────────────────────────────────

    @Test
    fun noMissingFieldsForCompletePuppy() {
        assertTrue(service.missingPuppyListingFields(puppy()).isEmpty())
    }

    @Test
    fun missingFieldsListsDateOfBirthWhenAbsent() {
        val missing = service.missingPuppyListingFields(puppy(dob = null))
        assertTrue(missing.contains("Date of birth"))
    }

    @Test
    fun missingFieldsListsBreedWhenAbsent() {
        val missing = service.missingPuppyListingFields(puppy(breed = null))
        assertTrue(missing.contains("Breed"))
    }

    @Test
    fun missingFieldsListsPriceWhenAbsent() {
        val missing = service.missingPuppyListingFields(puppy(price = null))
        assertTrue(missing.contains("Price"))
    }

    @Test
    fun missingFieldsListsPhotoWhenAbsent() {
        val missing = service.missingPuppyListingFields(puppy(hasPhoto = false))
        assertTrue(missing.any { it.contains("photo", ignoreCase = true) })
    }

    @Test
    fun missingPuppyListingFieldsRejectsNonPuppyDogType() {
        assertFailsWith<IllegalArgumentException> {
            service.missingPuppyListingFields(puppy().copy(dogType = DogType.DAM))
        }
    }

    // ── isOldEnoughForGoHome / canMarkAvailable ──────────────────────────────

    @Test
    fun isOldEnoughAtExactlyEightWeeks() {
        assertTrue(service.isOldEnoughForGoHome(today.minusWeeks(8), today))
    }

    @Test
    fun isNotOldEnoughBeforeEightWeeks() {
        assertFalse(service.isOldEnoughForGoHome(today.minusWeeks(7), today))
    }

    @Test
    fun canMarkAvailableWhenCompleteAndOldEnough() {
        assertTrue(service.canMarkAvailable(puppy(dob = today.minusWeeks(8)), today))
    }

    @Test
    fun cannotMarkAvailableWhenTooYoung() {
        assertFalse(service.canMarkAvailable(puppy(dob = today.minusWeeks(3)), today))
    }

    @Test
    fun cannotMarkAvailableWhenMissingRequiredFields() {
        assertFalse(service.canMarkAvailable(puppy(price = null), today))
    }

    @Test
    fun cannotMarkAvailableWithoutDateOfBirth() {
        assertFalse(service.canMarkAvailable(puppy(dob = null), today))
    }

    // ── recalculateAvailableCount ────────────────────────────────────────────

    @Test
    fun recalculateAvailableCountOnlyCountsAvailablePuppies() {
        val puppies = listOf(
            puppy().copy(id = "p1", status = PuppyStatus.AVAILABLE),
            puppy().copy(id = "p2", status = PuppyStatus.RESERVED),
            puppy().copy(id = "p3", status = PuppyStatus.AVAILABLE),
            puppy().copy(id = "p4", status = PuppyStatus.PLACED)
        )
        assertEquals(2, service.recalculateAvailableCount(puppies))
    }

    @Test
    fun recalculateAvailableCountIgnoresNonPuppyDogs() {
        val puppies = listOf(
            puppy().copy(id = "dam1", dogType = DogType.DAM, status = PuppyStatus.AVAILABLE)
        )
        assertEquals(0, service.recalculateAvailableCount(puppies))
    }

    // ── publish ───────────────────────────────────────────────────────────────

    @Test
    fun canPublishCompleteLitter() {
        assertTrue(service.canPublish(litter()))
    }

    @Test
    fun cannotPublishWithoutAccuracyConfirmation() {
        assertFalse(service.canPublish(litter(accuracyConfirmed = false)))
    }

    @Test
    fun cannotPublishWithBlankTitle() {
        assertFalse(service.canPublish(litter(title = "")))
    }

    @Test
    fun publishSetsPublishedStatusAndTimestamp() {
        val published = service.publish(litter(), now)
        assertEquals(LitterStatus.PUBLISHED, published.status)
        assertEquals(now, published.publishedAt)
    }

    @Test
    fun publishFailsWhenFieldsMissing() {
        assertFailsWith<IllegalArgumentException> {
            service.publish(litter(accuracyConfirmed = false), now)
        }
    }
}
