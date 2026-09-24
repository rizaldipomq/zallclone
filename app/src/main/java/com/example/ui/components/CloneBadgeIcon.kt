package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image

@Composable
fun CloneBadgeIcon(
    iconBitmap: ImageBitmap?,
    badgeText: String,
    badgeColorHex: Long,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val badgeColor = Color(badgeColorHex)
    val badgeSize = (size.value * 0.42f).dp

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // App Icon Base
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape((size.value * 0.28f).dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape((size.value * 0.28f).dp)
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape((size.value * 0.28f).dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size((size.value * 0.55f).dp)
                )
            }
        }

        // Overlay Badge on bottom right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(badgeSize)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(badgeColor)
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (badgeText.length > 3) badgeText.take(2) + "…" else badgeText,
                color = Color.White,
                fontSize = (badgeSize.value * 0.46f).sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
