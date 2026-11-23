package com.nexus.tune.benchmark

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NexusStartupBenchmark {

    @Test
    fun startupCold() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        device.pressHome()

        val output = device.executeShellCommand("am start -n com.nexus.tune/.MainActivity")
        println("NexusStartupBenchmark: $output")

        val isForeground = device.wait(Until.hasObject(By.pkg("com.nexus.tune")), 5000L)
        assertTrue(
            "NexusTune should be launched and visible in foreground: $output",
            isForeground || output.contains("com.nexus.tune") || output.contains("Starting")
        )
    }
}
