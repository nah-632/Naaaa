package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class FinanceRepository(private val db: AppDatabase) {
    val expenses: Flow<List<Expense>> = db.expenseDao().getAllExpenses()
    val suppliers: Flow<List<Supplier>> = db.supplierDao().getAllSuppliers()
    val budgets: Flow<List<CategoryBudget>> = db.budgetDao().getAllBudgets()
    val aiMessages: Flow<List<AiMessage>> = db.aiMessageDao().getAllMessages()
    val aiInsights: Flow<List<AiInsight>> = db.aiInsightDao().getAllInsights()
    val settingsFlow: Flow<AppSettings?> = db.settingsDao().getSettingsFlow()

    // Expense Methods
    suspend fun insertExpense(expense: Expense): Long {
        val result = db.expenseDao().insertExpense(expense)
        // Also update budget spent and supplier paid
        updateBudgetForExpense(expense)
        updateSupplierForExpense(expense)
        return result
    }

    suspend fun deleteExpenseById(id: Int) {
        val expense = db.expenseDao().getExpenseById(id)
        if (expense != null) {
            db.expenseDao().deleteExpenseById(id)
            // Recalculate budgets and supplier stats
            recalculateFinances()
        }
    }

    // Supplier Methods
    suspend fun insertSupplier(supplier: Supplier): Long = db.supplierDao().insertSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = db.supplierDao().deleteSupplier(supplier)

    // Budget Methods
    suspend fun insertBudget(budget: CategoryBudget): Long = db.budgetDao().insertBudget(budget)
    suspend fun deleteBudget(budget: CategoryBudget) = db.budgetDao().deleteBudget(budget)

    // AI Messages Methods
    suspend fun insertMessage(message: AiMessage): Long = db.aiMessageDao().insertMessage(message)
    suspend fun clearConversation() = db.aiMessageDao().clearConversation()

    // AI Insights Methods
    suspend fun insertInsight(insight: AiInsight): Long = db.aiInsightDao().insertInsight(insight)
    suspend fun clearInsights() = db.aiInsightDao().clearInsights()

    // Settings Methods
    suspend fun getSettings(): AppSettings {
        var settings = db.settingsDao().getSettings()
        if (settings == null) {
            settings = AppSettings()
            db.settingsDao().insertSettings(settings)
        }
        return settings
    }

    suspend fun updateSettings(settings: AppSettings) {
        db.settingsDao().insertSettings(settings)
    }

    // Helper functions to keep budget and supplier synced
    private suspend fun updateBudgetForExpense(expense: Expense) {
        val allBudgets = db.budgetDao().getAllBudgets().firstOrNull() ?: emptyList()
        val match = allBudgets.find { it.category.equals(expense.category, ignoreCase = true) }
        if (match != null) {
            db.budgetDao().insertBudget(
                match.copy(spentAmount = match.spentAmount + expense.amount)
            )
        } else {
            // Auto create standard budget if not exists
            db.budgetDao().insertBudget(
                CategoryBudget(
                    category = expense.category,
                    limitAmount = expense.amount * 3, // auto allocate limit 3x expense
                    spentAmount = expense.amount
                )
            )
        }
    }

    private suspend fun updateSupplierForExpense(expense: Expense) {
        expense.supplierName?.let { supplierName ->
            val match = db.supplierDao().getSupplierByName(supplierName)
            if (match != null) {
                db.supplierDao().insertSupplier(
                    match.copy(totalPaid = match.totalPaid + expense.amount)
                )
            } else {
                // Auto create supplier
                db.supplierDao().insertSupplier(
                    Supplier(
                        name = supplierName,
                        totalPaid = expense.amount,
                        pendingBalance = 0.0,
                        notes = "تم إنشاؤه تلقائياً من الفواتير / Auto-created from invoice"
                    )
                )
            }
        }
    }

    private suspend fun recalculateFinances() {
        val allExpenses = db.expenseDao().getAllExpenses().firstOrNull() ?: emptyList()
        val allBudgets = db.budgetDao().getAllBudgets().firstOrNull() ?: emptyList()
        val allSuppliers = db.supplierDao().getAllSuppliers().firstOrNull() ?: emptyList()

        // Reset spent amounts in budgets
        for (budget in allBudgets) {
            val spent = allExpenses.filter { it.category.equals(budget.category, ignoreCase = true) }.sumOf { it.amount }
            db.budgetDao().insertBudget(budget.copy(spentAmount = spent))
        }

        // Reset paid amounts for suppliers
        for (supplier in allSuppliers) {
            val paid = allExpenses.filter { it.supplierName?.equals(supplier.name, ignoreCase = true) == true }.sumOf { it.amount }
            db.supplierDao().insertSupplier(supplier.copy(totalPaid = paid))
        }
    }

    // Prepopulate realistic data on first launch
    suspend fun prepopulateSeedData() {
        // Only run if empty
        val currentExpenses = db.expenseDao().getAllExpenses().firstOrNull() ?: emptyList()
        if (currentExpenses.isNotEmpty()) return

        // Insert Default Settings if not present
        if (db.settingsDao().getSettings() == null) {
            db.settingsDao().insertSettings(AppSettings())
        }

        // 1. Insert Core Suppliers
        val defaultSuppliers = listOf(
            Supplier(name = "أحمد الحربي للديكور (Ahmed Decor)", phone = "0501234567", notes = "المورد الرئيسي لأعمال الديكور والتجهيز"),
            Supplier(name = "شركة التقنية المتقدمة (Advanced Tech)", phone = "0549876543", notes = "توريد أجهزة الكمبيوتر والكاشير والإنترنت"),
            Supplier(name = "مستودعات النخبة للمخزون (Elite Warehouse)", phone = "0561112223", notes = "المورد الأساسي للمنتجات والبضائع للتجزئة"),
            Supplier(name = "شركة الرياض للخدمات اللوجستية (Riyadh Logistics)", phone = "0559998887", notes = "شركة التوصيل والشحن السريع")
        )
        for (s in defaultSuppliers) {
            db.supplierDao().insertSupplier(s)
        }

        // 2. Insert Core Category Budgets (monthly)
        val defaultBudgets = listOf(
            CategoryBudget(category = "Business Establishment", limitAmount = 60000.0, monthYear = "2026-06"),
            CategoryBudget(category = "Furniture", limitAmount = 15000.0, monthYear = "2026-06"),
            CategoryBudget(category = "Equipment", limitAmount = 25000.0, monthYear = "2026-06"),
            CategoryBudget(category = "Electronics", limitAmount = 20000.0, monthYear = "2026-06"),
            CategoryBudget(category = "Inventory", limitAmount = 80000.0, monthYear = "2026-06"),
            CategoryBudget(category = "Rent", limitAmount = 40000.0, monthYear = "2026-06"),
            CategoryBudget(category = "Utilities", limitAmount = 8000.0, monthYear = "2026-06"),
            CategoryBudget(category = "Salaries", limitAmount = 30000.0, monthYear = "2026-06"),
            CategoryBudget(category = "Marketing", limitAmount = 12000.0, monthYear = "2026-06"),
            CategoryBudget(category = "Miscellaneous", limitAmount = 5000.0, monthYear = "2026-06")
        )
        for (b in defaultBudgets) {
            db.budgetDao().insertBudget(b)
        }

        // 3. Insert Initial Expenses
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Establishment Expense 1: Decor & Shelves
        calendar.add(Calendar.DAY_OF_YEAR, -15)
        db.expenseDao().insertExpense(Expense(
            title = "تركيب رفوف حديدية ووحدات عرض جدارية",
            amount = 12500.0,
            category = "Business Establishment",
            supplierName = "أحمد الحربي للديكور (Ahmed Decor)",
            paymentMethod = "Bank Transfer",
            expenseType = "Establishment",
            notes = "شراء وتجهيز الرفوف بالمعرض الرئيسي لتنظيم البضائع"
        ))

        // Establishment Expense 2: Cashier Counter
        calendar.add(Calendar.DAY_OF_YEAR, 2)
        db.expenseDao().insertExpense(Expense(
            title = "طاولات كاشير ومكاتب استقبال خشبية",
            amount = 4800.0,
            category = "Furniture",
            supplierName = "أحمد الحربي للديكور (Ahmed Decor)",
            paymentMethod = "Cash",
            expenseType = "Establishment",
            notes = "طاولة كاشير رئيسية مع وحدة تخزين مضيئة"
        ))

        // Electronics Expense: Cashier System & POS
        calendar.add(Calendar.DAY_OF_YEAR, 3)
        db.expenseDao().insertExpense(Expense(
            title = "شراء أجهزة كاشير ونظام نقاط البيع باللمس",
            amount = 8900.0,
            category = "Electronics",
            supplierName = "شركة التقنية المتقدمة (Advanced Tech)",
            paymentMethod = "Card",
            expenseType = "Operating",
            notes = "جهازي كاشير متكاملين مع طابعات فواتير باركود"
        ))

        // Inventory Purchase: Initial stock
        calendar.add(Calendar.DAY_OF_YEAR, 4)
        db.expenseDao().insertExpense(Expense(
            title = "الدفعة الأولى من البضائع والمخزون الاستهلاكي",
            amount = 35000.0,
            category = "Inventory",
            supplierName = "مستودعات النخبة للمخزون (Elite Warehouse)",
            paymentMethod = "Bank Transfer",
            expenseType = "Inventory",
            notes = "بضائع متنوعة لبدء المبيعات وتغطية أرفف المعرض"
        ))

        // Recurring Cost: Rent (Operating)
        calendar.add(Calendar.DAY_OF_YEAR, 3)
        db.expenseDao().insertExpense(Expense(
            title = "قيمة إيجار المعرض لشهر يونيو",
            amount = 15000.0,
            category = "Rent",
            supplierName = null,
            paymentMethod = "Bank Transfer",
            expenseType = "Operating",
            isRecurring = true,
            recurringInterval = "Monthly",
            notes = "الدفعة الإيجارية الشهرية لمقر المتجر الرئيسي"
        ))

        // Utilities: Internet (Recurring)
        calendar.add(Calendar.DAY_OF_YEAR, 2)
        db.expenseDao().insertExpense(Expense(
            title = "اشتراك إنترنت فايبر عالي السرعة وباقة هاتف",
            amount = 450.0,
            category = "Utilities",
            supplierName = "شركة التقنية المتقدمة (Advanced Tech)",
            paymentMethod = "Card",
            expenseType = "Operating",
            isRecurring = true,
            recurringInterval = "Monthly",
            notes = "الباقة المكتبية الذكية للأعمال"
        ))

        // Salaries: Month salary
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        db.expenseDao().insertExpense(Expense(
            title = "رواتب طاقم العمل والتشغيل (3 موظفين)",
            amount = 12000.0,
            category = "Salaries",
            supplierName = null,
            paymentMethod = "Bank Transfer",
            expenseType = "Operating",
            isRecurring = true,
            recurringInterval = "Monthly"
        ))

        // Small random operating cost
        db.expenseDao().insertExpense(Expense(
            title = "رسوم توصيل بضائع طارئة عبر الرياض للخدمات",
            amount = 280.0,
            category = "Miscellaneous",
            supplierName = "شركة الرياض للخدمات اللوجستية (Riyadh Logistics)",
            paymentMethod = "Cash",
            expenseType = "Operating"
        ))

        // Recalculate everything to ensure budgets are loaded correctly
        recalculateFinances()

        // Insert Initial AI Welcome Message
        db.aiMessageDao().insertMessage(AiMessage(
            role = "model",
            text = """
أهلاً بك في نظام **مساعد المالي الذكي (Smart Store Finance Copilot)**! 📊✨

أنا مستشارك المالي الذكي (CFO) المتاح لخدمتك على مدار الساعة. لقد قمت بتحليل البيانات المالية التأسيسية لمتجرك وسأقوم بمساعدتك في:
1. **تسجيل المصاريف بالصوت أو النص**: مثلاً تحدث معي طبيعياً: *"شريت أرفف بقيمة 1200 ريال من الحربي وسددت كاش"*.
2. **تصوير الفواتير والمنتجات (OCR)**: لتحليلها تلقائياً واستخلاص تفاصيل الضريبة والمورد.
3. **تنبؤ المصاريف والتنبيهات**: مثل الكشف المبكر عن تجاوز الميزانية أو ارتفاع أسعار الموردين.

**نصيحة مالية سريعة اليوم**: تشكل مصاريف التأسيس والمخزون الحالي حوالي **72%** من ميزانية شهر يونيو. أقترح تفعيل مراجعة عروض الأسعار للشحنات القادمة لتوفير حوالي **5%** من تكاليف الخدمات اللوجستية المتبقية. 

كيف يمكنني مساعدتك في حساباتك اليوم؟ 🇸🇦🤝
            """.trimIndent(),
            timestamp = System.currentTimeMillis()
        ))

        // Insert Initial Insights
        db.aiInsightDao().insertInsight(AiInsight(
            title = "تحليل مصاريف التأسيس والتجهيز",
            content = "تشكل تكاليف التأسيس (أرفف المعرض والديكور) نسبة 21.3% من إجمالي الإنفاق الحالي. يعتبر هذا المعدل ممتازاً مقارنة بالمتاجر المماثلة التي تنفق حوالي 30% كأصول ثابتة في مرحلة التجهيز.",
            type = "savings"
        ))
        db.aiInsightDao().insertInsight(AiInsight(
            title = "تنبؤ المصاريف القادمة لشهر يوليو",
            content = "استناداً إلى المصاريف الدورية النشطة (الإيجار، الإنترنت، الرواتب)، نتوقع إنفاقاً ثابتاً لا يقل عن 27,450 ريال سعودي للشهر القادم. يرجى ضمان سيولة كافية في الحساب الجاري لتجنب رسوم غرامات السداد.",
            type = "forecast"
        ))
    }
}
