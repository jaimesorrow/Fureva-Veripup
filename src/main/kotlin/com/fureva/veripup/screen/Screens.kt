package com.fureva.veripup.screen

sealed class BuyerScreen(val route: String) {
    data object Home : BuyerScreen("buyer/home")
    data object SearchResults : BuyerScreen("buyer/search")
    data object LitterDetail : BuyerScreen("buyer/litters/detail")
    data object BreederProfile : BuyerScreen("buyer/breeder-profile")
    data object ParentDogProfile : BuyerScreen("buyer/parent-dog")
    data object PuppyDetail : BuyerScreen("buyer/puppy-detail")
    data object WaitlistSignup : BuyerScreen("buyer/waitlist")
    data object Application : BuyerScreen("buyer/application")
    data object SavedLitters : BuyerScreen("buyer/saved")
    data object Messages : BuyerScreen("buyer/messages")
    data object ReservationStatus : BuyerScreen("buyer/reservation")
    data object PuppyRecords : BuyerScreen("buyer/puppy-records")
    data object ReportListingModal : BuyerScreen("buyer/report")
}

sealed class BreederScreen(val route: String) {
    data object OnboardingVerification : BreederScreen("breeder/onboarding")
    data object Dashboard : BreederScreen("breeder/dashboard")
    data object LitterCampaignEditor : BreederScreen("breeder/litters/editor")
    data object LitterMediaManager : BreederScreen("breeder/litters/media")
    data object LitterUpdateComposer : BreederScreen("breeder/litters/updates")
    data object PuppyManager : BreederScreen("breeder/puppies")
    data object ApplicantInbox : BreederScreen("breeder/applicants")
    data object WaitlistRankingBoard : BreederScreen("breeder/waitlist")
    data object ReservationDashboard : BreederScreen("breeder/reservations")
    data object Messages : BreederScreen("breeder/messages")
    data object PublicStorefront : BreederScreen("breeder/storefront")
    data object Analytics : BreederScreen("breeder/analytics")
}

sealed class AdminScreen(val route: String) {
    data object VerificationReviewQueue : AdminScreen("admin/verification-queue")
    data object ListingApprovalQueue : AdminScreen("admin/listing-approval")
    data object FlaggedReportQueue : AdminScreen("admin/reports")
    data object UserAccountProfile : AdminScreen("admin/accounts")
    data object DepositDisputeConsole : AdminScreen("admin/deposits")
    data object AuditLogs : AdminScreen("admin/audit")
    data object SafetyContentCms : AdminScreen("admin/cms")
}

sealed class TransportScreen(val route: String) {
    data object ServiceProfile : TransportScreen("transport/profile")
    data object RouteManager : TransportScreen("transport/routes")
    data object QuoteInbox : TransportScreen("transport/quotes")
}
