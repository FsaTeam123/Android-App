package com.example.androidapprpg.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    // ---- Nomes de arquivos/keys ----
    private companion object {
        private const val SECURE_PREFS = "UserPrefs.secure"
        private const val KEY_USER_ID  = "USER_ID"
        private const val KEY_TOKEN    = "TOKEN"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "SESSION_AES_KEY"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(SECURE_PREFS, Context.MODE_PRIVATE)
    }


    // ===================== API pública =====================

    fun saveLogin(id: Long, token: String?) {
        putLongSecure(KEY_USER_ID, id)
        putStringSecure(KEY_TOKEN, token)
    }

    fun saveToken(token: String?) {
        putStringSecure(KEY_TOKEN, token)
    }

    fun getUserIdOrNull(): Long? = getLongSecure(KEY_USER_ID)

    fun getToken(): String? = getStringSecure(KEY_TOKEN)

    fun isLoggedIn(): Boolean = getUserIdOrNull() != null && getToken() != null

    fun logout() { prefs.edit().clear().apply() }

    // ===================== Criptografia =====================

    private fun getOrCreateSecretKey(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            // .setUserAuthenticationRequired(true) // opcional: exige biometria/bloqueio antes de usar a chave
            .build()
        keyGen.init(spec)
        return keyGen.generateKey()
    }

    private fun encrypt(plain: ByteArray): Pair<String, String> {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val ivB64 = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val ctB64 = Base64.encodeToString(cipher.doFinal(plain), Base64.NO_WRAP)
        return ivB64 to ctB64
    }

    private fun decrypt(ivB64: String, ctB64: String): ByteArray {
        val iv = Base64.decode(ivB64, Base64.NO_WRAP)
        val ct = Base64.decode(ctB64, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(GCM_TAG_BITS, iv)
        )
        return cipher.doFinal(ct)
    }

    private fun putStringSecure(key: String, value: String?) {
        if (value.isNullOrEmpty()) {
            prefs.edit().remove("${key}_iv").remove("${key}_ct").apply()
            return
        }
        val (iv, ct) = encrypt(value.toByteArray(Charsets.UTF_8))
        prefs.edit().putString("${key}_iv", iv).putString("${key}_ct", ct).apply()
    }

    private fun getStringSecure(key: String): String? {
        val iv = prefs.getString("${key}_iv", null) ?: return null
        val ct = prefs.getString("${key}_ct", null) ?: return null
        return try {
            String(decrypt(iv, ct), Charsets.UTF_8)
        } catch (_: Exception) {
            // Chave inválida (ex.: app reinstalado) ou dados corrompidos.
            // Limpe a entrada para evitar loops e retorne null.
            prefs.edit().remove("${key}_iv").remove("${key}_ct").apply()
            null
        }
    }

    private fun putLongSecure(key: String, value: Long) =
        putStringSecure(key, value.toString())

    private fun getLongSecure(key: String): Long? =
        getStringSecure(key)?.toLongOrNull()

}
