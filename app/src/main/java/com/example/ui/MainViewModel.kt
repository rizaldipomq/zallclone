package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ZallCloneApplication
import com.example.data.local.CloneEntity
import com.example.engine.AppCategory
import com.example.engine.CloneLauncherEngine
import com.example.engine.CloneShortcutManager
import com.example.engine.InstalledAppItem
import com.example.engine.InstalledAppScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as ZallCloneApplication).repository

    val allClones: StateFlow<List<CloneEntity>> = repository.allClones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(true)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)
    val selectedCategory: StateFlow<AppCategory> = _selectedCategory.asStateFlow()

    private val _isDisguiseModeEnabled = MutableStateFlow(false)
    val isDisguiseModeEnabled: StateFlow<Boolean> = _isDisguiseModeEnabled.asStateFlow()

    private val _disguisePin = MutableStateFlow("7777")
    val disguisePin: StateFlow<String> = _disguisePin.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _activeWebClone = MutableStateFlow<CloneEntity?>(null)
    val activeWebClone: StateFlow<CloneEntity?> = _activeWebClone.asStateFlow()

    val filteredApps: StateFlow<List<InstalledAppItem>> = combine(
        _installedApps,
        _searchQuery,
        _selectedCategory
    ) { apps, query, cat ->
        apps.filter { item ->
            val matchesCategory = (cat == AppCategory.ALL) || (item.category == cat)
            val matchesQuery = query.isBlank() ||
                    item.appName.contains(query, ignoreCase = true) ||
                    item.packageName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStorageBytes: StateFlow<Long> = allClones.combine(_installedApps) { clones, _ ->
        calculateTotalClonesStorage()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    init {
        loadSettings()
        loadInstalledApps()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val disguise = repository.getSettingDirect("disguise_mode") == "true"
            val pin = repository.getSettingDirect("disguise_pin") ?: "7777"
            _isDisguiseModeEnabled.value = disguise
            _disguisePin.value = pin
            // If disguise mode is not enabled, unlock immediately
            if (!disguise) {
                _isUnlocked.value = true
            }
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val apps = InstalledAppScanner.getInstalledApps(getApplication())
            _installedApps.value = apps
            _isLoadingApps.value = false
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: AppCategory) {
        _selectedCategory.value = category
    }

    fun openWebSandbox(clone: CloneEntity) {
        _activeWebClone.value = clone
    }

    fun closeWebSandbox() {
        _activeWebClone.value = null
    }

    fun launchClone(clone: CloneEntity, onOpenWeb: (CloneEntity) -> Unit) {
        CloneLauncherEngine.launchCloneInstance(
            context = getApplication(),
            clone = clone,
            repository = repository,
            scope = viewModelScope,
            onOpenWebSandbox = {
                _activeWebClone.value = it
                onOpenWeb(it)
            }
        )
    }

    fun createClone(
        packageName: String,
        appName: String,
        customLabel: String,
        badgeColorHex: Long,
        badgeText: String,
        cloneMode: String,
        webUrl: String = "",
        pinCode: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val id = repository.createClone(
                packageName = packageName,
                appName = appName,
                customLabel = customLabel,
                badgeColorHex = badgeColorHex,
                badgeText = badgeText,
                cloneMode = cloneMode,
                webUrl = webUrl,
                pinCode = pinCode,
                notes = notes
            )
            Toast.makeText(getApplication(), "Klon '$customLabel' berhasil dibuat!", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateClone(clone: CloneEntity) {
        viewModelScope.launch {
            repository.updateClone(clone)
            Toast.makeText(getApplication(), "Pengaturan klon diperbarui.", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteClone(clone: CloneEntity) {
        viewModelScope.launch {
            repository.deleteClone(clone)
            Toast.makeText(getApplication(), "Klon '${clone.cloneLabel}' dihapus.", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFavorite(clone: CloneEntity) {
        viewModelScope.launch {
            repository.updateClone(clone.copy(isFavorite = !clone.isFavorite))
        }
    }

    fun clearCloneCache(clone: CloneEntity, onFreed: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val freed = repository.clearCloneCache(clone)
            onFreed(freed)
            Toast.makeText(
                getApplication(),
                "Cache '${clone.cloneLabel}' dibersihkan.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun resetCloneData(clone: CloneEntity) {
        viewModelScope.launch {
            repository.resetCloneData(clone)
            Toast.makeText(
                getApplication(),
                "Data klon '${clone.cloneLabel}' berhasil direset seperti baru.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun clearAllCache(onFreed: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val freed = repository.clearAllClonesCache()
            onFreed(freed)
            Toast.makeText(
                getApplication(),
                "Semua cache klon berhasil dibersihkan!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun createShortcut(clone: CloneEntity) {
        val success = CloneShortcutManager.createHomeScreenShortcut(getApplication(), clone)
        if (success) {
            Toast.makeText(
                getApplication(),
                "Shortcut '${clone.cloneLabel}' ditambahkan ke Layar Utama!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                getApplication(),
                "Peluncur Anda belum mengizinkan pembuatan shortcut otomatis.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun setDisguiseMode(enabled: Boolean, pin: String) {
        viewModelScope.launch {
            repository.setSetting("disguise_mode", enabled.toString())
            repository.setSetting("disguise_pin", pin.ifBlank { "7777" })
            _isDisguiseModeEnabled.value = enabled
            _disguisePin.value = pin.ifBlank { "7777" }
            if (!enabled) {
                _isUnlocked.value = true
            }
        }
    }

    fun unlockWithPin(pin: String): Boolean {
        if (pin == _disguisePin.value) {
            _isUnlocked.value = true
            return true
        }
        return false
    }

    fun lockManager() {
        if (_isDisguiseModeEnabled.value) {
            _isUnlocked.value = false
        }
    }

    private suspend fun calculateTotalClonesStorage(): Long = withContext(Dispatchers.IO) {
        val clonesDir = File(getApplication<Application>().filesDir, "clones")
        if (!clonesDir.exists()) return@withContext 0L
        var size = 0L
        clonesDir.walkTopDown().forEach {
            if (it.isFile) size += it.length()
        }
        size
    }
}
