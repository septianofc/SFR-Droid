package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SfrConfig
import com.example.data.repository.ConfigRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SFR Droid", appName)
  }

  @Test
  fun testConfigRepositoryPersistence() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = ConfigRepository.getInstance(context)
    val testConfig = SfrConfig(
      endpoint = "https://example.com/api/webhook",
      token = "test-token-123",
      intervalMinutes = 30,
      isStandby = true,
      selectedApps = setOf("com.whatsapp", "org.telegram.messenger"),
      selectedFields = setOf("app", "title", "message"),
      wizardCompleted = true
    )
    repo.saveConfig(testConfig)

    val loaded = repo.getConfig()
    assertEquals("https://example.com/api/webhook", loaded.endpoint)
    assertEquals("test-token-123", loaded.token)
    assertEquals(30, loaded.intervalMinutes)
    assertTrue(loaded.isStandby)
    assertTrue(loaded.selectedApps.contains("com.whatsapp"))
    assertTrue(loaded.selectedFields.contains("title"))
    assertTrue(loaded.wizardCompleted)
  }
}
