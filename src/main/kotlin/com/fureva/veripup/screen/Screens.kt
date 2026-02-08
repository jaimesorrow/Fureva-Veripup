package com.fureva.veripup.screen

sealed class UserScreen(val route: String) {
    data object BrowseSearch : UserScreen("user/browse-search")
    data object BreederProfile : UserScreen("user/breeder-profile")
    data object PuppyListing : UserScreen("user/puppy-listing")
    data object FollowManagement : UserScreen("user/follows")
    data object AdoptionApplication : UserScreen("user/adoption-form")
    data object PaymentReceipts : UserScreen("user/payments")
    data object SettingsAlerts : UserScreen("user/settings-alerts")
}

sealed class BreederScreen(val route: String) {
    data object VerificationOnboarding : BreederScreen("breeder/onboarding")
    data object ProfileEditor : BreederScreen("breeder/profile")
    data object PuppyCrud : BreederScreen("breeder/puppies")
    data object LitterMilestones : BreederScreen("breeder/litters")
    data object SubscriptionPayouts : BreederScreen("breeder/billing")
    data object WeeklyTasks : BreederScreen("breeder/weekly-tasks")
    data object TermsPolicy : BreederScreen("breeder/policy")
}

sealed class AdminScreen(val route: String) {
    data object VerificationQueue : AdminScreen("admin/verification-queue")
    data object FraudFlags : AdminScreen("admin/fraud")
    data object ClinicCallChecklist : AdminScreen("admin/clinic-calls")
    data object AppealReview : AdminScreen("admin/appeals")
    data object EnforcementActions : AdminScreen("admin/enforcement")
    data object AuditLogs : AdminScreen("admin/audit")
}
