package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.ui.theme.Loc
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    expenses: List<Expense>,
    lang: String,
    currency: String,
    onAddExpense: (Expense) -> Unit,
    onDeleteExpense: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Dialog state fields
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Miscellaneous") }
    var supplierName by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var expenseType by remember { mutableStateOf("Operating") }
    var notes by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringInterval by remember { mutableStateOf("Monthly") }

    val categories = listOf(
        "All", "Business Establishment", "Furniture", "Equipment",
        "Electronics", "Inventory", "Rent", "Utilities", "Salaries", "Marketing", "Miscellaneous"
    )

    // Filtered list
    val filteredExpenses = expenses.filter { exp ->
        val matchesSearch = exp.title.contains(searchQuery, ignoreCase = true) ||
                (exp.notes?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (exp.supplierName?.contains(searchQuery, ignoreCase = true) ?: false)
        
        val matchesCategory = selectedCategoryFilter == "All" || exp.category.equals(selectedCategoryFilter, ignoreCase = true)
        
        matchesSearch && matchesCategory
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar & Export Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = Loc.get("search_placeholder", lang), fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("expense_search_input"),
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Export Button
                IconButton(
                    onClick = {
                        Toast.makeText(context, Loc.get("export_success", context.getString(
                            if (lang == "ar") com.example.R.string.app_name else com.example.R.string.app_name
                        )), Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = Loc.get("export", lang),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Categories horizontal filter list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == "ar") "التصنيف:" else "Category:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(modifier = Modifier.weight(1f)) {
                    // Small scrollable filter tag list
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            val label = if (cat == "All") Loc.get("all", lang) else cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(text = label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Expense Records Lazy Column List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("expenses_list_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (filteredExpenses.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (lang == "ar") "لم نعثر على أي مصاريف مطابقة للتصفية." else "No matching expenses found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredExpenses) { expense ->
                        ExpenseRecordRowItem(
                            expense = expense,
                            currency = currency,
                            lang = lang,
                            onDelete = { onDeleteExpense(expense.id) }
                        )
                    }
                }
            }
        }

        // Add Expense FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_expense_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = Loc.get("add_expense", lang))
        }

        // Add Manual Expense Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        text = Loc.get("add_expense", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    // Form fields inside a scrollable column
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text(text = Loc.get("expense_title", lang)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                label = { Text(text = Loc.get("amount", lang)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            // Category Dropdown simulated
                            Text(text = Loc.get("category", lang), style = MaterialTheme.typography.labelSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val dropdownCategories = categories.filter { it != "All" }
                                var expanded by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { expanded = !expanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = category, fontSize = 12.sp)
                                }
                                // Small selection items
                                if (expanded) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(6.dp)
                                    ) {
                                        Column {
                                            dropdownCategories.forEach { cat ->
                                                Text(
                                                    text = cat,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            category = cat
                                                            expanded = false
                                                        }
                                                        .padding(6.dp),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = supplierName,
                                onValueChange = { supplierName = it },
                                label = { Text(text = Loc.get("supplier", lang) + " (Optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            // Payment method row
                            Text(text = Loc.get("payment_method", lang), style = MaterialTheme.typography.labelSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Cash", "Card", "Bank Transfer").forEach { method ->
                                    val isSelected = paymentMethod == method
                                    ElevatedButton(
                                        onClick = { paymentMethod = method },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = method, fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        item {
                            // Expense type row
                            Text(text = Loc.get("expense_type", lang), style = MaterialTheme.typography.labelSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Operating", "Establishment", "Inventory").forEach { type ->
                                    val isSelected = expenseType == type
                                    ElevatedButton(
                                        onClick = { expenseType = type },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = type, fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text(text = Loc.get("notes", lang)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            // Recurring checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                                Text(text = Loc.get("is_recurring", lang), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && parsedAmount > 0) {
                                onAddExpense(
                                    Expense(
                                        title = title,
                                        amount = parsedAmount,
                                        currency = currency,
                                        category = category,
                                        supplierName = if (supplierName.isBlank()) null else supplierName,
                                        paymentMethod = paymentMethod,
                                        expenseType = expenseType,
                                        notes = if (notes.isBlank()) null else notes,
                                        isRecurring = isRecurring,
                                        recurringInterval = if (isRecurring) recurringInterval else null
                                    )
                                )
                                // Clear form & close
                                title = ""
                                amount = ""
                                category = "Miscellaneous"
                                supplierName = ""
                                paymentMethod = "Cash"
                                expenseType = "Operating"
                                notes = ""
                                isRecurring = false
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text(text = Loc.get("save", lang))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(text = Loc.get("discard", lang))
                    }
                }
            )
        }
    }
}

@Composable
fun ExpenseRecordRowItem(
    expense: Expense,
    currency: String,
    lang: String,
    onDelete: () -> Unit
) {
    val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    var showMenu by remember { mutableStateOf(false) }

    val leftBarColor = when (expense.expenseType) {
        "Establishment" -> Color(0xFF72D2FF) // Blue
        "Operating" -> Color(0xFFFFB4AB) // Warning Red/Pink
        "Inventory" -> Color(0xFF3DDC84) // Emerald Green
        else -> Color(0xFFD1E4FF)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp), // Match .rounded-2xl
        colors = CardDefaults.cardColors(containerColor = Color(0x802F3033)), // Match bg-[#2F3033]/50
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left stripe color accent
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(leftBarColor)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(leftBarColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
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
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }

                    // Delete Action Trigger Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(text = Loc.get("delete", lang), color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${expense.category} | ${expense.paymentMethod} | ${Loc.get("supplier", lang)}: ${expense.supplierName ?: "-"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFC2C6CF)
                    )
                    Text(
                        text = "${expense.amount} $currency",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = leftBarColor
                    )
                }

                if (!expense.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = expense.notes,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Loc.get("tax_pct", lang)}: ${expense.taxPercentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (expense.isRecurring) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                tint = Color(0xFF3DDC84),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = Loc.get("is_recurring", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF3DDC84)
                            )
                        }
                        Text(
                            text = format.format(Date(expense.date)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
