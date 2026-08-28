package com.fureva.veripup.screen

/**
 * Navigation destinations available to buyers / adopters on the platform.
 *
 * Each object exposes a [route] string that uniquely identifies the screen
 * within the navigation graph.
 */
sealed class UserScreen(val route: String) {
    /** Browse and search verified breeder listings and available puppies. */
    data object BrowseSearch : UserScreen("user/browse-search")
    /** View a specific breeder's public profile and trust badge. */
    data object BreederProfile : UserScreen("user/breeder-profile")
    /** View a specific puppy listing with photos, breed info, and price. */
    data object PuppyListing : UserScreen("user/puppy-listing")
    /** Manage the list of verified breeders the user is following. */
    data object FollowManagement : UserScreen("user/follows")
    /** Submit an adoption application for a puppy. */
    data object AdoptionApplication : UserScreen("user/adoption-form")
    /** View past payment receipts for deposits and completed adoptions. */
    data object PaymentReceipts : UserScreen("user/payments")
    /** Configure SMS alert preferences and notification opt-in settings. */
    data object SettingsAlerts : UserScreen("user/settings-alerts")
}

/**
 * Navigation destinations available to verified breeders managing their
 * presence on the platform.
 *
 * Each object exposes a [route] string that uniquely identifies the screen
 * within the navigation graph.
 */
sealed class BreederScreen(val route: String) {
    /** Step-by-step onboarding and verification flow for new breeders. */
    data object VerificationOnboarding : BreederScreen("breeder/onboarding")
    /** Edit the breeder's public profile, bio, and location. */
    data object ProfileEditor : BreederScreen("breeder/profile")
    /** Create, update, and remove individual puppy listings. */
    data object PuppyCrud : BreederScreen("breeder/puppies")
    /** Record and update litter milestone events (vet confirmation, due date, etc.). */
    data object LitterMilestones : BreederScreen("breeder/litters")
    /** View subscription status, payout history, and billing settings. */
    data object SubscriptionPayouts : BreederScreen("breeder/billing")
    /** View and complete recurring compliance tasks required by platform policy. */
    data object WeeklyTasks : BreederScreen("breeder/weekly-tasks")
    /** Review the platform's current terms of service and usage policies. */
    data object TermsPolicy : BreederScreen("breeder/policy")
}

/**
 * Navigation destinations available to platform administrators.
 *
 * Each object exposes a [route] string that uniquely identifies the screen
 * within the navigation graph.
 */
sealed class AdminScreen(val route: String) {
    /** Review and action pending breeder verification submissions. */
    data object VerificationQueue : AdminScreen("admin/verification-queue")
    /** Investigate breeders flagged for suspected fraudulent activity. */
    data object FraudFlags : AdminScreen("admin/fraud")
    /** Track and log outbound calls to veterinary clinics for verification. */
    data object ClinicCallChecklist : AdminScreen("admin/clinic-calls")
    /** Review and resolve breeder appeals against enforcement actions. */
    data object AppealReview : AdminScreen("admin/appeals")
    /** Initiate and record enforcement actions (deactivations, warnings). */
    data object EnforcementActions : AdminScreen("admin/enforcement")
    /** View the immutable audit log of all significant platform events. */
    data object AuditLogs : AdminScreen("admin/audit")
}
