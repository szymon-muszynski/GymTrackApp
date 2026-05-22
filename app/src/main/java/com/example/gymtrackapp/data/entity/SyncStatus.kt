package com.example.gymtrackapp.data.entity

/**
 * Wspólne statusy synchronizacji (offline-first) dla encji trzymanych w Room i wysyłanych do Firestore.
 */
object SyncStatus {
    const val SYNCED: Int = 0
    const val PENDING_UPSERT: Int = 1
    const val PENDING_DELETE: Int = 2
}

