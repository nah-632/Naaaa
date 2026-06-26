package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.Loc
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent() {
    val viewModel: FinanceViewModel = viewModel()
    
    // Collecting StateFlows reactively and safely using collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val aiInsights by viewModel.aiInsights.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val extractedResult by viewModel.extractedExpensesResult.collectAsStateWithLifecycle()
    val errorState by viewModel.errorState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) }

    // Dynamic RTL/LTR layout direction depending on chosen language
    val layoutDirection = if (settings.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("main_app_scaffold"),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (settings.language == "ar") "م" else "M",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        // Title / Subtitle Info
                        Column {
                            Text(
                                text = settings.storeName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Loc.get("app_title", settings.language),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // Search & Language Actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Button
                        IconButton(
                            onClick = { currentTab = 1 }, // Navigates to Copilot
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surface, shape = androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Language Switcher Button
                        Button(
                            onClick = {
                                val nextLang = if (settings.language == "ar") "en" else "ar"
                                viewModel.updateSettings(nextLang, settings.currency, settings.storeName)
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .widthIn(min = 44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (settings.language == "ar") "EN" else "عربي",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), thickness = 1.dp)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            label = { Text(text = Loc.get("dashboard", settings.language), fontSize = 10.sp) },
                            icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = null) },
                            modifier = Modifier.testTag("tab_dashboard")
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            label = { Text(text = Loc.get("copilot", settings.language), fontSize = 10.sp) },
                            icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null) },
                            modifier = Modifier.testTag("tab_copilot")
                        )
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            label = { Text(text = Loc.get("expenses", settings.language), fontSize = 10.sp) },
                            icon = { Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null) },
                            modifier = Modifier.testTag("tab_expenses")
                        )
                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { currentTab = 3 },
                            label = { Text(text = Loc.get("suppliers_budgets", settings.language), fontSize = 10.sp) },
                            icon = { Icon(imageVector = Icons.Default.GroupWork, contentDescription = null) },
                            modifier = Modifier.testTag("tab_suppliers")
                        )
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { currentTab = 4 },
                            label = { Text(text = Loc.get("settings", settings.language), fontSize = 10.sp) },
                            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
                            modifier = Modifier.testTag("tab_settings")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> DashboardScreen(
                        expenses = expenses,
                        insights = aiInsights,
                        budgets = budgets,
                        lang = settings.language,
                        currency = settings.currency,
                        onNavigateToTab = { currentTab = it }
                    )
                    1 -> CopilotChatScreen(
                        messages = aiMessages,
                        isAnalyzing = isAnalyzing,
                        extractedResult = extractedResult,
                        errorState = errorState,
                        lang = settings.language,
                        currency = settings.currency,
                        onSendMessage = { prompt ->
                            // Check if strategic trigger or standard transaction
                            if (prompt.contains("كيف") || prompt.contains("هل") || prompt.contains("تحليل") ||
                                prompt.contains("How") || prompt.contains("Are") || prompt.contains("Analyze") ||
                                prompt.contains("تنبؤ") || prompt.contains("Forecast")
                            ) {
                                viewModel.getStrategicFinancialAdvice(prompt)
                            } else {
                                viewModel.processNaturalTextExpense(prompt)
                            }
                        },
                        onSendImage = { bitmap ->
                            viewModel.processReceiptImage(bitmap)
                        },
                        onApproveExtracted = {
                            viewModel.approveExtractedExpenses()
                        },
                        onDiscardExtracted = {
                            viewModel.discardExtractedExpenses()
                        }
                    )
                    2 -> ExpenseListScreen(
                        expenses = expenses,
                        lang = settings.language,
                        currency = settings.currency,
                        onAddExpense = { expense ->
                            viewModel.insertExpense(expense)
                        },
                        onDeleteExpense = { id ->
                            viewModel.deleteExpense(id)
                        }
                    )
                    3 -> SupplierScreen(
                        suppliers = suppliers,
                        budgets = budgets,
                        lang = settings.language,
                        currency = settings.currency,
                        onAddSupplier = { name, phone, notes ->
                            viewModel.addSupplier(name, phone, notes)
                        },
                        onAddBudget = { category, limit ->
                            viewModel.addBudget(category, limit)
                        }
                    )
                    4 -> SettingsScreen(
                        currentLanguage = settings.language,
                        currentCurrency = settings.currency,
                        storeName = settings.storeName,
                        onUpdateSettings = { language, currency, name ->
                            viewModel.updateSettings(language, currency, name)
                        }
                    )
                }
            }
        }
    }
}
