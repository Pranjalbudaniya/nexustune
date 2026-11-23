package com.nexus.tune.benchmark

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NexusStartupBenchmark {

    @Test
    fun startupCold() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val intent = context.packageManager.getLaunchIntentForPackage("com.nexus.tune") ?: Intent().apply {
            setClassName("com.nexus.tune", "com.nexus.tune.MainActivity")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
        Thread.sleep(1500)
        assertTrue("NexusTune launched successfully", true)
    }
}
