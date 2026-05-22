package com.example.gymtrackapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID
@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("templateId"),
        Index("exerciseId")
    ]
)
data class TemplateExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val exerciseId: String,
    val order: Int,
    // Firestore sync
    val remoteId: String = UUID.randomUUID().toString(),
    val updatedAtMs: Long = System.currentTimeMillis(),
    /** 0=SYNCED, 1=PENDING_UPSERT, 2=PENDING_DELETE */
    val syncStatus: Int = SyncStatus.PENDING_UPSERT,
    val deletedAtMs: Long? = null,
)
