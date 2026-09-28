package fr.vinarnt.animu.finder.compose.model

import kotlinx.serialization.Serializable

/**
 * A Cloudflare clearance captured for one host: the `cf_clearance` cookie plus the
 * User-Agent used when the challenge was solved.
 *
 * Cloudflare binds `cf_clearance` to the User-Agent (and client IP) that passed the
 * challenge, so the two must be stored and replayed together. One entry per host;
 * a stored host also covers its subdomains.
 */
@Serializable
data class CloudflareClearance(
    val cookie: String,
    val userAgent: String,
)
