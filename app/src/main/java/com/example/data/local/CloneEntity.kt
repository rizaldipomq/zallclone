package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clone_instances")
data class CloneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val cloneLabel: String,
    val cloneNumber: Int,
    val badgeColorHex: Long = 0xFF6366F1,
    val badgeText: String = "1",
    val dataDirectoryName: String,
    val storageBytes: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLaunchedAt: Long = 0L,
    val launchCount: Int = 0,
    val isLocked: Boolean = false,
    val pinCode: String = "",
    val cloneMode: String = "SANDBOX_ISOLATED", // SANDBOX_ISOLATED, WEB_DUAL, NATIVE_LAUNCH
    val webUrl: String = "",
    val spoofAndroidId: String = "",
    val spoofDeviceModel: String = "Default",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val incognitoMode: Boolean = false
)
