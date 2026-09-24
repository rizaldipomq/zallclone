package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.InstalledAppItem
import com.example.ui.theme.BadgeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCloneBottomSheet(
    appItem: InstalledAppItem,
    currentCloneCount: Int,
    onDismiss: () -> Unit,
    onCreateConfirm: (
        label: String,
        badgeColorHex: Long,
        badgeText: String,
        mode: String,
        webUrl: String,
        pin: String
    ) -> Unit
) {
    val nextCloneNumber = currentCloneCount + 1
    var cloneLabel by remember { mutableStateOf("${appItem.appName} (Akun $nextCloneNumber)") }
    var badgeText by remember { mutableStateOf(nextCloneNumber.toString()) }
    var selectedColorHex by remember {
        val defaultIdx = (currentCloneCount % BadgeColors.size)
        mutableLongStateOf(BadgeColors[defaultIdx])
    }
    var selectedMode by remember {
        val defaultMode = if (appItem.defaultWebUrl.isNotBlank()) "WEB_DUAL" else "SANDBOX_ISOLATED"
        mutableStateOf(defaultMode)
    }
    var webUrl by remember { mutableStateOf(appItem.defaultWebUrl) }
    var isPinEnabled by remember { mutableStateOf(false) }
    var pinCode by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with live preview
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CloneBadgeIcon(
                    iconBitmap = appItem.iconBitmap,
                    badgeText = badgeText.ifBlank { "1" },
                    badgeColorHex = selectedColorHex,
                    size = 64.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Kloning Baru",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = appItem.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Clone Name Field
            OutlinedTextField(
                value = cloneLabel,
                onValueChange = { cloneLabel = it },
                label = { Text("Nama Klon (Instance)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clone_name_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Badge Text & Color
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = badgeText,
                    onValueChange = { if (it.length <= 4) badgeText = it },
                    label = { Text("Teks Lencana") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Pilih Warna Lencana:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BadgeColors) { colorHex ->
                    val isSelected = colorHex == selectedColorHex
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex))
                            .clickable { selectedColorHex = colorHex }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Clone Mode Selector
            Text(
                text = "Mode Lingkungan Kloning:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Sandbox Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMode = "SANDBOX_ISOLATED" }
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = selectedMode == "SANDBOX_ISOLATED",
                            onClick = { selectedMode = "SANDBOX_ISOLATED" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Virtual Sandbox (Ruang Privat)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Folder data mandiri, spoof ID Android unik tanpa root",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Web Dual Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMode = "WEB_DUAL" }
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = selectedMode == "WEB_DUAL",
                            onClick = { selectedMode = "WEB_DUAL" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Dual Web & PWA Sandbox",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Cookie & sesi terpisah (Cocok untuk WhatsApp, Telegram, Medsos)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Native Fast Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMode = "NATIVE_LAUNCH" }
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = selectedMode == "NATIVE_LAUNCH",
                            onClick = { selectedMode = "NATIVE_LAUNCH" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Peluncur Instan / Shortcut Native",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Luncurkan dengan parameter intent & identitas klon terpisah",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (selectedMode == "WEB_DUAL") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = webUrl,
                    onValueChange = { webUrl = it },
                    label = { Text("URL Web Service / PWA") },
                    placeholder = { Text("https://web.whatsapp.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PIN Lock Option
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Kunci PIN untuk Klon Ini",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { isPinEnabled = it }
                        )
                    }

                    if (isPinEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 6) pinCode = it },
                            label = { Text("Masukkan 4-6 Digit PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = {
                    onCreateConfirm(
                        cloneLabel.ifBlank { "${appItem.appName} #$nextCloneNumber" },
                        selectedColorHex,
                        badgeText.ifBlank { nextCloneNumber.toString() },
                        selectedMode,
                        webUrl,
                        if (isPinEnabled) pinCode else ""
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_create_clone_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buat Klon Sekarang (Gratis)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
