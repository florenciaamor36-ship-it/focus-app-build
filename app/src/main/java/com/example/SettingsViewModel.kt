package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("focus_launcher_settings", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _unlockMethod = MutableStateFlow(prefs.getString("unlock_method", "Biometría") ?: "Biometría")
    val unlockMethod: StateFlow<String> = _unlockMethod.asStateFlow()

    private val _stealthTaps = MutableStateFlow(prefs.getInt("stealth_taps", 5))
    val stealthTaps: StateFlow<Int> = _stealthTaps.asStateFlow()

    private val _customPin = MutableStateFlow(prefs.getString("custom_pin", "1234") ?: "1234")
    val customPin: StateFlow<String> = _customPin.asStateFlow()

    private val _customPassword = MutableStateFlow(prefs.getString("custom_password", "1234") ?: "1234")
    val customPassword: StateFlow<String> = _customPassword.asStateFlow()

    private val _customPattern = MutableStateFlow(prefs.getString("custom_pattern", "0,1,2,5,8") ?: "0,1,2,5,8")
    val customPattern: StateFlow<String> = _customPattern.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setUnlockMethod(method: String) {
        prefs.edit().putString("unlock_method", method).apply()
        _unlockMethod.value = method
    }

    fun setStealthTaps(taps: Int) {
        prefs.edit().putInt("stealth_taps", taps).apply()
        _stealthTaps.value = taps
    }

    fun setCustomPin(pin: String) {
        prefs.edit().putString("custom_pin", pin).apply()
        _customPin.value = pin
    }

    fun setCustomPassword(password: String) {
        prefs.edit().putString("custom_password", password).apply()
        _customPassword.value = password
    }

    fun setCustomPattern(patternString: String) {
        prefs.edit().putString("custom_pattern", patternString).apply()
        _customPattern.value = patternString
    }

    fun verifyPin(input: String): Boolean {
        return input == _customPin.value
    }

    fun verifyPassword(input: String): Boolean {
        return input == _customPassword.value
    }

    fun verifyPattern(patternList: List<Int>): Boolean {
        val patternStr = patternList.joinToString(",")
        return patternStr == _customPattern.value
    }
}
