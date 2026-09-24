package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@Composable
fun DisguiseCalculatorScreen(
    disguisePin: String,
    onUnlock: () -> Unit
) {
    var display by remember { mutableStateOf("0") }
    var currentOperation by remember { mutableStateOf<String?>(null) }
    var previousValue by remember { mutableStateOf<Double?>(null) }
    var isNewNumber by remember { mutableStateOf(true) }
    var showHintDialog by remember { mutableStateOf(false) }

    fun onDigitPress(digit: String) {
        if (isNewNumber || display == "0") {
            display = digit
            isNewNumber = false
        } else {
            if (display.length < 12) {
                display += digit
            }
        }
    }

    fun onClearPress() {
        display = "0"
        currentOperation = null
        previousValue = null
        isNewNumber = true
    }

    fun onOperatorPress(op: String) {
        previousValue = display.toDoubleOrNull()
        currentOperation = op
        isNewNumber = true
    }

    fun onEqualsPress() {
        // Secret PIN Check!
        if (display == disguisePin) {
            onUnlock()
            return
        }

        val prev = previousValue
        val op = currentOperation
        val current = display.toDoubleOrNull()

        if (prev != null && op != null && current != null) {
            val result = when (op) {
                "+" -> prev + current
                "-" -> prev - current
                "×" -> prev * current
                "÷" -> if (current != 0.0) prev / current else Double.NaN
                else -> current
            }

            display = if (result.isNaN()) {
                "Error"
            } else {
                val format = DecimalFormat("#.########")
                format.format(result)
            }
            previousValue = null
            currentOperation = null
            isNewNumber = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Top Disguise Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Kalkulator",
                        color = Color(0xFF64748B),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { showHintDialog = true },
                    modifier = Modifier.testTag("calculator_hint_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Petunjuk Rahasia",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Calculator Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (previousValue != null && currentOperation != null) {
                    Text(
                        text = "$previousValue $currentOperation",
                        fontSize = 20.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Light
                    )
                }
                Text(
                    text = display,
                    fontSize = if (display.length > 8) 42.sp else 56.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("calculator_display")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad Grid
            val buttonRows = listOf(
                listOf("C" to Color(0xFFEF4444), "÷" to Color(0xFF6366F1), "%" to Color(0xFF334155), "⌫" to Color(0xFF334155)),
                listOf("7" to Color(0xFF1E293B), "8" to Color(0xFF1E293B), "9" to Color(0xFF1E293B), "×" to Color(0xFF6366F1)),
                listOf("4" to Color(0xFF1E293B), "5" to Color(0xFF1E293B), "6" to Color(0xFF1E293B), "-" to Color(0xFF6366F1)),
                listOf("1" to Color(0xFF1E293B), "2" to Color(0xFF1E293B), "3" to Color(0xFF1E293B), "+" to Color(0xFF6366F1)),
                listOf("0" to Color(0xFF1E293B), "." to Color(0xFF1E293B), "🔓" to Color(0xFF06B6D4), "=" to Color(0xFF10B981))
            )

            for (row in buttonRows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for ((label, bg) in row) {
                        CalcButton(
                            label = label,
                            backgroundColor = bg,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (label) {
                                    "C" -> onClearPress()
                                    "⌫" -> {
                                        if (display.length > 1) display = display.dropLast(1)
                                        else display = "0"
                                    }
                                    "+", "-", "×", "÷" -> onOperatorPress(label)
                                    "=" -> onEqualsPress()
                                    "🔓" -> onUnlock() // Direct unlock bypass helper
                                    "." -> {
                                        if (!display.contains(".")) {
                                            display += "."
                                            isNewNumber = false
                                        }
                                    }
                                    "%" -> {
                                        val v = display.toDoubleOrNull()
                                        if (v != null) display = (v / 100).toString()
                                    }
                                    else -> onDigitPress(label)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showHintDialog) {
        AlertDialog(
            onDismissRequest = { showHintDialog = false },
            title = { Text("Mode Penyamaran Aktif") },
            text = {
                Text(
                    "Aplikasi Zall Clone sedang disamarkan sebagai kalkulator.\n\n" +
                            "Ketik PIN rahasia Anda ('$disguisePin') lalu tekan tombol '=' atau tekan tombol gembok '🔓' untuk membuka manager clone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showHintDialog = false
                    onUnlock()
                }) {
                    Text("Buka Zall Clone Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHintDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun CalcButton(
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .testTag("calc_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
