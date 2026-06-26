package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ExtractedExpense
import com.example.data.model.AiMessage
import com.example.ui.theme.Loc
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopilotChatScreen(
    messages: List<AiMessage>,
    isAnalyzing: Boolean,
    extractedResult: List<ExtractedExpense>?,
    errorState: String?,
    lang: String,
    currency: String,
    onSendMessage: (String) -> Unit,
    onSendImage: (Bitmap) -> Unit,
    onApproveExtracted: () -> Unit,
    onDiscardExtracted: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showScanDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().testTag("copilot_screen_root")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Quick strategic suggestion chips at top
            LazyColumn(
                modifier = Modifier
                    .weight(1.0f)
                    .testTag("chat_messages_column"),
                state = lazyListState,
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = Loc.get("ai_strategic_help", lang),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = if (lang == "ar") TextAlign.Right else TextAlign.Left
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val prompts = if (lang == "ar") {
                                listOf(
                                    "كيف يمكنني تقليل تكاليف التشغيل؟" to "تكاليف التشغيل",
                                    "هل هناك ميزانيات مهددة بالتجاوز؟" to "تحذير الميزانية",
                                    "تحليل لأداء وأسعار الموردين" to "تحليل الموردين"
                                )
                            } else {
                                listOf(
                                    "How can I reduce operating costs?" to "Cut costs",
                                    "Are any category budgets over limit?" to "Budget alert",
                                    "Analyze supplier performance & pricing" to "Suppliers"
                                )
                            }
                            prompts.forEach { pair ->
                                SuggestionChip(
                                    label = pair.second,
                                    onClick = { onSendMessage(pair.first) }
                                )
                            }
                        }
                    }
                }

                // Chat Messages
                items(messages) { msg ->
                    ChatBubbleItem(message = msg, lang = lang)
                }

                // Analyzing indicator loading state
                if (isAnalyzing) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (lang == "ar") Alignment.CenterStart else Alignment.CenterEnd
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = Loc.get("analyzing", lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Panel (Sticky at Bottom)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Actions (Voice & OCR trigger buttons)
                    IconButton(
                        onClick = { showScanDialog = true },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = Loc.get("ocr_receipt", lang),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { showVoiceDialog = true },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = Loc.get("voice_record", lang),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Text Input Field
                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = Loc.get("input_hint", lang),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    // Send Button
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput)
                                textInput = ""
                            }
                        },
                        enabled = textInput.isNotBlank(),
                        modifier = Modifier.background(
                            if (textInput.isNotBlank()) MaterialTheme.colorScheme.primary else Color.LightGray,
                            CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = Loc.get("send", lang),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // --- Dialogs & Drawers ---

        // 1. AI OCR Scanned Receipt Confirmation Card
        if (extractedResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .testTag("extracted_expenses_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = null,
                                tint = Color(0xFFE07A5F)
                            )
                            Text(
                                text = Loc.get("ai_confirmation", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = Loc.get("ai_extracted_msg", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Scrollable List of Extracted items
                        LazyColumn(
                            modifier = Modifier
                                .heightIn(max = 220.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(extractedResult) { ext ->
                                ExtractedItemRow(ext = ext, currency = currency, lang = lang)
                            }
                        }

                        if (!errorState.isNullOrBlank()) {
                            Text(
                                text = errorState,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Bottom Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onApproveExtracted,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = Loc.get("approve", lang), color = Color.White)
                            }
                            OutlinedButton(
                                onClick = onDiscardExtracted,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = Loc.get("discard", lang))
                            }
                        }
                    }
                }
            }
        }

        // 2. OCR Scan Drawer (Preloaded Sample Receipts)
        if (showScanDialog) {
            AlertDialog(
                onDismissRequest = { showScanDialog = false },
                title = {
                    Text(
                        text = if (lang == "ar") "امسح فاتورة ضريبية للتجربة" else "Select Invoice to scan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (lang == "ar") "اختر فاتورة محاكاة لتجربة ميزة استخلاص الحقول الضريبية عبر الذكاء الاصطناعي (OCR):" else "Select a preloaded mock invoice to test real AI OCR extraction:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        SampleReceiptButton(
                            title = if (lang == "ar") "فاتورة توصيل الرياض للخدمات (280 ر.س)" else "Logistics Delivery invoice (280 SAR)",
                            supplier = "شركة الرياض للخدمات اللوجستية",
                            details = "توصيل بضائع طارئة للمخازن",
                            amount = 280.0,
                            onClick = {
                                showScanDialog = false
                                // Create a mock receipt bitmap
                                val bitmap = createMockReceiptBitmap(
                                    title = "فاتورة شركة الرياض اللوجستية",
                                    amount = "280.00 SAR",
                                    details = "توصيل شحنة رقم 549320\nالضريبة المتضمنة: 36.52 SAR\nالمورد: شركة الرياض للخدمات اللوجستية"
                                )
                                onSendImage(bitmap)
                            }
                        )

                        SampleReceiptButton(
                            title = if (lang == "ar") "شراء رفوف جدارية (12,500 ر.س)" else "Racks & Decor Invoice (12,500 SAR)",
                            supplier = "أحمد الحربي للديكور",
                            details = "توريد وتركيب رفوف عرض حديدية",
                            amount = 12500.0,
                            onClick = {
                                showScanDialog = false
                                val bitmap = createMockReceiptBitmap(
                                    title = "أحمد الحربي للديكور والتصميم",
                                    amount = "12,500.00 SAR",
                                    details = "توريد رفوف حديد مجلفن بالمعرض\nالضريبة (15%): 1,630.43 SAR\nطريقة الدفع: تحويل بنكي"
                                )
                                onSendImage(bitmap)
                            }
                        )

                        SampleReceiptButton(
                            title = if (lang == "ar") "أنظمة نقاط البيع الحديثة (8,900 ر.س)" else "Cashier System POS (8,900 SAR)",
                            supplier = "شركة التقنية المتقدمة",
                            details = "جهازي كاشير متكاملين بالملحقات",
                            amount = 8900.0,
                            onClick = {
                                showScanDialog = false
                                val bitmap = createMockReceiptBitmap(
                                    title = "شركة التقنية المتقدمة للحلول الرقمية",
                                    amount = "8,900.00 SAR",
                                    details = "شراء نظام كاشير POS باللمس\nملحقات: طابعات فواتير باركود\nشاملاً ضريبة القيمة المضافة 15%"
                                )
                                onSendImage(bitmap)
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showScanDialog = false }) {
                        Text(text = Loc.get("discard", lang))
                    }
                }
            )
        }

        // 3. Voice Record Dialog Simulation
        if (showVoiceDialog) {
            AlertDialog(
                onDismissRequest = { showVoiceDialog = false },
                title = {
                    Text(
                        text = Loc.get("voice_recording", lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Pulsing microphone animation
                        val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = 1.4f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .padding(scale.dp * 8),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Text(
                            text = if (lang == "ar") "اختر عبارة مسجلة مسبقاً لتجربة المحاكاة الصوتية:" else "Select a pre-transcribed sentence to simulate natural voice speech:",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )

                        val voiceTriggers = if (lang == "ar") {
                            listOf(
                                "شريت أرفف جديدة بـ 1200 ريال كاش من الحربي للديكور" to "أرفف (1200 ريال)",
                                "سددت فاتورة الكهرباء والإنترنت بقيمة 620 ريال بالبطاقة" to "إنترنت وكهرباء (620 ريال)",
                                "دفعت 15000 ريال إيجار المعرض الشهري تحويل بنكي" to "إيجار المعرض (15000 ريال)"
                            )
                        } else {
                            listOf(
                                "I bought some display racks for 1200 SAR cash from Ahmed Decor" to "Racks (1200 SAR)",
                                "Paid the internet and electric bill of 620 SAR by card" to "Internet (620 SAR)",
                                "Transferred 15000 SAR for the showroom rental costs" to "Rent (15000 SAR)"
                            )
                        }

                        voiceTriggers.forEach { trigger ->
                            OutlinedButton(
                                onClick = {
                                    showVoiceDialog = false
                                    onSendMessage(trigger.first)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = trigger.second, fontSize = 13.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVoiceDialog = false }) {
                        Text(text = Loc.get("discard", lang))
                    }
                }
            )
        }
    }
}

@Composable
fun SuggestionChip(label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ChatBubbleItem(message: AiMessage, lang: String) {
    val isModel = message.role == "model"
    val alignment = if (isModel) {
        if (lang == "ar") Alignment.CenterStart else Alignment.CenterEnd
    } else {
        if (lang == "ar") Alignment.CenterEnd else Alignment.CenterStart
    }
    val bubbleColor = if (isModel) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val contentColor = if (isModel) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isModel) Alignment.Start else Alignment.End
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isModel) 4.dp else 16.dp,
                    bottomEnd = if (isModel) 16.dp else 4.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bubbleColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        lineHeight = 20.sp
                    )
                }
            }
            Text(
                text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun ExtractedItemRow(ext: ExtractedExpense, currency: String, lang: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ext.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${ext.amount} $currency",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${Loc.get("category", lang)}: ${ext.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "${Loc.get("supplier", lang)}: ${ext.supplierName ?: "-"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Confidence: ${(ext.confidenceScore * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ext.confidenceScore > 0.7) Color(0xFF287D4B) else Color(0xFFC94A29)
                )
                Text(
                    text = "Method: ${ext.paymentMethod}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun SampleReceiptButton(
    title: String,
    supplier: String,
    details: String,
    amount: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = "$supplier - $details", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Generate realistic mock Invoice JPEGs for OCR testing
private fun createMockReceiptBitmap(title: String, amount: String, details: String): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // Draw background
    paint.color = android.graphics.Color.WHITE
    canvas.drawRect(0f, 0f, 400f, 300f, paint)

    // Draw invoice borders
    paint.color = android.graphics.Color.DKGRAY
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 4f
    canvas.drawRect(10f, 10f, 390f, 290f, paint)

    // Draw header
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 24f
    paint.isAntiAlias = true
    canvas.drawText("TAX INVOICE / فاتورة ضريبية", 30f, 50f, paint)

    paint.textSize = 20f
    canvas.drawText(title, 30f, 95f, paint)

    // Draw separator
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(30f, 120f, 370f, 120f, paint)

    // Draw details
    paint.color = android.graphics.Color.DKGRAY
    paint.textSize = 14f
    var y = 150f
    for (line in details.split("\n")) {
        canvas.drawText(line, 30f, y, paint)
        y += 24f
    }

    // Draw amount
    paint.color = android.graphics.Color.RED
    paint.textSize = 22f
    canvas.drawText("TOTAL: $amount", 30f, 260f, paint)

    return bitmap
}
