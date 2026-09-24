package com.example.engine

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.data.local.CloneEntity

object CloneShortcutManager {

    fun createHomeScreenShortcut(context: Context, clone: CloneEntity): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.aistudio.zallclone.LAUNCH_CLONE"
            putExtra("EXTRA_CLONE_ID", clone.id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val baseBitmap = getAppIconBitmap(context, clone.packageName)
        val badgedBitmap = createBadgedIcon(baseBitmap, clone.badgeText, clone.badgeColorHex.toInt())

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_clone_${clone.id}")
            .setShortLabel(clone.cloneLabel)
            .setLongLabel(clone.cloneLabel)
            .setIcon(IconCompat.createWithBitmap(badgedBitmap))
            .setIntent(launchIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    private fun getAppIconBitmap(context: Context, packageName: String): Bitmap {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            InstalledAppScanner.drawableToBitmap(appInfo.loadIcon(pm))
        } catch (e: Exception) {
            // Fallback default icon bitmap
            val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#4F46E5")
            }
            canvas.drawCircle(64f, 64f, 56f, paint)
            bitmap
        }
    }

    fun createBadgedIcon(base: Bitmap, badgeText: String, badgeColor: Int): Bitmap {
        val size = 144
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Draw base icon scaled
        val scaled = Bitmap.createScaledBitmap(base, size, size, true)
        canvas.drawBitmap(scaled, 0f, 0f, null)

        // Draw badge in bottom right corner
        val badgeRadius = 26f
        val cx = size - badgeRadius - 4f
        val cy = size - badgeRadius - 4f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, badgeRadius + 3f, bgPaint)

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = badgeColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, badgeRadius, badgePaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f)
        val textToDraw = if (badgeText.length > 4) badgeText.take(3) + "…" else badgeText
        canvas.drawText(textToDraw, cx, textY, textPaint)

        return result
    }
}
