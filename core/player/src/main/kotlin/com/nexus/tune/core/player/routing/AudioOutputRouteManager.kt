package com.nexus.tune.core.player.routing

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import com.nexus.tune.core.model.AudioDeviceType
import com.nexus.tune.core.model.AudioOutputDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioOutputRouteManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val _currentOutputDevice = MutableStateFlow(resolveCurrentDevice())
    val currentOutputDevice: StateFlow<AudioOutputDevice> = _currentOutputDevice.asStateFlow()

    private var isBitPerfectEnabled: Boolean = false
    private var forcedSampleRate: Int = 0

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            _currentOutputDevice.value = resolveCurrentDevice()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            _currentOutputDevice.value = resolveCurrentDevice()
        }
    }

    init {
        try {
            audioManager?.registerAudioDeviceCallback(deviceCallback, Handler(Looper.getMainLooper()))
        } catch (e: Exception) {
            // Graceful fallback if audio callback unsupported in test/mock
        }
    }

    fun setBitPerfectEnabled(enabled: Boolean) {
        isBitPerfectEnabled = enabled
        _currentOutputDevice.value = resolveCurrentDevice()
    }

    fun setForcedSampleRate(rate: Int) {
        forcedSampleRate = rate
        _currentOutputDevice.value = resolveCurrentDevice()
    }

    fun resolveCurrentDevice(): AudioOutputDevice {
        val am = audioManager ?: return AudioOutputDevice()
        try {
            val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            // Priority order: USB DAC -> Bluetooth A2DP -> Wired Headset -> Speaker
            devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_USB_DEVICE || it.type == AudioDeviceInfo.TYPE_USB_HEADSET }?.let {
                val rawName = it.productName.toString().ifBlank { "USB Audio DAC" }
                val rates = try { it.sampleRates.toList().filter { r -> r > 0 } } catch (_: Exception) { emptyList() }
                val encodings = try { it.encodings.toList() } catch (_: Exception) { emptyList() }
                val has24Or32 = encodings.any { enc ->
                    enc == AudioFormat.ENCODING_PCM_24BIT_PACKED ||
                    enc == AudioFormat.ENCODING_PCM_32BIT ||
                    enc == AudioFormat.ENCODING_PCM_FLOAT
                }
                val bitDepth = if (has24Or32) 24 else 16
                val name = if (isBitPerfectEnabled) "$rawName (Bit-Perfect)" else rawName
                return AudioOutputDevice(
                    type = AudioDeviceType.USB_DAC,
                    name = name,
                    isHighRes = true,
                    sampleRates = rates,
                    bitDepth = bitDepth,
                    isBitPerfectSupported = true
                )
            }

            devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }?.let {
                val name = it.productName.toString().ifBlank { "Bluetooth Audio" }
                val rates = try { it.sampleRates.toList().filter { r -> r > 0 } } catch (_: Exception) { emptyList() }
                return AudioOutputDevice(
                    type = AudioDeviceType.BLUETOOTH_A2DP,
                    name = name,
                    isHighRes = false,
                    sampleRates = rates
                )
            }

            devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES }?.let {
                val name = it.productName.toString().ifBlank { "Wired Headphones" }
                val rates = try { it.sampleRates.toList().filter { r -> r > 0 } } catch (_: Exception) { emptyList() }
                return AudioOutputDevice(
                    type = AudioDeviceType.WIRED_HEADSET,
                    name = name,
                    isHighRes = true,
                    sampleRates = rates,
                    bitDepth = 24
                )
            }
        } catch (e: Exception) {
            // Ignore in headless/mock environments
        }

        return AudioOutputDevice(type = AudioDeviceType.BUILTIN_SPEAKER, name = "Phone Speaker", isHighRes = false)
    }

    fun release() {
        try {
            audioManager?.unregisterAudioDeviceCallback(deviceCallback)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
