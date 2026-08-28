package com.fureva.veripup.design

/**
 * Central source of truth for all brand identity constants used across the
 * Fureva VeriPup application.
 *
 * Includes the app name, hero copy for the marketing/landing surfaces, and the
 * complete design-system colour palette. UI components should reference these
 * constants rather than hard-coding strings or hex values.
 */
object Brand {
    /** The full display name of the application. */
    const val appName = "Fureva VeriPup"

    /** Primary hero headline shown on the home/landing screen. */
    const val heroPrimary = "Bring home with confidence."

    /** Secondary hero sub-headline describing the Verified Text Alerts feature. */
    const val heroSecondary = "Follow VeriPup Verified breeders and get Verified Text Alerts—texts only for verified updates."

    /** Tertiary hero message reinforcing the opt-in, no-spam nature of alerts. */
    const val heroTertiary = "Opt-in. No spam. Turn off anytime."

    /**
     * The complete design-system colour palette, keyed by semantic colour name.
     *
     * | Name                | Hex       | Usage                                      |
     * |---------------------|-----------|--------------------------------------------|
     * | VeriPup Navy        | `#0E1B2A` | Primary brand background / nav bar         |
     * | Forever Amber       | `#F2A65A` | Primary CTA accent                         |
     * | Verification Green  | `#16A34A` | Verified badge and success states          |
     * | Cloud               | `#F7F7FB` | Page background                            |
     * | White               | `#FFFFFF` | Card and surface background                |
     * | Ink                 | `#0B0F14` | Body text                                  |
     * | Slate               | `#5B6472` | Secondary / muted text                     |
     * | Mist                | `#E6E8EF` | Dividers and borders                       |
     * | Signal Red          | `#DC2626` | Error and destructive action states        |
     * | Caution Gold        | `#EAB308` | Warning states                             |
     */
    val colors = mapOf(
        "VeriPup Navy" to "#0E1B2A",
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
