package com.example.tolamol.ui.settings

import android.widget.Toast
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
import com.example.tolamol.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val savedShopName by viewModel.shopName.collectAsStateWithLifecycle()
    val savedShopUpiId by viewModel.shopUpiId.collectAsStateWithLifecycle()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val soundAndHaptic by viewModel.soundAndHaptic.collectAsStateWithLifecycle()

    var shopNameInput by remember(savedShopName) { mutableStateOf(savedShopName) }
    var shopUpiInput by remember(savedShopUpiId) { mutableStateOf(savedShopUpiId) }

    var showAdminDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminPinError by remember { mutableStateOf(false) }

    var showResetDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (language == "HI") "सेटिंग्स और भाषा" else "Settings & Language",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Language Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (language == "HI") "ऐप की भाषा / App Language" else "App Language",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = language == "EN",
                            onClick = { viewModel.setLanguage("EN") },
                            label = { Text("English (EN)") },
                            leadingIcon = if (language == "EN") {
                                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = language == "HI",
                            onClick = { viewModel.setLanguage("HI") },
                            label = { Text("हिंदी (HI)") },
                            leadingIcon = if (language == "HI") {
                                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Theme Toggle Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (language == "HI") "ऐप थीम / App Theme" else "App Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = themeMode == "LIGHT",
                            onClick = { viewModel.setThemeMode("LIGHT") },
                            label = { Text(if (language == "HI") "लाइट" else "Light") },
                            leadingIcon = { Icon(Icons.Rounded.LightMode, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = themeMode == "DARK",
                            onClick = { viewModel.setThemeMode("DARK") },
                            label = { Text(if (language == "HI") "डार्क" else "Dark") },
                            leadingIcon = { Icon(Icons.Rounded.DarkMode, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = themeMode == "SYSTEM",
                            onClick = { viewModel.setThemeMode("SYSTEM") },
                            label = { Text(if (language == "HI") "सिस्टम" else "System") },
                            leadingIcon = { Icon(Icons.Rounded.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Shop Details Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (language == "HI") "दुकान की जानकारी" else "Shopkeeper Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (isAdminLoggedIn) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Admin Mode Active", fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.LockOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    if (isAdminLoggedIn) {
                        OutlinedTextField(
                            value = shopNameInput,
                            onValueChange = { shopNameInput = it },
                            label = { Text(if (language == "HI") "दुकान का नाम / Shop Name" else "Shop Name") },
                            placeholder = { Text("e.g. ${SettingsRepository.DEFAULT_SHOP_NAME}") },
                            leadingIcon = { Icon(Icons.Rounded.Storefront, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = shopUpiInput,
                            onValueChange = { shopUpiInput = it },
                            label = { Text(if (language == "HI") "यूपीआई आईडी / Shopkeeper UPI ID" else "Shopkeeper UPI ID") },
                            placeholder = { Text("e.g. ${SettingsRepository.DEFAULT_SHOP_UPI_ID}") },
                            leadingIcon = { Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    viewModel.setShopName(shopNameInput.trim())
                                    viewModel.setShopUpiId(shopUpiInput.trim())
                                    Toast.makeText(
                                        context,
                                        if (language == "HI") "सेटिंग्स सेव हो गईं!" else "Settings saved successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (language == "HI") "सेव करें" else "Save Settings")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.logoutAdmin()
                                    Toast.makeText(
                                        context,
                                        if (language == "HI") "एडमिन लॉगआउट हो गया" else "Logged out from Admin mode",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Lock, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (language == "HI") "लॉगआउट एडमिन" else "Logout Admin")
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = savedShopName,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text(if (language == "HI") "दुकान का नाम / Shop Name" else "Shop Name") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = savedShopUpiId,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text(if (language == "HI") "यूपीआई आईडी / Shopkeeper UPI ID" else "Shopkeeper UPI ID") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { showAdminDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (language == "HI") "अनलॉक / एडमिन लॉगिन" else "Unlock / Admin Login")
                        }
                    }
                }
            }

            // Sound & Haptic Feedback Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = if (language == "HI") "साउंड और हैप्टिक फीडबैक" else "Sound & Haptic Feedback",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (language == "HI") "कीप्रेस वाइब्रेशन और साउंड प्रतिक्रियाएं" else "Keypress vibration & sound preferences",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = soundAndHaptic,
                        onCheckedChange = { viewModel.setSoundAndHaptic(it) }
                    )
                }
            }

            // Cloud Database Status Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (language == "HI") "क्लाउड डेटाबेस स्थिति" else "Cloud Database Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "MongoDB Atlas: DATA.mainData",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = if (language == "HI") "स्थिति: ऑनलाइन और सिंक कनेक्टेड" else "Status: Online & Sync Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Reset Local Data Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (language == "HI") "लोकल डेटा रीसेट करें" else "Reset Local Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = if (language == "HI") "सभी सहेजे गए लोकल रेट्स और अस्थायी बिल डेटा हटाएँ।" else "Clear local temporary item rates and saved calculation items.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (language == "HI") "डेटा रीसेट करें" else "Reset Local Data")
                    }
                }
            }

            // App Info & Developer Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (language == "HI") "ऐप जानकारी और डेवलपर" else "App Info & Developer Card",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == "HI") "संस्करण / Version:" else "App Version:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                "Version 2.0.0",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == "HI") "डेवलपर / Developer:" else "Developer:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Karan Kumawat",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == "HI") "सहायता और संपर्क:" else "Contact Support:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "karankumawat3780@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }

    // Admin Login Dialog
    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminDialog = false
                adminPinInput = ""
                adminPinError = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (language == "HI") "एडमिन लॉगिन" else "Admin Login",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (language == "HI") "एडमिन फीचर्स अनलॉक करने के लिए पिन दर्ज करें:" else "Enter Admin PIN to unlock shopkeeper settings:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = {
                            adminPinInput = it
                            adminPinError = false
                        },
                        label = { Text("Admin PIN") },
                        placeholder = { Text("Default: 1234") },
                        singleLine = true,
                        isError = adminPinError,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        supportingText = if (adminPinError) {
                            { Text("Incorrect Admin PIN! Please try again.", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.verifyAdminPin(adminPinInput.trim())
                        if (success) {
                            showAdminDialog = false
                            adminPinInput = ""
                            adminPinError = false
                            Toast.makeText(
                                context,
                                if (language == "HI") "एडमिन मोड अनलॉक हो गया!" else "Admin Mode Unlocked!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            adminPinError = true
                        }
                    }
                ) {
                    Text(if (language == "HI") "अनलॉक" else "Unlock")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminDialog = false
                        adminPinInput = ""
                        adminPinError = false
                    }
                ) {
                    Text(if (language == "HI") "रद्द करें" else "Cancel")
                }
            }
        )
    }

    // Reset Data Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(if (language == "HI") "लोकल डेटा रीसेट करें?" else "Reset Local Data?")
            },
            text = {
                Text(
                    if (language == "HI")
                        "क्या आप निश्चित रूप से सभी स्थानीय रूप से सहेजे गए आइटम रेट और अस्थायी बिल डेटा हटाना चाहते हैं? यह क्रिया पूर्ववत नहीं की जा सकती।"
                    else
                        "Are you sure you want to clear all locally saved item rates and temporary data? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        viewModel.clearLocalData {
                            Toast.makeText(
                                context,
                                if (language == "HI") "लोकल डेटा सफलतापूर्वक रीसेट हो गया!" else "Local data reset successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(if (language == "HI") "हाँ, रीसेट करें" else "Reset Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (language == "HI") "रद्द करें" else "Cancel")
                }
            }
        )
    }
}
