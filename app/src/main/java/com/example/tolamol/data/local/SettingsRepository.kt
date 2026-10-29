package com.example.tolamol.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tolamol_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "EN") ?: "EN")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _shopName = MutableStateFlow(getValidShopName(prefs.getString(KEY_SHOP_NAME, null)))
    val shopName: StateFlow<String> = _shopName.asStateFlow()

    private val _shopUpiId = MutableStateFlow(getValidShopUpiId(prefs.getString(KEY_SHOP_UPI_ID, null)))
    val shopUpiId: StateFlow<String> = _shopUpiId.asStateFlow()

    private val _userId = MutableStateFlow(prefs.getString(KEY_USER_ID, "") ?: "")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString(KEY_USER_EMAIL, "") ?: "")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(!_userId.value.isBlank())
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _soundAndHaptic = MutableStateFlow(prefs.getBoolean(KEY_SOUND_HAPTIC, true))
    val soundAndHaptic: StateFlow<Boolean> = _soundAndHaptic.asStateFlow()

    init {
        val initialShopName = getValidShopName(prefs.getString(KEY_SHOP_NAME, null))
        val initialShopUpi = getValidShopUpiId(prefs.getString(KEY_SHOP_UPI_ID, null))
        prefs.edit()
            .putString(KEY_SHOP_NAME, initialShopName)
            .putString(KEY_SHOP_UPI_ID, initialShopUpi)
            .apply()
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setShopName(name: String) {
        val validName = getValidShopName(name)
        prefs.edit().putString(KEY_SHOP_NAME, validName).apply()
        _shopName.value = validName
    }

    fun setShopUpiId(upiId: String) {
        val validUpi = getValidShopUpiId(upiId)
        prefs.edit().putString(KEY_SHOP_UPI_ID, validUpi).apply()
        _shopUpiId.value = validUpi
    }

    fun verifyAdminPin(pin: String): Boolean {
        return if (pin == DEFAULT_ADMIN_PIN) {
            _isAdminLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    fun saveUserSession(id: String, email: String, name: String = "", upi: String = "") {
        prefs.edit()
            .putString(KEY_USER_ID, id)
            .putString(KEY_USER_EMAIL, email)
            .apply()
        _userId.value = id
        _userEmail.value = email
        _isUserLoggedIn.value = true

        if (name.isNotBlank()) setShopName(name)
        if (upi.isNotBlank()) setShopUpiId(upi)
    }

    fun logoutUser() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .apply()
        _userId.value = ""
        _userEmail.value = ""
        _isUserLoggedIn.value = false
    }

    companion object {
        const val DEFAULT_SHOP_NAME = "karan kumawat"
        const val DEFAULT_SHOP_UPI_ID = "karan3780@ptyes"
        const val DEFAULT_ADMIN_PIN = "1234"

        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_SHOP_UPI_ID = "shop_upi_id"
        private const val KEY_SOUND_HAPTIC = "sound_haptic_feedback"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"

        fun getValidShopName(name: String?): String {
            if (name.isNullOrBlank()) {
                return DEFAULT_SHOP_NAME
            }
            return name
        }

        fun getValidShopUpiId(upiId: String?): String {
            if (upiId.isNullOrBlank()) {
                return DEFAULT_SHOP_UPI_ID
            }
            return upiId
        }

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
