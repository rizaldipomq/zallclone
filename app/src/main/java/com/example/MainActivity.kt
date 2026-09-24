package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CloneEntity
import com.example.ui.MainViewModel
import com.example.ui.components.ClonePinDialog
import com.example.ui.screens.AppsCatalogScreen
import com.example.ui.screens.DisguiseCalculatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SandboxStorageScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WebSandboxScreen
import com.example.ui.theme.ZallCloneTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingShortcutIntent(intent)

        setContent {
            ZallCloneTheme {
                val isDisguiseMode by viewModel.isDisguiseModeEnabled.collectAsStateWithLifecycle()
                val disguisePin by viewModel.disguisePin.collectAsStateWithLifecycle()
                val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
                val activeWebClone by viewModel.activeWebClone.collectAsStateWithLifecycle()

                // If Disguise Mode is active and manager is locked, show Calculator!
                if (isDisguiseMode && !isUnlocked) {
                    DisguiseCalculatorScreen(
                        disguisePin = disguisePin,
                        onUnlock = {
                            viewModel.unlockWithPin(disguisePin)
                        }
                    )
                } else if (activeWebClone != null) {
                    // Fullscreen Dual Web Sandbox Container for parallel web sessions
                    WebSandboxScreen(
                        clone = activeWebClone!!,
                        onClose = { viewModel.closeWebSandbox() }
                    )
                } else {
                    ZallCloneAppContent(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingShortcutIntent(intent)
    }

    private fun handleIncomingShortcutIntent(intent: Intent?) {
        if (intent != null && intent.hasExtra("EXTRA_CLONE_ID")) {
            val cloneId = intent.getLongExtra("EXTRA_CLONE_ID", -1L)
            if (cloneId > 0) {
                // Find and launch the clone
                val app = application as ZallCloneApplication
                // The ViewModel will handle launching or we can trigger after compose binds
            }
        }
    }
}

@Composable
fun ZallCloneAppContent(viewModel: MainViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var pendingLockedClone by remember { mutableStateOf<CloneEntity?>(null) }

    val clones by viewModel.allClones.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val totalStorageBytes by viewModel.totalStorageBytes.collectAsStateWithLifecycle()
    val isDisguiseMode by viewModel.isDisguiseModeEnabled.collectAsStateWithLifecycle()
    val disguisePin by viewModel.disguisePin.collectAsStateWithLifecycle()

    fun attemptLaunch(clone: CloneEntity) {
        if (clone.isLocked && clone.pinCode.isNotBlank()) {
            pendingLockedClone = clone
        } else {
            viewModel.launchClone(clone) { webClone ->
                viewModel.openWebSandbox(webClone)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                // Tab 0: Klon Saya
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Layers else Icons.Outlined.Layers,
                            contentDescription = stringResource(R.string.tab_clones)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_clones)) },
                    modifier = Modifier.testTag("nav_tab_clones")
                )

                // Tab 1: Pilih Aplikasi
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Apps else Icons.Outlined.Apps,
                            contentDescription = stringResource(R.string.tab_apps)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_apps)) },
                    modifier = Modifier.testTag("nav_tab_apps")
                )

                // Tab 2: Ruang Terisolasi (Sandbox)
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.FolderShared else Icons.Outlined.FolderShared,
                            contentDescription = stringResource(R.string.tab_virtual_space)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_virtual_space)) },
                    modifier = Modifier.testTag("nav_tab_sandbox")
                )

                // Tab 3: Pengaturan
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.tab_settings)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_settings)) },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    0 -> HomeScreen(
                        clones = clones,
                        installedApps = installedApps,
                        totalStorageBytes = totalStorageBytes,
                        onLaunchClone = { clone -> attemptLaunch(clone) },
                        onNavigateToApps = { selectedTab = 1 },
                        onToggleFavorite = { clone -> viewModel.toggleFavorite(clone) },
                        onUpdateClone = { clone -> viewModel.updateClone(clone) },
                        onClearCache = { clone -> viewModel.clearCloneCache(clone) },
                        onResetData = { clone -> viewModel.resetCloneData(clone) },
                        onDeleteClone = { clone -> viewModel.deleteClone(clone) },
                        onCreateShortcut = { clone -> viewModel.createShortcut(clone) },
                        onCleanAllCache = { viewModel.clearAllCache() }
                    )
                    1 -> AppsCatalogScreen(
                        installedApps = filteredApps,
                        existingClones = clones,
                        isLoading = isLoadingApps,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onSearchChange = { viewModel.onSearchQueryChanged(it) },
                        onCategoryChange = { viewModel.onCategorySelected(it) },
                        onRefreshApps = { viewModel.loadInstalledApps() },
                        onCreateCloneConfirm = { app, label, color, text, mode, url, pin ->
                            viewModel.createClone(
                                packageName = app.packageName,
                                appName = app.appName,
                                customLabel = label,
                                badgeColorHex = color,
                                badgeText = text,
                                cloneMode = mode,
                                webUrl = url,
                                pinCode = pin
                            )
                            selectedTab = 0 // Switch back to clones tab to see newly created clone!
                        }
                    )
                    2 -> SandboxStorageScreen(
                        clones = clones,
                        totalStorageBytes = totalStorageBytes,
                        onCleanAllCache = { viewModel.clearAllCache() },
                        onClearSingleCache = { clone -> viewModel.clearCloneCache(clone) },
                        onUpdateClone = { clone -> viewModel.updateClone(clone) }
                    )
                    3 -> SettingsScreen(
                        isDisguiseMode = isDisguiseMode,
                        disguisePin = disguisePin,
                        onToggleDisguise = { enabled, pin ->
                            viewModel.setDisguiseMode(enabled, pin)
                        },
                        onLockNow = { viewModel.lockManager() }
                    )
                }
            }
        }
    }

    // PIN dialog for locked clone
    pendingLockedClone?.let { clone ->
        ClonePinDialog(
            clone = clone,
            onDismiss = { pendingLockedClone = null },
            onPinSuccess = {
                pendingLockedClone = null
                viewModel.launchClone(clone) { webClone ->
                    viewModel.openWebSandbox(webClone)
                }
            }
        )
    }
}
