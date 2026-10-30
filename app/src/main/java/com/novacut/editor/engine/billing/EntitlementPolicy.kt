package com.novacut.editor.engine.billing

import java.time.Instant

/**
 * Local projection of a verified Play subscription. Only the billing repository
 * may create VERIFIED snapshots after checking Play purchase state/signature.
 * Unknown, pending, expired, revoked and unverified states never unlock Pro.
 */
enum class SubscriptionState {
    UNKNOWN, PENDING, ACTIVE, IN_GRACE_PERIOD, CANCELED, EXPIRED, REVOKED
}

enum class EntitlementFeature {
    CORE_EDITING,
    PROJECT_MANAGEMENT,
    STANDARD_EXPORT,
    PREMIUM_EFFECTS,
    ADVANCED_EXPORT,
    PRO_AI
}

data class SubscriptionSnapshot(
    val state: SubscriptionState = SubscriptionState.UNKNOWN,
    val productId: String? = null,
    val expiresAt: Instant? = null,
    val verified: Boolean = false,
    val autoRenewing: Boolean = false,
)

data class EntitlementSnapshot(
    val subscription: SubscriptionSnapshot = SubscriptionSnapshot(),
) {
    val isPro: Boolean
        get() {
            val sub = subscription
            if (!sub.verified) return false
            val timeValid = sub.expiresAt?.isAfter(Instant.now()) == true
            return timeValid && sub.state in setOf(
                SubscriptionState.ACTIVE,
                SubscriptionState.IN_GRACE_PERIOD,
                SubscriptionState.CANCELED,
            )
        }

    fun allows(feature: EntitlementFeature): Boolean = when (feature) {
        EntitlementFeature.CORE_EDITING,
        EntitlementFeature.PROJECT_MANAGEMENT,
        EntitlementFeature.STANDARD_EXPORT -> true
        EntitlementFeature.PREMIUM_EFFECTS,
        EntitlementFeature.ADVANCED_EXPORT,
        EntitlementFeature.PRO_AI -> isPro
    }
}

/**
 * Product identifiers are supplied by the Play Console release configuration;
 * no guessed IDs are embedded in the application.
 */
data class BillingProductConfig(
    val subscriptionProductId: String,
    val basePlanId: String? = null,
) {
    init {
        require(subscriptionProductId.isNotBlank()) {
            "A Play Console subscription product ID must be configured."
        }
    }
}
