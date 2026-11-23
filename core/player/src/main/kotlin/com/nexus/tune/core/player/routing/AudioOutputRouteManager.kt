package com.nexus.tune.core.player.routing

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
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

    fun resolveCurrentDevice(): AudioOutputDevice {
        val am = audioManager ?: return AudioOutputDevice()
        try {
            val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            // Priority order: USB DAC -> Bluetooth A2DP -> Wired Headset -> Speaker
            devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_USB_DEVICE || it.type == AudioDeviceInfo.TYPE_USB_HEADSET }?.let {
                val name = it.productName.toString().ifBlank { "USB DAC (Hi-Res)" }
                return AudioOutputDevice(type = AudioDeviceType.USB_DAC, name = name, isHighRes = true)
            }

            devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }?.let {
                val name = it.productName.toString().ifBlank { "Bluetooth Audio" }
                return AudioOutputDevice(type = AudioDeviceType.BLUETOOTH_A2DP, name = name, isHighRes = false)
            }

            devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES }?.let {
                val name = it.productName.toString().ifBlank { "Wired Headphones" }
                return AudioOutputDevice(type = AudioDeviceType.WIRED_HEADSET, name = name, isHighRes = true)
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
