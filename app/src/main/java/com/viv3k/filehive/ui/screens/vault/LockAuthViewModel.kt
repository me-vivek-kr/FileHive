package com.viv3k.filehive.ui.screens.vault

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viv3k.filehive.data.repository.SecurityRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class LockAuthState {
    CHECKING,
    SETUP_PIN,
    CONFIRM_PIN,
    SETUP_SECURITY_QUESTION,
    UNLOCK,
    FORGOT_PASSWORD,
    UNLOCKED
}

class LockAuthViewModel(private val securityRepository: SecurityRepository) : ViewModel() {

    private val _authState = MutableStateFlow(LockAuthState.CHECKING)
    val authState: StateFlow<LockAuthState> = _authState.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _tempSetupPin = MutableStateFlow("")

    private val _securityQuestion = MutableStateFlow("")
    val securityQuestion: StateFlow<String> = _securityQuestion.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Lockout countdown in seconds (0 = not locked out)
    private val _lockoutSeconds = MutableStateFlow(0L)
    val lockoutSeconds: StateFlow<Long> = _lockoutSeconds.asStateFlow()

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            val hasPin = securityRepository.hasPin.firstOrNull() ?: false
            _authState.value = if (hasPin) LockAuthState.UNLOCK else LockAuthState.SETUP_PIN
            if (hasPin) checkLockout()
        }
    }

    private fun checkLockout() {
        val remainingMs = securityRepository.getLockoutRemainingMs()
        if (remainingMs > 0) {
            startLockoutCountdown(remainingMs)
        }
    }

    private fun startLockoutCountdown(remainingMs: Long) {
        viewModelScope.launch {
            var remaining = (remainingMs / 1000) + 1
            while (remaining > 0) {
                _lockoutSeconds.value = remaining
                _errorMessage.value = "Too many attempts. Try again in ${remaining}s"
                delay(1000)
                remaining--
            }
            _lockoutSeconds.value = 0
            _errorMessage.value = null
        }
    }

    fun onPinDigitEntered(digit: String) {
        if (securityRepository.isLockedOut()) return
        if (_pinInput.value.length < 4) {
            _pinInput.value += digit
            _errorMessage.value = null
            if (_pinInput.value.length == 4) processCompletePin()
        }
    }

    fun onPinDelete() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
            _errorMessage.value = null
        }
    }

    private fun processCompletePin() {
        viewModelScope.launch {
            when (_authState.value) {
                LockAuthState.SETUP_PIN -> {
                    _tempSetupPin.value = _pinInput.value
                    _pinInput.value = ""
                    _authState.value = LockAuthState.CONFIRM_PIN
                }
                LockAuthState.CONFIRM_PIN -> {
                    if (_pinInput.value == _tempSetupPin.value) {
                        _pinInput.value = ""
                        _authState.value = LockAuthState.SETUP_SECURITY_QUESTION
                    } else {
                        _pinInput.value = ""
                        _errorMessage.value = "PINs do not match. Try again."
                        _authState.value = LockAuthState.SETUP_PIN
                        _tempSetupPin.value = ""
                    }
                }
                LockAuthState.UNLOCK -> {
                    val isCorrect = securityRepository.verifyPin(_pinInput.value)
                    if (isCorrect) {
                        _authState.value = LockAuthState.UNLOCKED
                    } else {
                        _pinInput.value = ""
                        val remainingMs = securityRepository.getLockoutRemainingMs()
                        if (remainingMs > 0) {
                            startLockoutCountdown(remainingMs)
                        } else {
                            _errorMessage.value = "Incorrect PIN."
                        }
                    }
                }
                else -> {}
            }
        }
    }

    fun setSecurityQuestionAndAnswer(question: String, answer: String) {
        if (question.isBlank() || answer.isBlank()) {
            _errorMessage.value = "Question and answer cannot be empty"
            return
        }
        viewModelScope.launch {
            securityRepository.setPin(_tempSetupPin.value)
            securityRepository.setSecurityQuestion(question, answer)
            _authState.value = LockAuthState.UNLOCKED
        }
    }

    fun onForgotPasswordClick() {
        viewModelScope.launch {
            val question = securityRepository.securityQuestion.firstOrNull()
            if (question != null) {
                _securityQuestion.value = question
                _authState.value = LockAuthState.FORGOT_PASSWORD
            } else {
                _errorMessage.value = "No security question set."
            }
        }
    }

    fun submitSecurityAnswer(answer: String) {
        viewModelScope.launch {
            val isCorrect = securityRepository.verifySecurityAnswer(answer)
            if (isCorrect) {
                securityRepository.resetPin()
                _pinInput.value = ""
                _tempSetupPin.value = ""
                _authState.value = LockAuthState.SETUP_PIN
            } else {
                _errorMessage.value = "Incorrect answer."
            }
        }
    }

    fun onBiometricSuccess() {
        _authState.value = LockAuthState.UNLOCKED
    }

    fun resetError() {
        _errorMessage.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // ✅ Fixed: single instance, not two
                val context = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Context
                LockAuthViewModel(SecurityRepository(context))
            }
        }
    }
}