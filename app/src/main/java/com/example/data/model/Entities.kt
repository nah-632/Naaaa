package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val currency: String = "SAR",
    val date: Long = System.currentTimeMillis(),
    val category: String = "Miscellaneous",
    val supplierName: String? = null,
    val paymentMethod: String = "Cash",
    val expenseType: String = "Operating", // "Establishment", "Operating", "Inventory", "Recurring"
    val taxPercentage: Double = 15.0, // 15% VAT default
    val confidenceScore: Double = 1.0,
    val notes: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // "Monthly", "Weekly", "Daily"
    val attachmentPath: String? = null
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val totalPaid: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val notes: String? = null
)

@Entity(tableName = "budgets")
data class CategoryBudget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val limitAmount: Double,
    val spentAmount: Double = 0.0,
    val monthYear: String = "2026-06"
)

@Entity(tableName = "ai_messages")
data class AiMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentType: String? = null, // "voice", "receipt", "product"
    val attachmentPath: String? = null
)

@Entity(tableName = "ai_insights")
data class AiInsight(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val type: String, // "savings", "risk", "forecast", "general"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val language: String = "ar", // "ar" (Arabic) or "en" (English)
    val currency: String = "SAR", // "SAR" or "USD"
    val storeName: String = "المتجر الذكي",
    val securityLockEnabled: Boolean = false
)
