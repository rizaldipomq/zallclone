package com.example.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.local.CloneEntity
import com.example.data.repository.CloneRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object CloneLauncherEngine {

    fun launchCloneInstance(
        context: Context,
        clone: CloneEntity,
        repository: CloneRepository,
        scope: CoroutineScope,
        onOpenWebSandbox: (CloneEntity) -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            repository.markLaunched(clone.id)
        }

        when (clone.cloneMode) {
            "WEB_DUAL" -> {
                onOpenWebSandbox(clone)
            }
            else -> {
                launchNativeIsolated(context, clone, onOpenWebSandbox)
            }
        }
    }

    private fun launchNativeIsolated(
        context: Context,
        clone: CloneEntity,
        onOpenWebSandbox: (CloneEntity) -> Unit
    ) {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(clone.packageName)

        if (launchIntent != null) {
            // Inject isolation parameters
            launchIntent.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                putExtra("CLONE_INSTANCE_ID", clone.id)
                putExtra("CLONE_NUMBER", clone.cloneNumber)
                putExtra("CLONE_LABEL", clone.cloneLabel)
                putExtra("CLONE_DATA_DIR", clone.dataDirectoryName)
                putExtra("CLONE_ANDROID_ID", clone.spoofAndroidId)
            }
            try {
                context.startActivity(launchIntent)
                Toast.makeText(
                    context,
                    "Menjalankan ${clone.cloneLabel} dalam Ruang Terisolasi Zall Clone",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membuka aplikasi: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } else {
            // If native package not directly installable or web fallback available
            if (clone.webUrl.isNotBlank()) {
                onOpenWebSandbox(clone)
            } else {
                Toast.makeText(
                    context,
                    "Aplikasi ${clone.appName} tidak ditemukan pada perangkat ini.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
