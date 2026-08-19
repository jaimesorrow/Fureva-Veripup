package com.fureva.veripup.design

object Brand {
    const val appName = "Fureva Veripup"
    const val heroPrimary = "Alaska's trusted home for verified litters."
    const val heroSecondary = "Shareable Litter Pages with transparent breeder, parent, and health records."
    const val heroTertiary = "Deposits, messaging, and pickup logistics built for Alaska breeders and buyers."

    /** Use only these badge labels in UI copy; never imply a claim the platform hasn't verified. */
    val verificationBadgeLabels = mapOf(
        "UNVERIFIED" to "Unverified",
        "IDENTITY_VERIFIED" to "Identity Verified",
        "DOCUMENTS_VERIFIED" to "Documents Verified",
        "TRUSTED_BREEDER" to "Fureva Veripup Trusted Breeder",
        "REJECTED" to "Verification Rejected",
        "SUSPENDED" to "Account Suspended"
    )

    val colors = mapOf(
        "Veripup Navy" to "#0E1B2A",
        "Aurora Teal" to "#1C7C7C",
        "Forever Amber" to "#F2A65A",
        "Verification Green" to "#16A34A",
        "Cloud" to "#F7F7FB",
        "White" to "#FFFFFF",
        "Ink" to "#0B0F14",
        "Slate" to "#5B6472",
        "Mist" to "#E6E8EF",
        "Signal Red" to "#DC2626",
        "Caution Gold" to "#EAB308"
    )
}
