package com.example.data.repository

import android.content.Context
import com.example.data.local.AppSettingDao
import com.example.data.local.AppSettingEntity
import com.example.data.local.CloneDao
import com.example.data.local.CloneEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class CloneRepository(
    private val context: Context,
    private val cloneDao: CloneDao,
    private val appSettingDao: AppSettingDao
) {
    val allClones: Flow<List<CloneEntity>> = cloneDao.getAllClones().flowOn(Dispatchers.IO)
    val cloneCount: Flow<Int> = cloneDao.getCloneCount().flowOn(Dispatchers.IO)

    fun getCloneById(id: Long): Flow<CloneEntity?> = cloneDao.getCloneById(id).flowOn(Dispatchers.IO)

    suspend fun getCloneByIdDirect(id: Long): CloneEntity? = withContext(Dispatchers.IO) {
        cloneDao.getCloneByIdDirect(id)
    }

    suspend fun getClonesForPackage(packageName: String): List<CloneEntity> = withContext(Dispatchers.IO) {
        cloneDao.getClonesByPackageDirect(packageName)
    }

    suspend fun createClone(
        packageName: String,
        appName: String,
        customLabel: String,
        badgeColorHex: Long,
        badgeText: String,
        cloneMode: String,
        webUrl: String = "",
        pinCode: String = "",
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val count = cloneDao.getCloneCountForPackage(packageName)
        val cloneNumber = count + 1
        val instanceDirName = "clone_${packageName.replace('.', '_')}_$cloneNumber"
        val cloneDir = File(context.filesDir, "clones/$instanceDirName")
        if (!cloneDir.exists()) {
            cloneDir.mkdirs()
            File(cloneDir, "files").mkdirs()
            File(cloneDir, "cache").mkdirs()
            File(cloneDir, "shared_prefs").mkdirs()
            File(cloneDir, "databases").mkdirs()
        }

        val cloneEntity = CloneEntity(
            packageName = packageName,
            appName = appName,
            cloneLabel = customLabel.ifBlank { "$appName #$cloneNumber" },
            cloneNumber = cloneNumber,
            badgeColorHex = badgeColorHex,
            badgeText = badgeText.ifBlank { cloneNumber.toString() },
            dataDirectoryName = instanceDirName,
            storageBytes = calculateDirectorySize(cloneDir),
            createdAt = System.currentTimeMillis(),
            lastLaunchedAt = 0L,
            launchCount = 0,
            isLocked = pinCode.isNotBlank(),
            pinCode = pinCode,
            cloneMode = cloneMode,
            webUrl = webUrl,
            spoofAndroidId = UUID.randomUUID().toString().replace("-", "").take(16),
            notes = notes
        )
        val id = cloneDao.insertClone(cloneEntity)
        id
    }

    suspend fun updateClone(clone: CloneEntity) = withContext(Dispatchers.IO) {
        cloneDao.updateClone(clone)
    }

    suspend fun deleteClone(clone: CloneEntity) = withContext(Dispatchers.IO) {
        // Remove isolated directory
        val cloneDir = File(context.filesDir, "clones/${clone.dataDirectoryName}")
        if (cloneDir.exists()) {
            cloneDir.deleteRecursively()
        }
        cloneDao.deleteClone(clone)
    }

    suspend fun markLaunched(id: Long) = withContext(Dispatchers.IO) {
        cloneDao.markLaunched(id, System.currentTimeMillis())
    }

    suspend fun clearCloneCache(clone: CloneEntity): Long = withContext(Dispatchers.IO) {
        val cacheDir = File(context.filesDir, "clones/${clone.dataDirectoryName}/cache")
        var freed = 0L
        if (cacheDir.exists()) {
            freed = calculateDirectorySize(cacheDir)
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
        }
        val fullDir = File(context.filesDir, "clones/${clone.dataDirectoryName}")
        val remaining = calculateDirectorySize(fullDir)
        cloneDao.updateStorageBytes(clone.id, remaining)
        freed
    }

    suspend fun resetCloneData(clone: CloneEntity) = withContext(Dispatchers.IO) {
        val cloneDir = File(context.filesDir, "clones/${clone.dataDirectoryName}")
        if (cloneDir.exists()) {
            cloneDir.deleteRecursively()
        }
        cloneDir.mkdirs()
        File(cloneDir, "files").mkdirs()
        File(cloneDir, "cache").mkdirs()
        File(cloneDir, "shared_prefs").mkdirs()
        File(cloneDir, "databases").mkdirs()
        cloneDao.updateStorageBytes(clone.id, 0L)
    }

    suspend fun clearAllClonesCache(): Long = withContext(Dispatchers.IO) {
        var totalFreed = 0L
        val clonesParent = File(context.filesDir, "clones")
        if (clonesParent.exists()) {
            clonesParent.listFiles()?.forEach { dir ->
                val cache = File(dir, "cache")
                if (cache.exists()) {
                    totalFreed += calculateDirectorySize(cache)
                    cache.deleteRecursively()
                    cache.mkdirs()
                }
            }
        }
        totalFreed
    }

    suspend fun refreshStorageSizes() = withContext(Dispatchers.IO) {
        val list = cloneDao.getClonesByPackageDirect("") // will be refreshed in list
    }

    // App Settings
    fun getSetting(key: String): Flow<String?> = appSettingDao.getSetting(key).flowOn(Dispatchers.IO)

    suspend fun getSettingDirect(key: String): String? = withContext(Dispatchers.IO) {
        appSettingDao.getSettingDirect(key)
    }

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        appSettingDao.setSetting(AppSettingEntity(key, value))
    }

    private fun calculateDirectorySize(dir: File): Long {
        if (!dir.exists()) return 0L
        var size = 0L
        try {
            dir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        } catch (e: Exception) {
            // Ignore access errors
        }
        return size
    }
}
