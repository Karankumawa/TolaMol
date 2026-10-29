package com.example.tolamol.ui.billing

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tolamol.data.local.SettingsRepository
import com.example.tolamol.viewmodel.QuickBillingViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickBillingScreen(
    viewModel: QuickBillingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val settingsRepo = remember(context) { SettingsRepository.getInstance(context) }
    val language by settingsRepo.language.collectAsStateWithLifecycle()
    var showQrDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val formattedTotal = String.format(Locale.US, "%.2f", state.grandTotal)

    fun launchUpiPayment() {
        if (state.grandTotal <= 0.0) {
            Toast.makeText(context, if (language == "HI") "यूपीआई भुगतान के लिए कुल बिल ₹0.00 से अधिक होना चाहिए" else "Bill total must be greater than ₹0.00 to process UPI payment", Toast.LENGTH_SHORT).show()
            return
        }
        val upiUriString = viewModel.getUpiPaymentUri()
        try {
            val upiUri = Uri.parse(upiUriString)
            val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
            val chooser = Intent.createChooser(upiIntent, if (language == "HI") "यूपीआई से भुगतान करें" else "Pay via UPI")
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                if (language == "HI") "कोई यूपीआई ऐप (GPay, PhonePe, Paytm आदि) नहीं मिला। क्यूआर कोड दिखाया जा रहा है।" else "No UPI app (GPay, PhonePe, Paytm, etc.) found. Showing QR Code instead.",
                Toast.LENGTH_LONG
            ).show()
            showQrDialog = true
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error launching UPI: ${e.localizedMessage ?: "Unknown error"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    if (showQrDialog) {
        val upiUri = viewModel.getUpiPaymentUri()
        val qrBitmap = remember(upiUri) { generateQrCodeBitmap(upiUri, 600) }

        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Text(
                    text = if (language == "HI") "यूपीआई क्यूआर कोड" else "UPI Payment QR Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = if (language == "HI") "प्राप्तकर्ता: ${SettingsRepository.getValidShopName(state.shopName)}" else "Payee: ${SettingsRepository.getValidShopName(state.shopName)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "UPI ID: ${SettingsRepository.getValidShopUpiId(state.shopUpiId)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (language == "HI") "राशि: ₹$formattedTotal" else "Amount: ₹$formattedTotal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (qrBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = androidx.compose.ui.graphics.Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Image(
                                bitmap = qrBitmap,
                                contentDescription = "UPI Payment QR Code",
                                modifier = Modifier
                                    .size(220.dp)
                                    .padding(12.dp)
                            )
                        }
                    } else {
                        Text(if (language == "HI") "क्यूआर कोड बनाने में त्रुटि" else "Error generating QR Code", color = MaterialTheme.colorScheme.error)
                    }

                    Text(
                        text = if (language == "HI") "भुगतान करने के लिए गूगल पे, फोनपे, पेटीएम या भीम यूपीआई से स्कैन करें।" else "Scan this QR code using Google Pay, PhonePe, Paytm, BHIM or any UPI app to pay.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showQrDialog = false
                    launchUpiPayment()
                }) {
                    Text(if (language == "HI") "यूपीआई ऐप खोलें" else "Open UPI App")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showQrDialog = false }) {
                    Text(if (language == "HI") "बंद करें" else "Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (language == "HI") "त्वरित बिलिंग" else "Quick Billing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (language == "HI") "कुल: ₹$formattedTotal" else "Total: ₹$formattedTotal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = {
                            val shareText = buildString {
                                appendLine(if (language == "HI") "*त्वरित बिल*" else "*Quick Bill*")
                                appendLine("------------")
                                state.items.forEach { item ->
                                    val qtyStr = if (item.quantity.isNotBlank()) "(${item.quantity})" else ""
                                    appendLine("${item.name} $qtyStr - ₹${item.subtotal}")
                                }
                                appendLine("------------")
                                if (state.discount.isNotBlank() && (state.discount.toDoubleOrNull() ?: 0.0) > 0) {
                                    val subtotalVal = state.items.sumOf { it.subtotal.toDoubleOrNull() ?: 0.0 }
                                    appendLine(String.format(Locale.US, if (language == "HI") "उप-योग: ₹%.2f" else "Subtotal: ₹%.2f", subtotalVal))
                                    appendLine(if (language == "HI") "छूट: ₹${state.discount}" else "Discount: ₹${state.discount}")
                                }
                                appendLine(if (language == "HI") "कुल योग: ₹$formattedTotal" else "Grand Total: ₹$formattedTotal")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share Bill")
                        }

                        IconButton(onClick = {
                            if (state.grandTotal <= 0.0) {
                                Toast.makeText(context, if (language == "HI") "क्यूआर कोड दिखाने के लिए बिल कुल ₹0.00 से अधिक होना चाहिए" else "Bill total must be greater than ₹0.00 to show QR code", Toast.LENGTH_SHORT).show()
                            } else {
                                showQrDialog = true
                            }
                        }) {
                            Icon(Icons.Rounded.QrCode, contentDescription = "Show UPI QR Code")
                        }

                        FloatingActionButton(onClick = {
                            launchUpiPayment()
                        }) {
                            Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Receive via UPI")
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    viewModel.updateCurrentItemName("")
                    viewModel.updateCurrentItemQty("")
                    viewModel.updateCurrentItemSubtotal("")
                    viewModel.updateDiscount("")
                    delay(600)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.currentItemName,
                        onValueChange = viewModel::updateCurrentItemName,
                        label = { Text(if (language == "HI") "आइटम का नाम" else "Item Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.currentItemQty,
                            onValueChange = viewModel::updateCurrentItemQty,
                            label = { Text(if (language == "HI") "वज़न/मात्रा" else "Weight/Qty") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.currentItemSubtotal,
                            onValueChange = viewModel::updateCurrentItemSubtotal,
                            label = { Text(if (language == "HI") "कीमत (₹)" else "Price (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = viewModel::addItem,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Item", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.items) { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        supportingContent = { if (item.quantity.isNotBlank()) Text("Qty: ${item.quantity}") },
                        trailingContent = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("₹${item.subtotal}", fontWeight = FontWeight.Bold)
                                IconButton(onClick = { viewModel.removeItem(item) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Remove")
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }
            }
            
            OutlinedTextField(
                value = state.discount,
                onValueChange = viewModel::updateDiscount,
                label = { Text("Discount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
}

private fun generateQrCodeBitmap(content: String, sizePx: Int = 512): ImageBitmap? {
    if (content.isBlank()) return null
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
