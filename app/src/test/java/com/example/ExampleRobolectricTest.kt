package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CloneEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Zall Clone", appName)
  }

  @Test
  fun `create clone entity instance`() {
    val clone = CloneEntity(
      packageName = "com.whatsapp",
      appName = "WhatsApp",
      cloneLabel = "WhatsApp 2",
      cloneNumber = 2,
      badgeColorHex = 0xFF10B981,
      badgeText = "2",
      dataDirectoryName = "clone_whatsapp_2"
    )
    assertEquals("WhatsApp 2", clone.cloneLabel)
    assertEquals(2, clone.cloneNumber)
    assertEquals("com.whatsapp", clone.packageName)
    assertNotNull(clone.dataDirectoryName)
  }
}
