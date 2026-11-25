package com.directorx.editor.engine.billing

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Stable Hilt entry point for app-owned entitlement diagnostics and QA setup.
 * Kept in the main source set so every Android test source set aggregates it
 * into the generated singleton component.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface EntitlementStoreEntryPoint {
    fun entitlementStore(): EntitlementStore
}
