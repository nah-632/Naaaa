package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryBudget
import com.example.data.model.Supplier
import com.example.ui.theme.Loc

@Composable
fun SupplierScreen(
    suppliers: List<Supplier>,
    budgets: List<CategoryBudget>,
    lang: String,
    currency: String,
    onAddSupplier: (String, String?, String?) -> Unit,
    onAddBudget: (String, Double) -> Unit
) {
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }

    // Add Supplier dialog states
    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var supplierNotes by remember { mutableStateOf("") }

    // Add Budget dialog states
    var budgetCategory by remember { mutableStateOf("Inventory") }
    var budgetLimit by remember { mutableStateOf("") }

    val standardCategories = listOf(
        "Business Establishment", "Furniture", "Equipment", "Electronics",
        "Inventory", "Rent", "Utilities", "Salaries", "Marketing", "Miscellaneous"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("supplier_screen_column")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // --- 1. Suppliers Section ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Loc.get("supplier_analytics", lang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { showAddSupplierDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = Loc.get("add_supplier", lang), fontSize = 12.sp)
                }
            }
        }

        if (suppliers.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (lang == "ar") "لم تقم بإضافة موردين بعد." else "No suppliers registered yet.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(suppliers) { supplier ->
                SupplierRowItem(supplier = supplier, currency = currency, lang = lang)
            }
        }

        // --- 2. Budget Allocations Section ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Loc.get("category_limits", lang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { showAddBudgetDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = Loc.get("add_budget", lang), fontSize = 12.sp)
                }
            }
        }

        if (budgets.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (lang == "ar") "لا توجد ميزانيات أقسام نشطة." else "No category budgets allocated yet.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(budgets) { budget ->
                BudgetProgressRowItem(budget = budget, currency = currency, lang = lang)
            }
        }
    }

    // --- Dialogs ---

    // 1. Add Supplier Dialog
    if (showAddSupplierDialog) {
        AlertDialog(
            onDismissRequest = { showAddSupplierDialog = false },
            title = { Text(text = Loc.get("add_supplier", lang), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text(text = Loc.get("supplier_name", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = supplierPhone,
                        onValueChange = { supplierPhone = it },
                        label = { Text(text = Loc.get("supplier_phone", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = supplierNotes,
                        onValueChange = { supplierNotes = it },
                        label = { Text(text = Loc.get("notes", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (supplierName.isNotBlank()) {
                            onAddSupplier(
                                supplierName,
                                if (supplierPhone.isBlank()) null else supplierPhone,
                                if (supplierNotes.isBlank()) null else supplierNotes
                            )
                            supplierName = ""
                            supplierPhone = ""
                            supplierNotes = ""
                            showAddSupplierDialog = false
                        }
                    }
                ) {
                    Text(text = Loc.get("save", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSupplierDialog = false }) {
                    Text(text = Loc.get("discard", lang))
                }
            }
        )
    }

    // 2. Add Budget Dialog
    if (showAddBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showAddBudgetDialog = false },
            title = { Text(text = Loc.get("add_budget", lang), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = Loc.get("category", lang), style = MaterialTheme.typography.labelSmall)
                    standardCategories.forEach { cat ->
                        val isSelected = budgetCategory == cat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { budgetCategory = cat }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { budgetCategory = cat })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = cat, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = budgetLimit,
                        onValueChange = { budgetLimit = it },
                        label = { Text(text = Loc.get("limit_amount", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedLimit = budgetLimit.toDoubleOrNull() ?: 0.0
                        if (parsedLimit > 0) {
                            onAddBudget(budgetCategory, parsedLimit)
                            budgetLimit = ""
                            showAddBudgetDialog = false
                        }
                    }
                ) {
                    Text(text = Loc.get("save", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBudgetDialog = false }) {
                    Text(text = Loc.get("discard", lang))
                }
            }
        )
    }
}

@Composable
fun SupplierRowItem(supplier: Supplier, currency: String, lang: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supplier.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                supplier.phone?.let {
                    Text(text = it, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                if (!supplier.notes.isNullOrBlank()) {
                    Text(text = supplier.notes, style = MaterialTheme.typography.bodySmall, color = Color.Gray.copy(alpha = 0.8f))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${Loc.get("total_paid", lang)}: ${supplier.totalPaid} $currency",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${Loc.get("pending_balance", lang)}: ${supplier.pendingBalance} $currency",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (supplier.pendingBalance > 0) Color(0xFFC94A29) else Color.Gray
                )
            }
        }
    }
}

@Composable
fun BudgetProgressRowItem(budget: CategoryBudget, currency: String, lang: String) {
    val progress = if (budget.limitAmount > 0) (budget.spentAmount / budget.limitAmount).toFloat() else 0.0f
    val color = when {
        progress >= 1.0f -> Color(0xFFC94A29) // Red (Over Budget)
        progress >= 0.7f -> Color(0xFFD35400) // Orange (Warning)
        else -> Color(0xFF287D4B) // Green (Safe)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = budget.category,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${budget.spentAmount} / ${budget.limitAmount} $currency",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                color = color,
                trackColor = color.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.Transparent, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% " + if (progress >= 1.0f) "تجاوز الميزانية! / Over Budget!" else "مستهلك / Spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
                Text(
                    text = "${Loc.get("remaining_budget", lang)}: ${budget.limitAmount - budget.spentAmount} $currency",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
