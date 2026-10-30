package com.novacut.editor.engine.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class EntitlementPolicyTest {
    private val future = Instant.parse("2099-01-01T00:00:00Z")
    private val past = Instant.parse("2020-01-01T00:00:00Z")

    @Test fun freeFeaturesRemainAvailableWithoutSubscription() {
        val entitlements = EntitlementSnapshot()
        assertTrue(entitlements.allows(EntitlementFeature.CORE_EDITING))
        assertTrue(entitlements.allows(EntitlementFeature.PROJECT_MANAGEMENT))
        assertTrue(entitlements.allows(EntitlementFeature.STANDARD_EXPORT))
        assertFalse(entitlements.allows(EntitlementFeature.PREMIUM_EFFECTS))
        assertFalse(entitlements.allows(EntitlementFeature.ADVANCED_EXPORT))
        assertFalse(entitlements.allows(EntitlementFeature.PRO_AI))
    }

    @Test fun verifiedActiveSubscriptionUnlocksPro() {
        val snapshot = EntitlementSnapshot(
            SubscriptionSnapshot(
                state = SubscriptionState.ACTIVE,
                productId = "configured-product",
                expiresAt = future,
                verified = true,
                autoRenewing = true,
            )
        )
        assertTrue(snapshot.isPro)
        assertTrue(snapshot.allows(EntitlementFeature.PRO_AI))
    }

    @Test fun unverifiedOrExpiredPurchaseNeverUnlocksPro() {
        val unverified = EntitlementSnapshot(
            SubscriptionSnapshot(SubscriptionState.ACTIVE, "configured-product", future, false)
        )
        val expired = EntitlementSnapshot(
            SubscriptionSnapshot(SubscriptionState.EXPIRED, "configured-product", past, true)
        )
        assertFalse(unverified.isPro)
        assertFalse(expired.isPro)
    }

    @Test fun pendingAndRevokedPurchasesRemainLocked() {
        for (state in listOf(SubscriptionState.PENDING, SubscriptionState.REVOKED, SubscriptionState.UNKNOWN)) {
            val snapshot = EntitlementSnapshot(
                SubscriptionSnapshot(state, "configured-product", future, true)
            )
            assertFalse("$state must not unlock Pro", snapshot.isPro)
        }
    }

    @Test fun canceledAndGracePeriodRemainValidOnlyUntilExpiry() {
        for (state in listOf(SubscriptionState.CANCELED, SubscriptionState.IN_GRACE_PERIOD)) {
            val active = EntitlementSnapshot(
                SubscriptionSnapshot(state, "configured-product", future, true)
            )
            assertTrue("$state should remain active before expiry", active.isPro)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankPlayProductIdIsRejected() {
        BillingProductConfig(" ")
    }
}
