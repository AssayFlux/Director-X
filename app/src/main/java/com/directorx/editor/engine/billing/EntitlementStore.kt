package com.directorx.editor.engine.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory, fail-closed entitlement projection.
 *
 * The future Play Billing repository must call [publishVerifiedSnapshot] only
 * after server/Play verification and configured product-to-tier mapping. A
 * process restart intentionally returns to Free until the purchase is checked
 * again; this store never persists an unverified premium grant.
 */
@Singleton
class EntitlementStore @Inject constructor() {
    private val mutableSnapshot = MutableStateFlow(EntitlementSnapshot())
    val snapshot: StateFlow<EntitlementSnapshot> = mutableSnapshot.asStateFlow()

    internal fun publishVerifiedSnapshot(snapshot: EntitlementSnapshot) {
        mutableSnapshot.value = if (snapshot.subscription.verified) {
            snapshot
        } else {
            EntitlementSnapshot()
        }
    }

    internal fun clear() {
        mutableSnapshot.value = EntitlementSnapshot()
    }
}
