package com.viv3k.filehive.ui.screens.vault

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.viv3k.filehive.R

@Composable
fun LockAuthScreen(
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    viewModel: LockAuthViewModel = viewModel(factory = LockAuthViewModel.Factory)
) {
    val authState by viewModel.authState.collectAsState()
    val pinInput by viewModel.pinInput.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    val lockoutSeconds by viewModel.lockoutSeconds.collectAsState()

    // Assuming we transition forward when UNLOCKED
    if (authState == LockAuthState.UNLOCKED) {
        onAuthSuccess()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Folder Lock",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        when (authState) {
            LockAuthState.SETUP_SECURITY_QUESTION, LockAuthState.FORGOT_PASSWORD -> {
                SecurityQuestionContent(
                    isSetup = authState == LockAuthState.SETUP_SECURITY_QUESTION,
                    viewModel = viewModel
                )
            }
            else -> {
                PinEntryContent(
                    state = authState,
                    pinInput = pinInput,
                    error = error,
                    lockoutSeconds = lockoutSeconds,
                    onDigitClick = { viewModel.onPinDigitEntered(it) },
                    onDeleteClick = { viewModel.onPinDelete() },
                    onForgotPinClick = { viewModel.onForgotPasswordClick() },
                    onBiometricClick = {
                        triggerBiometricAuth(
                            context = context,
                            onSuccess = { viewModel.onBiometricSuccess() },
                            onError = { /* Let user fallback to PIN */ }
                        )
                    }
                )
            }
        }
    }
}

fun triggerBiometricAuth(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val fragmentActivity = context as? FragmentActivity ?: return
    val executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Vault")
        .setSubtitle("Authenticate to access locked folders")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()

    biometricPrompt.authenticate(promptInfo)
}

@Composable
fun PinEntryContent(
    state: LockAuthState,
    pinInput: String,
    error: String?,
    lockoutSeconds: Long,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onForgotPinClick: () -> Unit,
    onBiometricClick: () -> Unit
) {
    // Lock Icon
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(Color(0xFF1B2524), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.lock),
            contentDescription = "Lock",
            tint = Color(0xFF13C296),
            modifier = Modifier.size(32.dp)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    val title = when (state) {
        LockAuthState.SETUP_PIN -> "Create PIN"
        LockAuthState.CONFIRM_PIN -> "Confirm PIN"
        else -> "Enter PIN"
    }
    
    val subtitle = when(state) {
        LockAuthState.SETUP_PIN -> "Create a 4-digit PIN for the vault"
        LockAuthState.CONFIRM_PIN -> "Re-enter your 4-digit PIN"
        else -> "Enter your 4-digit PIN to access locked folders"
    }

    Text(
        text = title,
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = subtitle,
        color = Color(0xFF9AA0A6),
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Dots Indicator
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        for (i in 0 until 4) {
            val isFilled = i < pinInput.length
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        if (isFilled) Color(0xFF13C296) else Color(0xFF2B2D31),
                        CircleShape
                    )
            )
        }
    }

    // Replace error display block with:
    if (lockoutSeconds > 0) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(8.dp)),
            color = Color(0xFF3B1A1A)
        ) {
            Text(
                text = "Too many attempts\nTry again in ${lockoutSeconds}s",
                color = Color(0xFFE57373),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    } else if (error != null) {
        Text(
            text = error,
            color = Color(0xFFE57373),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }

    // Keypad
    val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(if (state == LockAuthState.UNLOCK) "BIO" else "", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (row in keypadRows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (key in row) {
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(80.dp, 60.dp))
                    } else if (key == "BIO") {
                        KeypadButton(
                            text = "",
                            icon = R.drawable.lock, // Update this if biometric icon is different
                            onClick = onBiometricClick
                        )
                    } else if (key == "DEL") {
                        KeypadButton(
                            text = "",
                            icon = R.drawable.trash, // Or use backspace icon
                            onClick = onDeleteClick
                        )
                    } else {
                        KeypadButton(
                            text = key,
                            onClick = { onDigitClick(key) }
                        )
                    }
                }
            }
        }
    }

    if (state == LockAuthState.UNLOCK) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Forgot PIN?",
            color = Color(0xFF13C296),
            fontSize = 14.sp,
            modifier = Modifier
                .clickable { onForgotPinClick() }
                .padding(8.dp)
        )
    }
}

@Composable
fun KeypadButton(
    text: String,
    icon: Int? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(80.dp, 60.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = Color(0xFF1A1C21)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Added stub for SecurityQuestionContent. Needs explicit UI implementation
@Composable
fun SecurityQuestionContent(
    isSetup: Boolean,
    viewModel: LockAuthViewModel
) {
    // Basic implementation for now
    var tempQuestion by remember { mutableStateOf("") }
    var tempAnswer by remember { mutableStateOf("") }
    val storedQuestion by viewModel.securityQuestion.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSetup) "Setup Recovery" else "Forgot PIN",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isSetup) {
            OutlinedTextField(
                value = tempQuestion,
                onValueChange = { tempQuestion = it },
                label = { Text("Security Question") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF13C296),
                    unfocusedBorderColor = Color(0xFF2B2D31),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = storedQuestion,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = tempAnswer,
            onValueChange = { tempAnswer = it },
            label = { Text("Answer") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF13C296),
                unfocusedBorderColor = Color(0xFF2B2D31),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error!!,
                color = Color(0xFFE57373),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isSetup) viewModel.setSecurityQuestionAndAnswer(tempQuestion, tempAnswer)
                else viewModel.submitSecurityAnswer(tempAnswer)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13C296)),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isSetup) "Complete Setup" else "Reset PIN")
        }
    }
}

//@Preview
//@Composable
//fun LockScreenPreview() {
//    LockAuthScreen(
//        state = LockAuthState.UNLOCK,
//        viewModel = viewModel,
//        onForgotPinClick = {},
//        onDigitClick = {},
//        onDeleteClick = {}
//    )
//}
