package com.milesolutions.rewardify.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.milesolutions.rewardify.data.PayoutMethodRow
import com.milesolutions.rewardify.ui.theme.Danger500
import com.milesolutions.rewardify.ui.theme.Emerald600
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo700

const val MIN_WITHDRAWAL = 5.00

/** Withdraw dialog with amount validation. Calls [onConfirm] only for a valid amount. */
@Composable
fun WithdrawDialog(
    availableBalance: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Withdraw funds",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
        },
        text = {
            Column {
                Text(
                    text = "Available: $${"%.2f".format(availableBalance)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        error = null
                    },
                    label = { Text("Amount") },
                    prefix = { Text("$") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo700,
                        cursorColor = Indigo700
                    )
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Danger500
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Minimum withdrawal $${"%.2f".format(MIN_WITHDRAWAL)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    error = when {
                        amount == null || amount <= 0 ->
                            "Please enter a valid amount"
                        amount < MIN_WITHDRAWAL ->
                            "Minimum withdrawal is $${"%.2f".format(MIN_WITHDRAWAL)}"
                        amount > availableBalance ->
                            "Insufficient balance"
                        else -> null
                    }
                    if (error == null && amount != null) onConfirm(amount)
                }
            ) {
                Text("Withdraw", fontWeight = FontWeight.Bold, color = Indigo700)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray500)
            }
        }
    )
}

/** Gift-card redemption dialog with code validation. */
@Composable
fun RedeemDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Redeem gift card",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the code from your gift card to add it to your wallet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase()
                        error = null
                    },
                    label = { Text("Gift card code") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo700,
                        cursorColor = Indigo700
                    )
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
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
                onClick = {
                    error = when {
                        code.isBlank() -> "Please enter your gift card code"
                        code.length < 6 -> "Code looks too short — check and try again"
                        else -> null
                    }
                    if (error == null) onConfirm(code)
                }
            ) {
                Text("Redeem", fontWeight = FontWeight.Bold, color = Emerald600)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray500)
            }
        }
    )
}

/**
 * Real withdrawal dialog: pick one of the user's payout methods (USDC wallet
 * or exchange UID), enter an amount, and confirm. Calls [onConfirm] with the
 * chosen method id and a validated amount (>= $5.00, <= [availableBalance]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestWithdrawDialog(
    availableBalance: Double,
    methods: List<PayoutMethodRow>,
    onDismiss: () -> Unit,
    onConfirm: (methodId: String, amount: Double) -> Unit
) {
    var selectedId by remember {
        mutableStateOf(methods.firstOrNull { it.is_default }?.id ?: methods.firstOrNull()?.id)
    }
    var expanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val selected = methods.firstOrNull { it.id == selectedId }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Withdraw funds",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
        },
        text = {
            Column {
                Text(
                    text = "Available: $${"%.2f".format(availableBalance)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Payout method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selected?.let { methodLabel(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Indigo700,
                            cursorColor = Indigo700
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        methods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(methodLabel(method)) },
                                onClick = {
                                    selectedId = method.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        error = null
                    },
                    label = { Text("Amount") },
                    prefix = { Text("$") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo700,
                        cursorColor = Indigo700
                    )
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Danger500
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Minimum withdrawal $${"%.2f".format(MIN_WITHDRAWAL)}. " +
                            "Requests show as Pending until paid.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    error = when {
                        selectedId == null -> "Pick a payout method"
                        amount == null || amount <= 0 -> "Please enter a valid amount"
                        amount < MIN_WITHDRAWAL ->
                            "Minimum withdrawal is $${"%.2f".format(MIN_WITHDRAWAL)}"
                        amount > availableBalance -> "Insufficient balance"
                        else -> null
                    }
                    if (error == null && amount != null && selectedId != null) {
                        onConfirm(selectedId!!, amount)
                    }
                }
            ) {
                Text("Withdraw", fontWeight = FontWeight.Bold, color = Indigo700)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray500)
            }
        }
    )
}

private fun methodLabel(m: PayoutMethodRow): String =
    if (m.method_type == "usdc") {
        "${m.label} • USDC" + (m.network?.let { " ($it)" } ?: "")
    } else {
        "${m.label} • ${m.exchange ?: "Exchange"} UID"
    }
