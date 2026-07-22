package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.data.model.Transaction
import com.example.ui.theme.NexGenBlueSecondary
import com.example.ui.theme.NexGenIndigoPrimary
import com.example.ui.theme.RemitaGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RemitaCheckoutDialog(
    course: Course,
    rrr: String,
    onDismiss: () -> Unit,
    onConfirmPayment: (paymentMethod: String) -> Unit,
    isProcessing: Boolean = false
) {
    var selectedMethod by remember { mutableStateOf("Debit / Credit Card") }
    val serviceFee = 100.0
    val totalAmount = course.priceNgn + serviceFee

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFFFFECE5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "remita",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD9381E),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text("Remita Secure Gateway", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Order Summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = course.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tutor: ${course.tutorName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Course Fee", fontSize = 12.sp)
                            Text("₦%,.2f".format(course.priceNgn), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remita Processing Fee", fontSize = 12.sp)
                            Text("₦%,.2f".format(serviceFee), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payable", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("₦%,.2f".format(totalAmount), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = RemitaGreen)
                        }
                    }
                }

                // RRR Code Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "REMITA RETRIEVAL REFERENCE (RRR)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = rrr,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = NexGenIndigoPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Text("Select Payment Method:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                val methods = listOf(
                    Pair("Debit / Credit Card", Icons.Outlined.CreditCard),
                    Pair("Bank Transfer", Icons.Default.AccountBalance),
                    Pair("USSD Code (*737# / *919#)", Icons.Default.PhoneAndroid),
                    Pair("Remita RRR Direct Pay", Icons.Outlined.QrCode)
                )

                methods.forEach { (method, icon) ->
                    val isSelected = selectedMethod == method
                    Surface(
                        onClick = { selectedMethod = method },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedMethod = method }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = icon, contentDescription = method, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = method, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmPayment(selectedMethod) },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = RemitaGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("remita_authorize_pay_btn")
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verifying Remita Payment...")
                } else {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay ₦%,.2f via Remita".format(totalAmount), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RemitaReceiptDialog(
    transaction: Transaction,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateStr = remember(transaction.timestamp) { dateFormat.format(Date(transaction.timestamp)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Outlined.ReceiptLong, contentDescription = "Receipt", tint = RemitaGreen)
                    Text("Remita Payment Receipt", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Success Banner
                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = "Success", tint = RemitaGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PAYMENT SUCCESSFUL", fontWeight = FontWeight.ExtraBold, color = Color(0xFF065F46), fontSize = 14.sp)
                    }
                }

                // RRR Code Header
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("REMITA RETRIEVAL REFERENCE (RRR)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = transaction.remitaRrr,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = NexGenIndigoPrimary
                        )
                    }
                }

                // Line items
                ReceiptLineItem("Billed To", transaction.studentName)
                ReceiptLineItem("Student Email", transaction.studentEmail)
                ReceiptLineItem("Course Title", transaction.courseTitle)
                ReceiptLineItem("Payment Gateway", "Remita (${transaction.paymentMethod})")
                ReceiptLineItem("Transaction Date", dateStr)
                ReceiptLineItem("Course Tuition", "₦%,.2f".format(transaction.amountNgn))
                ReceiptLineItem("Remita Fee", "₦%,.2f".format(transaction.remitaServiceFeeNgn))

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Paid", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("₦%,.2f".format(transaction.totalAmountNgn), fontWeight = FontWeight.Black, fontSize = 18.sp, color = RemitaGreen)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("close_receipt_btn")
            ) {
                Text("Done & Continue Learning")
            }
        }
    )
}

@Composable
private fun ReceiptLineItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
