package com.milesolutions.rewardify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.milesolutions.rewardify.data.PayoutMethodRow
import com.milesolutions.rewardify.data.ReferralRepository
import com.milesolutions.rewardify.ui.components.showToast
import com.milesolutions.rewardify.ui.theme.Danger500
import com.milesolutions.rewardify.ui.theme.Emerald700
import com.milesolutions.rewardify.ui.theme.Gray100
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo100
import com.milesolutions.rewardify.ui.theme.Indigo700
import kotlinx.coroutines.launch

private val EXCHANGE_OPTIONS =
    listOf("Binance", "OKX", "Bybit", "KuCoin", "MEXC", "Bitget", "Gate.io", "Kraken", "Coinbase", "Other")
private val USDC_NETWORKS =
    listOf("Solana", "Ethereum", "Polygon", "BNB Chain", "Avalanche", "Tron")

/** Saved payout destinations: USDC wallets and exchange UIDs. */
@Composable
fun PayoutMethodsScreen(tabNavController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var methods by remember { mutableStateOf<List<PayoutMethodRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showAdd by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch {
            loading = true
            methods = runCatching { ReferralRepository.getMyPayoutMethods() }
                .getOrDefault(emptyList())
            loading = false
        }
    }
    LaunchedEffect(Unit) { refresh() }

    if (showAdd) {
        AddPayoutMethodDialog(
            onDismiss = { showAdd = false },
            onSaved = {
                showAdd = false
                context.showToast("Payout method added ✓")
                refresh()
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { tabNavController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Gray900)
            }
            Text(
                text = "Payout methods",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Indigo700)
            }
            return@Column
        }

        if (methods.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No payout methods yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add a USDC wallet or an exchange UID (Binance, OKX, …) to receive withdrawals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(methods, key = { it.id }) { method ->
                    PayoutMethodCard(
                        method = method,
                        onSetDefault = {
                            scope.launch {
                                runCatching {
                                    ReferralRepository.setDefaultPayoutMethod(method.id)
                                }.onSuccess {
                                    context.showToast("Default payout method updated ✓")
                                    refresh()
                                }.onFailure {
                                    context.showToast("Couldn't update default")
                                }
                            }
                        },
                        onDelete = {
                            scope.launch {
                                runCatching {
                                    ReferralRepository.deletePayoutMethod(method.id)
                                }.onSuccess {
                                    context.showToast("Payout method removed")
                                    refresh()
                                }.onFailure {
                                    context.showToast("Couldn't delete method")
                                }
                            }
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Indigo700)
                .clickable { showAdd = true }
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add payout method",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PayoutMethodCard(
    method: PayoutMethodRow,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    val isUsdc = method.method_type == "usdc"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !method.is_default, onClick = onSetDefault),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Indigo100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUsdc) Icons.Filled.Paid else Icons.Filled.SwapHoriz,
                    contentDescription = null,
                    tint = Indigo700,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = method.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    )
                    if (method.is_default) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Emerald700.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Emerald700
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = methodDetailLine(method),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Gray500)
            }
        }
    }
}

private fun methodDetailLine(m: PayoutMethodRow): String {
    val ref = if (m.account_ref.length > 18) {
        m.account_ref.take(8) + "…" + m.account_ref.takeLast(6)
    } else m.account_ref
    return if (m.method_type == "usdc") {
        "USDC • $ref" + (m.network?.let { " • $it" } ?: "")
    } else {
        "${m.exchange ?: "Exchange"} • UID $ref"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPayoutMethodDialog(
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isUsdc by remember { mutableStateOf(true) }
    var label by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf("") }
    var network by remember { mutableStateOf(USDC_NETWORKS.first()) }
    var exchange by remember { mutableStateOf(EXCHANGE_OPTIONS.first()) }
    var networkExpanded by remember { mutableStateOf(false) }
    var exchangeExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Add payout method",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
        },
        text = {
            Column {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TypeChip(
                        label = "USDC",
                        selected = isUsdc,
                        onClick = { isUsdc = true; error = null },
                        modifier = Modifier.weight(1f)
                    )
                    TypeChip(
                        label = "Exchange",
                        selected = !isUsdc,
                        onClick = { isUsdc = false; error = null },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it; error = null },
                    label = { Text(if (isUsdc) "Label (e.g. My USDC wallet)" else "Label (e.g. My Binance)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (isUsdc) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it.trim(); error = null },
                        label = { Text("USDC wallet address") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ExposedDropdownMenuBox(
                        expanded = networkExpanded,
                        onExpandedChange = { networkExpanded = !networkExpanded }
                    ) {
                        OutlinedTextField(
                            value = network,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Network") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(networkExpanded)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = fieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = networkExpanded,
                            onDismissRequest = { networkExpanded = false }
                        ) {
                            USDC_NETWORKS.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { network = option; networkExpanded = false }
                                )
                            }
                        }
                    }
                } else {
                    ExposedDropdownMenuBox(
                        expanded = exchangeExpanded,
                        onExpandedChange = { exchangeExpanded = !exchangeExpanded }
                    ) {
                        OutlinedTextField(
                            value = exchange,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Exchange") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(exchangeExpanded)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = fieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = exchangeExpanded,
                            onDismissRequest = { exchangeExpanded = false }
                        ) {
                            EXCHANGE_OPTIONS.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { exchange = option; exchangeExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = uid,
                        onValueChange = { uid = it.trim(); error = null },
                        label = { Text("Exchange UID") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Danger500
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    val cleanLabel = label.trim()
                    error = when {
                        cleanLabel.isEmpty() -> "Give this method a label"
                        isUsdc && address.length < 10 -> "Enter a valid wallet address"
                        !isUsdc && uid.isEmpty() -> "Enter your exchange UID"
                        else -> null
                    }
                    if (error != null) return@TextButton
                    scope.launch {
                        saving = true
                        val result = runCatching {
                            if (isUsdc) {
                                ReferralRepository.addPayoutMethod(
                                    methodType = "usdc",
                                    label = cleanLabel,
                                    exchange = null,
                                    accountRef = address,
                                    network = network
                                )
                            } else {
                                ReferralRepository.addPayoutMethod(
                                    methodType = "exchange",
                                    label = cleanLabel,
                                    exchange = exchange,
                                    accountRef = uid,
                                    network = null
                                )
                            }
                        }
                        saving = false
                        result.onSuccess { onSaved() }
                            .onFailure {
                                error = it.message?.take(80) ?: "Couldn't save — try again"
                            }
                    }
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Indigo700)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray500)
            }
        }
    )
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Indigo700 else Gray100)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Gray500
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Indigo700,
    cursorColor = Indigo700
)
