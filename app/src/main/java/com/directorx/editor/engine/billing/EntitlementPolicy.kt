package com.directorx.editor.engine.billing

import java.time.Instant

enum class SubscriptionTier { FREE, PLUS, PRO }

enum class SubscriptionState {
    UNKNOWN, PENDING, ACTIVE, IN_GRACE_PERIOD, CANCELED, EXPIRED, REVOKED
}

enum class EntitlementFeature {
    CORE_EDITING,
    PROJECT_MANAGEMENT,
    STANDARD_EXPORT,
    WATERMARK_FREE_EXPORT,
    FULL_HD_EXPORT,
    PREMIUM_EFFECTS,
    ADVANCED_KEYFRAMES,
    SMART_AI,
    BACKGROUND_REMOVAL,
    ADVANCED_EXPORT,
    HDR_EXPORT,
    BATCH_EXPORT,
    PREMIUM_AI_MODELS,
    PROFESSIONAL_AUDIO,
    PRO_AI
}

data class SubscriptionSnapshot(
    val state: SubscriptionState = SubscriptionState.UNKNOWN,
    val productId: String? = null,
    val expiresAt: Instant? = null,
    val verified: Boolean = false,
    val autoRenewing: Boolean = false,
    /** Tier is assigned only after the purchase verifier maps a configured product ID. */
    val tier: SubscriptionTier = SubscriptionTier.FREE,
)

data class EntitlementSnapshot(
    val subscription: SubscriptionSnapshot = SubscriptionSnapshot(),
) {
    fun tierAt(now: Instant = Instant.now()): SubscriptionTier {
        val sub = subscription
        if (!sub.verified || sub.expiresAt?.isAfter(now) != true) return SubscriptionTier.FREE
        if (sub.state !in setOf(
                SubscriptionState.ACTIVE,
                SubscriptionState.IN_GRACE_PERIOD,
                SubscriptionState.CANCELED,
            )
        ) return SubscriptionTier.FREE
        return sub.tier
    }

    val tier: SubscriptionTier get() = tierAt()
    val isPro: Boolean get() = tier == SubscriptionTier.PRO
    val isPlus: Boolean get() = tier >= SubscriptionTier.PLUS

    fun allows(feature: EntitlementFeature): Boolean = allows(feature, tier)

    fun allows(feature: EntitlementFeature, tier: SubscriptionTier): Boolean = when (feature) {
        EntitlementFeature.CORE_EDITING,
        EntitlementFeature.PROJECT_MANAGEMENT,
        EntitlementFeature.STANDARD_EXPORT -> true

        EntitlementFeature.WATERMARK_FREE_EXPORT,
        EntitlementFeature.FULL_HD_EXPORT,
        EntitlementFeature.PREMIUM_EFFECTS,
        EntitlementFeature.ADVANCED_KEYFRAMES,
        EntitlementFeature.SMART_AI,
        EntitlementFeature.BACKGROUND_REMOVAL -> tier >= SubscriptionTier.PLUS

        EntitlementFeature.ADVANCED_EXPORT,
        EntitlementFeature.HDR_EXPORT,
        EntitlementFeature.BATCH_EXPORT,
        EntitlementFeature.PREMIUM_AI_MODELS,
        EntitlementFeature.PROFESSIONAL_AUDIO,
        EntitlementFeature.PRO_AI -> tier >= SubscriptionTier.PRO
    }
}

/**
 * Product identifiers are supplied by the Play Console release configuration.
 * No guessed product IDs or purchase credentials are embedded in the application.
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

/** Subscription mapping for every AI tool dispatched by AiToolsDelegate.runAiTool. */
object AiToolEntitlementPolicy {
    private val plusTools = setOf(
        "auto_captions", "remove_bg", "style_transfer", "face_track", "smart_reframe", "ai_stabilize",
    )
    private val proTools = setOf(
        "upscale", "frame_interp", "object_remove", "video_upscale", "ai_background", "ai_style_transfer", "bg_replace",
    )
    private val freeTools = setOf(
        "scene_detect", "smart_crop", "auto_color", "stabilize", "denoise", "track_motion",
    )

    val supportedToolIds: Set<String> = plusTools + proTools + freeTools

    fun requiredEntitlement(toolId: String): EntitlementFeature? = when (toolId) {
        in plusTools -> EntitlementFeature.SMART_AI
        in proTools -> EntitlementFeature.PRO_AI
        in freeTools -> null
        else -> null
    }
}
