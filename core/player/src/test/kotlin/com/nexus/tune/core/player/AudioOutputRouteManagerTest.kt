package com.nexus.tune.core.player

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import com.nexus.tune.core.model.AudioDeviceType
import com.nexus.tune.core.player.routing.AudioOutputRouteManager
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AudioOutputRouteManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val audioManager = mockk<AudioManager>(relaxed = true)

    @Before
    fun setup() {
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager
    }

    @Test
    fun `resolveCurrentDevice returns speaker when no external devices connected`() {
        every { audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS) } returns emptyArray()

        val manager = AudioOutputRouteManager(context)
        val device = manager.resolveCurrentDevice()

        assertEquals(AudioDeviceType.BUILTIN_SPEAKER, device.type)
        assertEquals("Phone Speaker", device.name)
    }

    @Test
    fun `resolveCurrentDevice identifies Bluetooth A2DP`() {
        val btDevice = mockk<AudioDeviceInfo> {
            every { type } returns AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
            every { productName } returns "Sony WH-1000XM5"
        }
        every { audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS) } returns arrayOf(btDevice)

        val manager = AudioOutputRouteManager(context)
        val device = manager.resolveCurrentDevice()

        assertEquals(AudioDeviceType.BLUETOOTH_A2DP, device.type)
        assertEquals("Sony WH-1000XM5", device.name)
        assertEquals(false, device.isHighRes)
    }

    @Test
    fun `resolveCurrentDevice prioritizes USB DAC as Hi-Res`() {
        val usbDevice = mockk<AudioDeviceInfo> {
            every { type } returns AudioDeviceInfo.TYPE_USB_DEVICE
            every { productName } returns "FiiO KA3"
        }
        val btDevice = mockk<AudioDeviceInfo> {
            every { type } returns AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
            every { productName } returns "Wireless Buds"
        }
        every { audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS) } returns arrayOf(btDevice, usbDevice)

        val manager = AudioOutputRouteManager(context)
        val device = manager.resolveCurrentDevice()

        assertEquals(AudioDeviceType.USB_DAC, device.type)
        assertEquals("FiiO KA3", device.name)
        assertEquals(true, device.isHighRes)
    }

    @Test
    fun `resolveCurrentDevice detects USB DAC bit depth and sample rates`() {
        val usbDevice = mockk<AudioDeviceInfo> {
            every { type } returns AudioDeviceInfo.TYPE_USB_DEVICE
            every { productName } returns "AudioQuest Dragonfly"
            every { sampleRates } returns intArrayOf(44100, 48000, 96000)
            every { encodings } returns intArrayOf(android.media.AudioFormat.ENCODING_PCM_24BIT_PACKED)
        }
        every { audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS) } returns arrayOf(usbDevice)

        val manager = AudioOutputRouteManager(context)
        val device = manager.resolveCurrentDevice()

        assertEquals(AudioDeviceType.USB_DAC, device.type)
        assertEquals(24, device.bitDepth)
        assertEquals(listOf(44100, 48000, 96000), device.sampleRates)
        assertEquals("24-bit / 96kHz", device.fidelityDescription)
    }

    @Test
    fun `setBitPerfectEnabled updates current device output name`() {
        val usbDevice = mockk<AudioDeviceInfo> {
            every { type } returns AudioDeviceInfo.TYPE_USB_DEVICE
            every { productName } returns "iFi Zen DAC"
            every { sampleRates } returns intArrayOf(192000)
            every { encodings } returns intArrayOf(android.media.AudioFormat.ENCODING_PCM_32BIT)
        }
        every { audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS) } returns arrayOf(usbDevice)

        val manager = AudioOutputRouteManager(context)
        manager.setBitPerfectEnabled(true)
        val device = manager.currentOutputDevice.value

        assertEquals("iFi Zen DAC (Bit-Perfect)", device.name)
        assertEquals(true, device.isBitPerfectSupported)
    }
}
