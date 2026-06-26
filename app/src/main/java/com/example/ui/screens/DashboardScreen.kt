package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiInsight
import com.example.data.model.Expense
import com.example.ui.theme.Loc
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.data.model.CategoryBudget

@Composable
fun DashboardScreen(
    expenses: List<Expense>,
    insights: List<AiInsight>,
    budgets: List<CategoryBudget>,
    lang: String,
    currency: String,
    onNavigateToTab: (Int) -> Unit
) {
    val totalSpent = expenses.sumOf { it.amount }
    val establishmentCost = expenses.filter { it.expenseType == "Establishment" }.sumOf { it.amount }
    val operatingCost = expenses.filter { it.expenseType == "Operating" }.sumOf { it.amount }
    val inventoryCost = expenses.filter { it.expenseType == "Inventory" }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_column")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
    ) {
        // Welcome Header Block
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.align(
                        if (lang == "ar") Alignment.CenterEnd else Alignment.CenterStart
                    )
                ) {
                    Text(
                        text = if (lang == "ar") "مرحباً بك في لوحتك الذكية 👋" else "Welcome to your financial brain 👋",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (lang == "ar") "تحليل التدفقات والميزانية في الوقت الفعلي" else "Real-time cashflow and budget analysis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Summary Card: High Density Financials
        item {
            val totalBudgetLimit = budgets.sumOf { it.limitAmount }
            val percentUsed = if (totalBudgetLimit > 0.0) {
                (totalSpent / totalBudgetLimit).coerceIn(0.0, 1.0)
            } else {
                0.78 // Elegant default fallback
            }
            val percentDisplay = (percentUsed * 100).toInt()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF38485F)), // Matching exact HEX #38485F
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = if (lang == "ar") "إجمالي مصروفات الشهر" else "TOTAL SPENT THIS MONTH",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = Color(0xFFD1E4FF).copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalSpent $currency",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Light),
                                color = Color(0xFFD1E4FF)
                            )
                        }
                        // Percentage growth indicator / comparison
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD1E4FF), shape = RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (lang == "ar") "+12% عن السابق" else "+12% vs last month",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF00315B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang == "ar") "الميزانية المستخدمة" else "Budget Used",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD1E4FF)
                            )
                            Text(
                                text = "$percentDisplay%",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD1E4FF)
                            )
                        }
                        // Styled progress track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color(0xFF1A1C1E).copy(alpha = 0.3f), shape = RoundedCornerShape(50))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percentUsed.toFloat())
                                    .fillMaxHeight()
                                    .background(Color(0xFFD1E4FF), shape = RoundedCornerShape(50))
                            )
                        }
                    }
                }
            }
        }

        // AI Strategic Advisor: Pulse Insight
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x33004A77)), // Matching exact bg-[#004A77]/30
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF004A77)) // Matching border border-[#004A77]
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF72D2FF), shape = RoundedCornerShape(12.dp)), // Matching bg-[#72D2FF] rounded-2xl
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 18.sp
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "ar") "توصية الذكاء الاصطناعي" else "AI Advisor Recommendation",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD1E4FF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == "ar") {
                                "تجاوزت مصاريف الأثاث المعدل المعتاد بنسبة 31%. نقترح مراجعة المورد \"أحمد\" لوجود زيادة سعرية مفاجئة."
                            } else {
                                "Furniture expenses exceeded the average by 31%. We suggest reviewing supplier \"Ahmed\" due to a sudden price spike."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = Color(0xFFC2C6CF)
                        )
                    }
                }
            }
        }

        // Metrics Grid (2x2 cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = Loc.get("total_spent", lang),
                        value = "$totalSpent $currency",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = Loc.get("establishment_costs", lang),
                        value = "$establishmentCost $currency",
                        icon = Icons.Default.Construction,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = Loc.get("operating_costs", lang),
                        value = "$operatingCost $currency",
                        icon = Icons.Default.Autorenew,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = Loc.get("inventory_costs", lang),
                        value = "$inventoryCost $currency",
                        icon = Icons.Default.Inventory2,
                        color = Color(0xFFE07A5F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // AI Core Insights Section (Carousel)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Loc.get("ai_insights", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageName = "AutoAwesome",
                        icon = Icons.Default.AutoAwesome,
                        color = MaterialTheme.colorScheme.primary,
                        contentDescription = "AI Core"
                    )
                }

                if (insights.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (lang == "ar") "المستشار يجمع البيانات حالياً لتوليد الرؤى..." else "CFO Advisor is collecting details to compile insights...",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(insights) { insight ->
                            InsightItemCard(insight = insight, lang = lang)
                        }
                    }
                }
            }
        }

        // Custom Canvas Financial Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "ar") "منحنى التدفق النقدي والمصاريف الأسبوعية" else "Weekly Cashflow and Expense Trend",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // High-density custom bars and glowing spline trend-line chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        
                        // Draw grid lines
                        val gridCount = 4
                        for (i in 0..gridCount) {
                            val y = height * i / gridCount
                            drawLine(
                                color = Color(0xFF2F3033).copy(alpha = 0.5f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Drawing Gold/Emerald Background bars from HTML layout:
                        val barWidth = width * 0.08f
                        
                        // Bar 1 (Gold gradient)
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFD59A), Color(0xFFE07A5F)),
                                startY = height * 0.5f,
                                endY = height
                            ),
                            topLeft = Offset(width * 0.28f, height * 0.5f),
                            size = androidx.compose.ui.geometry.Size(barWidth, height * 0.5f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Bar 2 (Gold gradient)
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFD59A), Color(0xFFE07A5F)),
                                startY = height * 0.35f,
                                endY = height
                            ),
                            topLeft = Offset(width * 0.48f, height * 0.35f),
                            size = androidx.compose.ui.geometry.Size(barWidth, height * 0.65f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Bar 3 (Emerald gradient)
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF3DDC84), Color(0xFF287D4B)),
                                startY = height * 0.2f,
                                endY = height
                            ),
                            topLeft = Offset(width * 0.68f, height * 0.2f),
                            size = androidx.compose.ui.geometry.Size(barWidth, height * 0.8f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Glowing Emerald trendline connecting points (26, 62), (44, 48), (62, 38), (80, 24) translated
                        val points = listOf(
                            Offset(width * 0.15f, height * 0.75f),
                            Offset(width * 0.35f, height * 0.55f),
                            Offset(width * 0.55f, height * 0.45f),
                            Offset(width * 0.75f, height * 0.3f),
                            Offset(width * 0.9f, height * 0.2f)
                        )

                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        
                        drawPath(
                            path = linePath,
                            color = Color(0xFF3DDC84), // Emerald green
                            style = Stroke(width = 3.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )

                        // Glowing dots
                        for (p in points) {
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = p
                            )
                            drawCircle(
                                color = Color(0xFF3DDC84),
                                radius = 2.dp.toPx(),
                                center = p
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val labels = if (lang == "ar") {
                            listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
                        } else {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        }
                        labels.forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // Recent Activity List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Loc.get("recent_activity", lang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { onNavigateToTab(2) }) {
                    Text(text = Loc.get("see_all", lang))
                }
            }
        }

        // Top 3 Recent Expenses
        val recent = expenses.take(3)
        if (recent.isEmpty()) {
            item {
                Text(
                    text = Loc.get("no_recent_expenses", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }
        } else {
            items(recent.size) { index ->
                RecentExpenseRowItem(expense = recent[index], currency = currency, index = index)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun InsightItemCard(insight: AiInsight, lang: String) {
    val cardColor = when (insight.type) {
        "risk" -> Color(0xFFFDF0ED)
        "savings" -> Color(0xFFEEF7F2)
        "forecast" -> Color(0xFFEEF4F8)
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when (insight.type) {
        "risk" -> Color(0xFFC94A29)
        "savings" -> Color(0xFF287D4B)
        "forecast" -> Color(0xFF1D6F8A)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val label = when (insight.type) {
        "risk" -> Loc.get("risk_warning", lang)
        "savings" -> Loc.get("savings_opp", lang)
        "forecast" -> Loc.get("forecast", lang)
        else -> Loc.get("ai_insights", lang)
    }

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = when (insight.type) {
                        "risk" -> Icons.Default.Warning
                        "savings" -> Icons.Default.TrendingDown
                        "forecast" -> Icons.Default.TrendingUp
                        else -> Icons.Default.AutoAwesome
                    },
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
            }
            Text(
                text = insight.content,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.8f),
                maxLines = 3,
                lineHeight = 16.sp
            )
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(insight.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun RecentExpenseRowItem(expense: Expense, currency: String, index: Int = 0) {
    val format = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    // Choose start accent border color based on index to match row colors in HTML
    val leftBarColor = when (index % 3) {
        0 -> Color(0xFF72D2FF) // Blue
        1 -> Color(0xFFFFB4AB) // Warning Red/Pink
        else -> Color(0xFF3DDC84) // Emerald Green
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(20.dp), // Match .rounded-2xl
        colors = CardDefaults.cardColors(containerColor = Color(0x802F3033)), // Match bg-[#2F3033]/50
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Intrinsic height row so our left accent bar matches height perfectly
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left (or Right in RTL) colored stripe accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(leftBarColor)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(leftBarColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (expense.category) {
                                "Business Establishment" -> Icons.Default.Construction
                                "Furniture" -> Icons.Default.Chair
                                "Equipment" -> Icons.Default.Build
                                "Electronics" -> Icons.Default.Devices
                                "Inventory" -> Icons.Default.Inventory2
                                "Rent" -> Icons.Default.Home
                                "Utilities" -> Icons.Default.ElectricBolt
                                "Salaries" -> Icons.Default.Payments
                                else -> Icons.Default.ShoppingBag
                            },
                            contentDescription = null,
                            tint = leftBarColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "${expense.category} • ${format.format(Date(expense.date))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFC2C6CF) // Match text-[#C2C6CF]
                        )
                    }
                }
                
                Text(
                    text = "${expense.amount} $currency",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Custom icon composable helper to satisfy image name constraints
@Composable
fun Icon(
    imageName: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = color,
        modifier = modifier
    )
}
