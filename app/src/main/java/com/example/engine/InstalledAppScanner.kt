package com.example.engine

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val category: AppCategory,
    val iconBitmap: ImageBitmap?,
    val defaultWebUrl: String = ""
)

enum class AppCategory(val title: String) {
    ALL("Semua"),
    SOCIAL("Sosial & Chat"),
    GAME("Game"),
    FINANCE("Finansial & Belanja"),
    TOOLS("Produktivitas & Alat")
}

object InstalledAppScanner {

    suspend fun getInstalledApps(context: Context): List<InstalledAppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = try {
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList<PackageInfo>()
        }

        val result = mutableListOf<InstalledAppItem>()

        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue
            // Exclude self
            if (pkg.packageName == context.packageName) continue

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            // If it has launch intent or is interesting
            val launchIntent = pm.getLaunchIntentForPackage(pkg.packageName)
            if (launchIntent != null || !isSystem) {
                val name = try {
                    appInfo.loadLabel(pm).toString()
                } catch (e: Exception) {
                    pkg.packageName
                }

                val category = determineCategory(pkg.packageName, name)
                val icon = try {
                    drawableToImageBitmap(appInfo.loadIcon(pm))
                } catch (e: Exception) {
                    null
                }

                val webUrl = getSuggestedWebUrl(pkg.packageName)

                result.add(
                    InstalledAppItem(
                        appName = name,
                        packageName = pkg.packageName,
                        versionName = pkg.versionName ?: "1.0",
                        isSystemApp = isSystem,
                        category = category,
                        iconBitmap = icon,
                        defaultWebUrl = webUrl
                    )
                )
            }
        }

        // If in emulator or minimal device, add well-known popular clonable templates so user can test right away!
        if (result.isEmpty() || result.size < 5) {
            addPopularTemplateApps(result)
        }

        result.sortedWith(
            compareBy<InstalledAppItem> { it.isSystemApp }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun determineCategory(pkg: String, name: String): AppCategory {
        val lower = pkg.lowercase() + " " + name.lowercase()
        return when {
            lower.contains("whatsapp") || lower.contains("telegram") || lower.contains("instagram") ||
                    lower.contains("facebook") || lower.contains("tiktok") || lower.contains("twitter") ||
                    lower.contains("discord") || lower.contains("line") || lower.contains("wechat") ||
                    lower.contains("snapchat") || lower.contains("messenger") || lower.contains("social") ->
                AppCategory.SOCIAL

            lower.contains("game") || lower.contains("mobile") || lower.contains("clash") ||
                    lower.contains("pubg") || lower.contains("freefire") || lower.contains("roblox") ||
                    lower.contains("genshin") || lower.contains("legend") || lower.contains("play") ->
                AppCategory.GAME

            lower.contains("bank") || lower.contains("pay") || lower.contains("shop") ||
                    lower.contains("tokopedia") || lower.contains("shopee") || lower.contains("dana") ||
                    lower.contains("ovo") || lower.contains("gopay") || lower.contains("bca") ||
                    lower.contains("mandiri") || lower.contains("crypto") || lower.contains("wallet") ->
                AppCategory.FINANCE

            else -> AppCategory.TOOLS
        }
    }

    private fun getSuggestedWebUrl(pkg: String): String {
        val lower = pkg.lowercase()
        return when {
            lower.contains("whatsapp") -> "https://web.whatsapp.com"
            lower.contains("telegram") -> "https://web.telegram.org/k/"
            lower.contains("instagram") -> "https://www.instagram.com"
            lower.contains("twitter") || lower.contains("x.corp") -> "https://x.com"
            lower.contains("facebook") -> "https://m.facebook.com"
            lower.contains("discord") -> "https://discord.com/app"
            lower.contains("tiktok") -> "https://www.tiktok.com"
            lower.contains("reddit") -> "https://www.reddit.com"
            else -> ""
        }
    }

    private fun addPopularTemplateApps(list: MutableList<InstalledAppItem>) {
        val templates = listOf(
            InstalledAppItem("WhatsApp", "com.whatsapp", "2.24.18", false, AppCategory.SOCIAL, null, "https://web.whatsapp.com"),
            InstalledAppItem("Telegram", "org.telegram.messenger", "10.14.0", false, AppCategory.SOCIAL, null, "https://web.telegram.org/k/"),
            InstalledAppItem("Instagram", "com.instagram.android", "345.0.0", false, AppCategory.SOCIAL, null, "https://www.instagram.com"),
            InstalledAppItem("TikTok", "com.zhiliaoapp.musically", "36.2.1", false, AppCategory.SOCIAL, null, "https://www.tiktok.com"),
            InstalledAppItem("Discord", "com.discord", "240.0", false, AppCategory.SOCIAL, null, "https://discord.com/app"),
            InstalledAppItem("Mobile Legends", "com.mobile.legends", "1.8.92", false, AppCategory.GAME, null, ""),
            InstalledAppItem("Shopee", "com.shopee.id", "3.28.1", false, AppCategory.FINANCE, null, "https://shopee.co.id"),
            InstalledAppItem("Facebook", "com.facebook.katana", "475.0.0", false, AppCategory.SOCIAL, null, "https://m.facebook.com")
        )
        for (tpl in templates) {
            if (list.none { it.packageName == tpl.packageName }) {
                list.add(tpl)
            }
        }
    }

    fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap.asImageBitmap()
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
