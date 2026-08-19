package com.fureva.veripup.service

import com.fureva.veripup.model.CampaignStage
import com.fureva.veripup.model.Dog
import com.fureva.veripup.model.DogType
import com.fureva.veripup.model.Litter
import com.fureva.veripup.model.LitterStatus
import com.fureva.veripup.model.PuppyStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Campaign stage, puppy availability, and publishing rules for a litter page.
 *
 * The platform defaults to a cautious 8-week minimum go-home age. This is a product default,
 * not a representation of Alaska law; a state-specific rule requires legal review.
 */
class LitterCampaignService {

    companion object {
        const val MINIMUM_GO_HOME_AGE_WEEKS = 8L

        private val STAGE_ORDER = listOf(
            CampaignStage.PLANNED,
            CampaignStage.CONFIRMED_PREGNANCY,
            CampaignStage.BORN,
            CampaignStage.TAKING_APPLICATIONS,
            CampaignStage.RESERVATION_OPEN,
            CampaignStage.READY_FOR_HOMES,
            CampaignStage.CLOSED
        )
    }

    fun canAdvanceStage(from: CampaignStage, to: CampaignStage): Boolean {
        val fromIndex = STAGE_ORDER.indexOf(from)
        val toIndex = STAGE_ORDER.indexOf(to)
        return toIndex > fromIndex
    }

    /** Required fields per puppy listing: date of birth, breed, price, and at least one photo. */
    fun missingPuppyListingFields(puppy: Dog): List<String> {
        require(puppy.dogType == DogType.PUPPY) { "Only puppies can be listed for sale" }
        val missing = mutableListOf<String>()
        if (puppy.dateOfBirth == null) missing += "Date of birth"
        if (puppy.breed.isNullOrBlank()) missing += "Breed"
        if (puppy.priceUsd == null) missing += "Price"
        if (!puppy.hasPhoto) missing += "At least one current photo"
        return missing
    }

    fun isOldEnoughForGoHome(dateOfBirth: LocalDate, asOf: LocalDate): Boolean =
        ChronoUnit.WEEKS.between(dateOfBirth, asOf) >= MINIMUM_GO_HOME_AGE_WEEKS

    /**
     * A puppy may only be publicly marked AVAILABLE once listing fields are complete and it has
     * reached the minimum go-home age. Other statuses (pending/reserved/placed/not_available)
     * are always allowed since they do not represent an open, unsupervised sale.
     */
    fun canMarkAvailable(puppy: Dog, asOf: LocalDate): Boolean {
        val dob = puppy.dateOfBirth ?: return false
        if (missingPuppyListingFields(puppy).isNotEmpty()) return false
        return isOldEnoughForGoHome(dob, asOf)
    }

    fun recalculateAvailableCount(puppies: List<Dog>): Int =
        puppies.count { it.dogType == DogType.PUPPY && it.status == PuppyStatus.AVAILABLE }

    /** Litter page must have its core public fields before it can be published. */
    fun missingPublishFields(litter: Litter): List<String> {
        val missing = mutableListOf<String>()
        if (litter.title.isBlank()) missing += "Title"
        if (litter.breed.isBlank()) missing += "Breed"
        if (litter.city.isBlank()) missing += "City"
        if (!litter.accuracyConfirmed) missing += "Breeder accuracy confirmation"
        return missing
    }

    fun canPublish(litter: Litter): Boolean = missingPublishFields(litter).isEmpty()

    fun publish(litter: Litter, now: java.time.Instant): Litter {
        require(canPublish(litter)) { "Litter is missing required fields: ${missingPublishFields(litter)}" }
        return litter.copy(status = LitterStatus.PUBLISHED, publishedAt = now, updatedAt = now)
    }
}
