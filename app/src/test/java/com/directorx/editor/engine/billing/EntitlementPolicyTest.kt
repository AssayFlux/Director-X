package com.directorx.editor.engine.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class EntitlementPolicyTest {
    private val future = Instant.parse("2099-01-01T00:00:00Z")
    private val past = Instant.parse("2020-01-01T00:00:00Z")
    private val now = Instant.parse("2026-11-22T00:00:00Z")

    private fun active(tier: SubscriptionTier, state: SubscriptionState = SubscriptionState.ACTIVE) =
        EntitlementSnapshot(
            SubscriptionSnapshot(
                state = state,
                productId = "configured-product",
                expiresAt = future,
                verified = true,
                autoRenewing = true,
                tier = tier,
            )
        )

    @Test fun everyDispatchedAiToolHasAnExplicitTierPolicy() {
        val expected = setOf(
            "auto_captions", "remove_bg", "style_transfer", "face_track", "smart_reframe", "ai_stabilize",
            "upscale", "frame_interp", "object_remove", "video_upscale", "ai_background", "ai_style_transfer", "bg_replace",
            "scene_detect", "smart_crop", "auto_color", "stabilize", "denoise", "track_motion",
        )
        assertEquals(expected, AiToolEntitlementPolicy.supportedToolIds)
        expected.forEach { id ->
            val required = AiToolEntitlementPolicy.requiredEntitlement(id)
            when (required) {
                null -> assertTrue("$id remains available to Free", active(SubscriptionTier.FREE).allows(EntitlementFeature.CORE_EDITING))
                EntitlementFeature.SMART_AI -> {
                    assertFalse("$id locked on Free", active(SubscriptionTier.FREE).allows(required))
                    assertTrue("$id unlocked on Plus", active(SubscriptionTier.PLUS).allows(required))
                    assertTrue("$id unlocked on Pro", active(SubscriptionTier.PRO).allows(required))
                }
                EntitlementFeature.PRO_AI -> {
                    assertFalse("$id locked on Free", active(SubscriptionTier.FREE).allows(required))
                    assertFalse("$id locked on Plus", active(SubscriptionTier.PLUS).allows(required))
                    assertTrue("$id unlocked on Pro", active(SubscriptionTier.PRO).allows(required))
                }
                else -> throw AssertionError("Unexpected AI entitlement $required for $id")
            }
        }
        assertEquals(null, AiToolEntitlementPolicy.requiredEntitlement("unknown_tool"))
    }

    @Test fun freeFeaturesRemainAvailableWithoutSubscription() {
        val entitlements = EntitlementSnapshot()
        assertEquals(SubscriptionTier.FREE, entitlements.tier)
        assertTrue(entitlements.allows(EntitlementFeature.CORE_EDITING))
        assertTrue(entitlements.allows(EntitlementFeature.PROJECT_MANAGEMENT))
        assertTrue(entitlements.allows(EntitlementFeature.STANDARD_EXPORT))
        assertFalse(entitlements.allows(EntitlementFeature.WATERMARK_FREE_EXPORT))
        assertFalse(entitlements.allows(EntitlementFeature.PREMIUM_EFFECTS))
        assertFalse(entitlements.allows(EntitlementFeature.ADVANCED_KEYFRAMES))
        assertFalse(entitlements.allows(EntitlementFeature.ADVANCED_EXPORT))
    }

    @Test fun verifiedPlusUnlocksMiddleTierButNotPro() {
        val snapshot = active(SubscriptionTier.PLUS)
        assertEquals(SubscriptionTier.PLUS, snapshot.tierAt(now))
        assertTrue(snapshot.allows(EntitlementFeature.WATERMARK_FREE_EXPORT))
        assertTrue(snapshot.allows(EntitlementFeature.FULL_HD_EXPORT))
        assertTrue(snapshot.allows(EntitlementFeature.PREMIUM_EFFECTS))
        assertTrue(snapshot.allows(EntitlementFeature.ADVANCED_KEYFRAMES))
        assertFalse(snapshot.allows(EntitlementFeature.HDR_EXPORT))
        assertFalse(snapshot.allows(EntitlementFeature.BATCH_EXPORT))
        assertFalse(snapshot.allows(EntitlementFeature.PRO_AI))
    }

    @Test fun verifiedProUnlocksAllPaidFeatures() {
        val snapshot = active(SubscriptionTier.PRO)
        assertEquals(SubscriptionTier.PRO, snapshot.tierAt(now))
        EntitlementFeature.entries.filter {
            it != EntitlementFeature.CORE_EDITING &&
                it != EntitlementFeature.PROJECT_MANAGEMENT &&
                it != EntitlementFeature.STANDARD_EXPORT
        }.forEach { assertTrue("$it should be Pro", snapshot.allows(it)) }
    }

    @Test fun unverifiedExpiredPendingAndRevokedPurchasesNeverUnlock() {
        val unverified = EntitlementSnapshot(
            SubscriptionSnapshot(SubscriptionState.ACTIVE, "configured-product", future, false, tier = SubscriptionTier.PRO)
        )
        val expired = EntitlementSnapshot(
            SubscriptionSnapshot(SubscriptionState.EXPIRED, "configured-product", past, true, tier = SubscriptionTier.PRO)
        )
        assertEquals(SubscriptionTier.FREE, unverified.tierAt(now))
        assertEquals(SubscriptionTier.FREE, expired.tierAt(now))
        for (state in listOf(SubscriptionState.PENDING, SubscriptionState.REVOKED, SubscriptionState.UNKNOWN)) {
            assertEquals(SubscriptionTier.FREE, active(SubscriptionTier.PRO, state).tierAt(now))
        }
    }

    @Test fun canceledAndGracePeriodRemainValidOnlyUntilExpiry() {
        for (state in listOf(SubscriptionState.CANCELED, SubscriptionState.IN_GRACE_PERIOD)) {
            assertEquals(SubscriptionTier.PLUS, active(SubscriptionTier.PLUS, state).tierAt(now))
        }
        assertEquals(SubscriptionTier.FREE, active(SubscriptionTier.PRO).tierAt(future))
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankPlayProductIdIsRejected() {
        BillingProductConfig(" ")
    }
}
