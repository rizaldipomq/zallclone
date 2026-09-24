package com.example

import android.app.Application
import com.example.data.local.ZallCloneDatabase
import com.example.data.repository.CloneRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ZallCloneApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { ZallCloneDatabase.getDatabase(this) }
    val repository by lazy {
        CloneRepository(
            context = this,
            cloneDao = database.cloneDao(),
            appSettingDao = database.appSettingDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize default presets if first launch
        applicationScope.launch {
            checkAndSeedDefaultClones()
        }
    }

    private suspend fun checkAndSeedDefaultClones() {
        val seeded = repository.getSettingDirect("has_seeded_presets")
        if (seeded == null) {
            // Seed a popular social multi-instance clone ready to use
            repository.createClone(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                customLabel = "WhatsApp (Akun 2)",
                badgeColorHex = 0xFF10B981, // Emerald Green
                badgeText = "2",
                cloneMode = "WEB_DUAL",
                webUrl = "https://web.whatsapp.com",
                notes = "Instance WhatsApp mandiri dengan session terpisah."
            )
            repository.createClone(
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                customLabel = "Telegram (Akun Kerja)",
                badgeColorHex = 0xFF06B6D4, // Cyan
                badgeText = "WORK",
                cloneMode = "WEB_DUAL",
                webUrl = "https://web.telegram.org/k/",
                notes = "Instance Telegram terpisah untuk kebutuhan kerja."
            )
            repository.setSetting("has_seeded_presets", "true")
            repository.setSetting("disguise_mode", "false")
            repository.setSetting("disguise_pin", "7777")
        }
    }
}
