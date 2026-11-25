package com.example

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.example.compatibility.HiOsCompatibilityHelper
import com.example.data.ClockFontOption
import com.example.data.ClockPreferencesRepository
import com.example.data.ClockStyleType
import com.example.data.LottieClockCatalog
import com.example.systemui.TecnoClockProviderPlugin
import com.example.ui.clock.external.ClockStylePresets
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Tecno Clock Studio`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Tecno Clock Studio", appName)
    }

    @Test
    fun `clock gallery has at least 12 styles including external analog and lottie`() {
        assertTrue(ClockStyleType.entries.size >= 16)
        assertEquals(8, ClockStylePresets.allExternalPresets.size)
        assertEquals(3, LottieClockCatalog.all.size)
        assertNotNull(ClockStyleType.EXT_TECNO_NEON.resolveExternalStyle())
        assertTrue(ClockStyleType.LOTTIE_ORBITAL.isAnimated)
    }

    @Test
    fun `clock provider plugin exposes all gallery styles`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val plugin = TecnoClockProviderPlugin()
        val clocks = plugin.getClocks()
        assertEquals(ClockStyleType.entries.size, clocks.size)

        val controller = plugin.createClock(context, clocks.first().clockId)
        controller.initialize(context, null)
        assertNotNull(controller.largeClock.view)
        controller.destroy()
    }

    @Test
    fun `compatibility helper detects sdk level and capabilities`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val compat = HiOsCompatibilityHelper.inspectDevice(context, magazineGuideConfirmed = false)
        assertEquals(Build.VERSION.SDK_INT, compat.sdkInt)
        assertTrue(compat.isAndroid13Plus)
        assertTrue(compat.isAodWeatherSupported)
    }

    @Test
    fun `datastore repository persists style font and custom wallpaper url`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ClockPreferencesRepository(context)
        repo.selectStyle(ClockStyleType.LOTTIE_ORBITAL, applyDefaultFont = true)
        repo.setFont(ClockFontOption.ORBITRON)
        repo.setDisableLottieAnimation(true)
        repo.setCustomWallpaperUrl("https://picsum.photos/1080/2400")
        val config = repo.clockConfigFlow.first()
        assertEquals(ClockStyleType.LOTTIE_ORBITAL, config.selectedStyle)
        assertEquals(ClockFontOption.ORBITRON, config.fontOption)
        assertTrue(config.disableLottieAnimation)
        assertEquals("https://picsum.photos/1080/2400", config.customWallpaperUrl)
    }
}
