package com.viv3k.filehive.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest

class SecurityRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_vault_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_QUESTION = "security_question"
        private const val KEY_ANSWER_HASH = "answer_hash"
        private const val KEY_FAIL_COUNT = "fail_count"
        private const val KEY_LOCKOUT_TIME = "lockout_time"

        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds

        private fun sha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256")
                .digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    val hasPin: Flow<Boolean> = flow {
        emit(prefs.getString(KEY_PIN_HASH, null) != null)
    }

    val securityQuestion: Flow<String?> = flow {
        emit(prefs.getString(KEY_QUESTION, null))
    }

    // Returns null if not locked out, or remaining ms if locked out
    fun getLockoutRemainingMs(): Long {
        val failCount = prefs.getInt(KEY_FAIL_COUNT, 0)
        if (failCount < MAX_ATTEMPTS) return 0L
        val lockoutTime = prefs.getLong(KEY_LOCKOUT_TIME, 0L)
        val elapsed = System.currentTimeMillis() - lockoutTime
        return if (elapsed < LOCKOUT_DURATION_MS) LOCKOUT_DURATION_MS - elapsed else 0L
    }

    fun isLockedOut(): Boolean = getLockoutRemainingMs() > 0L

    fun verifyPin(pin: String): Boolean {
        if (isLockedOut()) return false

        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val correct = sha256(pin) == stored

        if (correct) {
            resetFailCount()
        } else {
            recordFailedAttempt()
        }
        return correct
    }

    fun setPin(pin: String) {
        prefs.edit()
            .putString(KEY_PIN_HASH, sha256(pin))
            .apply()
        resetFailCount()
    }

    fun resetPin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .apply()
        resetFailCount()
    }

    fun setSecurityQuestion(question: String, answer: String) {
        prefs.edit()
            .putString(KEY_QUESTION, question)
            .putString(KEY_ANSWER_HASH, sha256(answer.trim().lowercase()))
            .apply()
    }

    fun verifySecurityAnswer(answer: String): Boolean {
        val stored = prefs.getString(KEY_ANSWER_HASH, null) ?: return false
        return sha256(answer.trim().lowercase()) == stored
    }

    private fun recordFailedAttempt() {
        val count = prefs.getInt(KEY_FAIL_COUNT, 0) + 1
        prefs.edit()
            .putInt(KEY_FAIL_COUNT, count)
            .putLong(KEY_LOCKOUT_TIME, System.currentTimeMillis())
            .apply()
    }

    private fun resetFailCount() {
        prefs.edit()
            .putInt(KEY_FAIL_COUNT, 0)
            .remove(KEY_LOCKOUT_TIME)
            .apply()
    }
}