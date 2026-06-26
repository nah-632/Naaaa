package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ExtractedExpense
import com.example.data.api.GeminiClient
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(db)

    // Exposed Flows
    val expenses = repository.expenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val suppliers = repository.suppliers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val budgets = repository.budgets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val aiMessages = repository.aiMessages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val aiInsights = repository.aiInsights.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings = repository.settingsFlow.map { it ?: AppSettings() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    // UI Interactive States
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _extractedExpensesResult = MutableStateFlow<List<ExtractedExpense>?>(null)
    val extractedExpensesResult: StateFlow<List<ExtractedExpense>?> = _extractedExpensesResult.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                // Prepopulate realistic data if empty
                repository.prepopulateSeedData()
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "Error prepopulating data: ${e.message}", e)
            }
        }
    }

    // --- Actions ---

    fun insertExpense(expense: Expense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
        }
    }

    fun addSupplier(name: String, phone: String? = null, notes: String? = null) {
        viewModelScope.launch {
            repository.insertSupplier(Supplier(name = name, phone = phone, notes = notes))
        }
    }

    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertBudget(CategoryBudget(category = category, limitAmount = limit))
        }
    }

    fun updateSettings(language: String, currency: String, storeName: String) {
        viewModelScope.launch {
            repository.updateSettings(AppSettings(id = 1, language = language, currency = currency, storeName = storeName))
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            repository.clearConversation()
        }
    }

    fun clearInsights() {
        viewModelScope.launch {
            repository.clearInsights()
        }
    }

    // --- AI Pipeline ---

    /**
     * Parse natural text expense (written or transcribed from voice)
     */
    fun processNaturalTextExpense(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorState.value = null
            
            // Add user's text to conversation memory
            repository.insertMessage(AiMessage(role = "user", text = text))

            try {
                val results = GeminiClient.extractExpenses(text)
                if (results.isNotEmpty()) {
                    _extractedExpensesResult.value = results
                } else {
                    _errorState.value = "لم نتمكن من استخلاص المصاريف من النص، يرجى كتابتها بطريقة أوضح. / Could not extract expenses."
                    repository.insertMessage(AiMessage(
                        role = "model",
                        text = "عذراً، لم أتمكن من استخلاص تفاصيل المصروف بدقة. هل يمكنك إدخاله يدوياً أو التحدث بشكل أوضح؟ 🔍"
                    ))
                }
            } catch (e: Exception) {
                _errorState.value = "حدث خطأ أثناء معالجة النص: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Process scanned receipt image bitmap
     */
    fun processReceiptImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorState.value = null

            // Log image message
            repository.insertMessage(AiMessage(
                role = "user",
                text = "تحليل صورة فاتورة أو منتج / Analysing receipt photo",
                attachmentType = "receipt"
            ))

            try {
                val results = GeminiClient.extractExpenses("", bitmap)
                if (results.isNotEmpty()) {
                    _extractedExpensesResult.value = results
                } else {
                    _errorState.value = "لم نتمكن من قراءة الفاتورة بوضوح. يرجى التأكد من جودة الصورة أو إدخالها يدوياً."
                    repository.insertMessage(AiMessage(
                        role = "model",
                        text = "عذراً، لم أتمكن من استخلاص معلومات الفاتورة من الصورة بوضوح. يرجى التأكد من الإضاءة وجودة الكلمات وإعادة المحاولة. 📸"
                    ))
                }
            } catch (e: Exception) {
                _errorState.value = "خطأ معالجة الفاتورة: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Approve and persist extracted expenses
     */
    fun approveExtractedExpenses() {
        val extracted = _extractedExpensesResult.value ?: return
        viewModelScope.launch {
            for (ext in extracted) {
                repository.insertExpense(
                    Expense(
                        title = ext.title,
                        amount = ext.amount,
                        currency = settings.value.currency,
                        date = System.currentTimeMillis(),
                        category = ext.category,
                        supplierName = ext.supplierName,
                        paymentMethod = ext.paymentMethod,
                        expenseType = ext.expenseType,
                        taxPercentage = ext.taxPercentage,
                        confidenceScore = ext.confidenceScore,
                        notes = ext.notes,
                        isRecurring = ext.isRecurring,
                        recurringInterval = ext.recurringInterval
                    )
                )
            }
            
            // Build a confirmation chatbot response
            val bulletPoints = extracted.joinToString("\n") { ext ->
                "• **${ext.title}**: ${ext.amount} ${settings.value.currency} (${ext.category}) [المورد: ${ext.supplierName ?: "غير محدد"}]"
            }
            val replyText = "تم بنجاح تأكيد وحفظ المصاريف المستخلصة في ميزانيتك:\n$bulletPoints\n\nتم تحديث لوحة التحكم والتحليلات فوراً! 📊🚀"
            repository.insertMessage(AiMessage(role = "model", text = replyText))

            _extractedExpensesResult.value = null
        }
    }

    fun discardExtractedExpenses() {
        _extractedExpensesResult.value = null
        viewModelScope.launch {
            repository.insertMessage(AiMessage(role = "model", text = "تم إلغاء المصاريف المستخلصة بطلب منك. يمكنك المحاولة مجدداً أو الإدخال اليدوي. 🤝"))
        }
    }

    /**
     * Request Strategic Advisor Response
     */
    fun getStrategicFinancialAdvice(userPrompt: String) {
        if (userPrompt.isBlank()) return
        viewModelScope.launch {
            _isAnalyzing.value = true
            
            // Log user message
            repository.insertMessage(AiMessage(role = "user", text = userPrompt))

            try {
                val expenseSnapshot = getFinancialSnapshot()
                val chatHistory = aiMessages.value.takeLast(6).map { it.role to it.text }
                
                val response = GeminiClient.getStrategicAdvice(chatHistory, expenseSnapshot)
                repository.insertMessage(AiMessage(role = "model", text = response))

                // Periodically add an insight if strategic advice suggests warnings
                if (response.contains("تحذير") || response.contains("تنبيه")) {
                    repository.insertInsight(
                        AiInsight(
                            title = "توصية CFO عاجلة",
                            content = response.take(150) + "...",
                            type = "risk"
                        )
                    )
                }
            } catch (e: Exception) {
                repository.insertMessage(AiMessage(role = "model", text = "عذراً، حدث خطأ أثناء الاتصال بمستشارك المالي الذكي: ${e.message}"))
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Builds a structured text snapshot of the financial database for Gemini context
     */
    private fun getFinancialSnapshot(): String {
        val allExpenses = expenses.value
        val allBudgets = budgets.value
        val allSuppliers = suppliers.value

        val totalSpent = allExpenses.sumOf { it.amount }
        val establishmentCost = allExpenses.filter { it.expenseType == "Establishment" }.sumOf { it.amount }
        val operatingCost = allExpenses.filter { it.expenseType == "Operating" }.sumOf { it.amount }
        val inventoryCost = allExpenses.filter { it.expenseType == "Inventory" }.sumOf { it.amount }

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastExpensesText = allExpenses.take(5).joinToString("\n") { 
            "- ${it.title}: ${it.amount} ${it.currency} | التصنيف: ${it.category} | المورد: ${it.supplierName ?: "غير معروف"} | التاريخ: ${format.format(Date(it.date))}"
        }

        val budgetText = allBudgets.joinToString("\n") { 
            "- ${it.category}: ميزانية مخصصة ${it.limitAmount}، مستهلك منها ${it.spentAmount} | المتبقي ${it.limitAmount - it.spentAmount}"
        }

        val supplierText = allSuppliers.joinToString("\n") {
            "- ${it.name}: إجمالي المدفوع ${it.totalPaid}، المستحق ${it.pendingBalance}"
        }

        return """
            [بيانات المتجر المالية - Financial Database Snapshot]
            إجمالي الإنفاق: $totalSpent SAR
            تكاليف التأسيس: $establishmentCost SAR
            تكاليف التشغيل: $operatingCost SAR
            تكاليف المخزون: $inventoryCost SAR

            [الميزانيات المخصصة للتصنيفات]:
            $budgetText

            [حالة الموردين]:
            $supplierText

            [آخر 5 عمليات مسجلة]:
            $lastExpensesText
        """.trimIndent()
    }
}
