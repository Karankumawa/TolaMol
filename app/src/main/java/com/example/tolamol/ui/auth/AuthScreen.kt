package com.example.tolamol.ui.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tolamol.data.local.SettingsRepository
import com.example.tolamol.viewmodel.AuthState
import com.example.tolamol.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val settingsRepo = remember(context) { SettingsRepository.getInstance(context) }
    val language by settingsRepo.language.collectAsStateWithLifecycle()

    var isRegisterMode by remember { mutableStateOf(false) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var shopNameInput by remember { mutableStateOf("") }
    var shopUpiInput by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onAuthSuccess()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (language == "HI") {
                            if (isRegisterMode) "खाता बनाएं (Register)" else "लॉगिन करें (Sign In)"
                        } else {
                            if (isRegisterMode) "Create Account" else "Sign In"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )

            Text(
                text = if (language == "HI") {
                    "लॉगिन करें और अपने डाटा को सुरक्षित क्लाउड में सेव करें"
                } else {
                    "Log in to store and sync your private data space securely in Cloud"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isRegisterMode,
                            onClick = { isRegisterMode = false },
                            label = { Text(if (language == "HI") "लॉगिन (Sign In)" else "Sign In") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isRegisterMode,
                            onClick = { isRegisterMode = true },
                            label = { Text(if (language == "HI") "रजिस्टर (Sign Up)" else "Register") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text(if (language == "HI") "ईमेल / यूज़रनेम" else "Email / Username") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text(if (language == "HI") "पासवर्ड" else "Password") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(visible = isRegisterMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = shopNameInput,
                                onValueChange = { shopNameInput = it },
                                label = { Text(if (language == "HI") "दुकान का नाम / Shop Name" else "Shop Name") },
                                leadingIcon = { Icon(Icons.Rounded.Storefront, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = shopUpiInput,
                                onValueChange = { shopUpiInput = it },
                                label = { Text(if (language == "HI") "यूपीआई आईडी / UPI ID" else "Shopkeeper UPI ID") },
                                leadingIcon = { Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                viewModel.register(emailInput, passwordInput, shopNameInput, shopUpiInput, onAuthSuccess)
                            } else {
                                viewModel.login(emailInput, passwordInput, onAuthSuccess)
                            }
                        },
                        enabled = authState !is AuthState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                if (isRegisterMode) {
                                    if (language == "HI") "खाता बनाएं" else "Create Account"
                                } else {
                                    if (language == "HI") "लॉगिन करें" else "Sign In"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
