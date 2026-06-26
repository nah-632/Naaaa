package com.example.ui.theme

object Loc {
    fun get(key: String, lang: String): String {
        return if (lang == "ar") {
            Arabic[key] ?: key
        } else {
            English[key] ?: key
        }
    }

    private val Arabic = mapOf(
        "app_title" to "المساعد المالي الذكي",
        "dashboard" to "لوحة التحكم",
        "copilot" to "المستشار الذكي",
        "expenses" to "المصاريف",
        "suppliers_budgets" to "الموردين والميزانيات",
        "settings" to "الإعدادات",
        
        // Dashboard
        "total_spent" to "إجمالي الإنفاق",
        "establishment_costs" to "تكاليف التأسيس",
        "operating_costs" to "التكاليف التشغيلية",
        "inventory_costs" to "تكاليف المخزون",
        "remaining_budget" to "الميزانية المتبقية",
        "budget_usage" to "استهلاك الميزانية العامة",
        "ai_insights" to "رؤى وتوصيات الذكاء الاصطناعي",
        "recent_activity" to "أحدث العمليات",
        "see_all" to "عرض الكل",
        "no_recent_expenses" to "لا توجد مصاريف مسجلة مؤخراً.",
        "weekly_stats" to "الإنفاق الأسبوعي",
        "monthly_stats" to "الإنفاق الشهري",
        "risk_warning" to "تنبيه مخاطر مالية",
        "savings_opp" to "فرصة توفير تكاليف",
        "forecast" to "التنبؤ المالي لشهر يوليو",

        // Copilot
        "copilot_welcome" to "أهلاً بك! أنا مستشارك المالي المتاح لمساعدتك 24/7. يمكنك التحدث معي طبيعياً بالصوت أو النص، أو تصوير الفواتير والمنتجات.",
        "input_hint" to "اكتب مصروفاً (مثال: شريت أرفف بـ 1200 ريال)...",
        "send" to "إرسال",
        "voice_record" to "تسجيل صوتي",
        "voice_recording" to "جاري التسجيل... اضغط للإيقاف",
        "ocr_receipt" to "مسح فاتورة",
        "photo_product" to "تصوير منتج",
        "analyzing" to "جاري التحليل واستخلاص الحقول عبر الذكاء الاصطناعي...",
        "ai_confirmation" to "تأكيد مستخلص الذكاء الاصطناعي",
        "ai_extracted_msg" to "لقد استخلصت المصاريف التالية بدقة عالية. يرجى تأكيد صحتها لحفظها في ميزانيتك:",
        "approve" to "تأكيد وحفظ",
        "discard" to "إلغاء",
        "ai_strategic_help" to "اطلب نصيحة استراتيجية (مثال: 'كيف أخفض التكاليف التشغيلية؟' أو 'تنبؤ بمصاريفي')",
        "generate_report" to "توليد تقرير استشاري",

        // Expenses
        "add_expense" to "إضافة مصروف يدوي",
        "edit_expense" to "تعديل المصروف",
        "expense_title" to "عنوان المصروف",
        "amount" to "المبلغ",
        "category" to "التصنيف",
        "supplier" to "المورد",
        "payment_method" to "طريقة الدفع",
        "expense_type" to "نوع المصروف",
        "tax_pct" to "نسبة الضريبة (VAT %)",
        "notes" to "ملاحظات إضافية",
        "is_recurring" to "مصروف دوري (متكرر)",
        "recurring_interval" to "معدل التكرار",
        "save" to "حفظ",
        "delete" to "حذف",
        "search_placeholder" to "البحث في المصاريف والموردين والملاحظات...",
        "all" to "الكل",
        "export" to "تصدير التقارير (CSV / JSON)",
        "export_success" to "تم تصدير التقرير المالي بنجاح إلى مجلد التنزيلات!",

        // Suppliers & Budgets
        "supplier_analytics" to "تحليلات واستحقاقات الموردين",
        "total_paid" to "إجمالي المدفوع",
        "pending_balance" to "مبالغ مستحقة",
        "add_supplier" to "إضافة مورد جديد",
        "supplier_name" to "اسم المورد",
        "supplier_phone" to "رقم الهاتف",
        "budgets_alloc" to "تخصيص ميزانيات الأقسام شريطة ألا تتعدى السقف",
        "add_budget" to "تحديد ميزانية تصنيف",
        "limit_amount" to "الحد الأقصى للميزانية",
        "category_limits" to "حدود الميزانيات ونسب الاستهلاك",

        // Settings
        "app_language" to "لغة التطبيق",
        "currency" to "العملة الأساسية",
        "store_name" to "اسم المتجر",
        "security_lock" to "قفل الحماية بالأمان البيومتري / رمز المرور",
        "backup_sync" to "النسخ الاحتياطي التلقائي والمزامنة السحابية",
        "about_copilot" to "مساعد المالي الذكي - النسخة المؤسسية المرخصة 2026",
        "language_name" to "العربية (Arabic)",
        "toggle_language" to "Change to English"
    )

    private val English = mapOf(
        "app_title" to "Finance Copilot",
        "dashboard" to "Dashboard",
        "copilot" to "Strategic AI",
        "expenses" to "Expenses",
        "suppliers_budgets" to "Suppliers & Budgets",
        "settings" to "Settings",
        
        // Dashboard
        "total_spent" to "Total Spent",
        "establishment_costs" to "Establishment Costs",
        "operating_costs" to "Operating Costs",
        "inventory_costs" to "Inventory Costs",
        "remaining_budget" to "Remaining Budget",
        "budget_usage" to "Global Budget Usage",
        "ai_insights" to "AI Core Insights & Analytics",
        "recent_activity" to "Recent Activity",
        "see_all" to "See All",
        "no_recent_expenses" to "No recent expenses recorded.",
        "weekly_stats" to "Weekly Spending",
        "monthly_stats" to "Monthly Spending",
        "risk_warning" to "Financial Risk Alarm",
        "savings_opp" to "Cost Savings Opportunity",
        "forecast" to "July Expense Forecasting",

        // Copilot
        "copilot_welcome" to "Welcome! I am your strategic CFO copilot. Speak or write naturally, or capture receipts to extract financial items securely.",
        "input_hint" to "Enter transaction details (e.g. Shelves for 1200 SAR)...",
        "send" to "Send",
        "voice_record" to "Voice Record",
        "voice_recording" to "Recording... Tap to Stop",
        "ocr_receipt" to "Scan Receipt",
        "photo_product" to "Snap Product",
        "analyzing" to "AI is analyzing & extracting financial fields...",
        "ai_confirmation" to "Confirm AI Extracted Fields",
        "ai_extracted_msg" to "The AI extracted the following transactions. Review and approve to persist them:",
        "approve" to "Approve & Save",
        "discard" to "Discard",
        "ai_strategic_help" to "Get advice (e.g., 'How to cut operating costs?' or 'Forecast my budget')",
        "generate_report" to "Generate Advisory Report",

        // Expenses
        "add_expense" to "Manual Expense",
        "edit_expense" to "Edit Expense",
        "expense_title" to "Expense Title",
        "amount" to "Amount",
        "category" to "Category",
        "supplier" to "Supplier",
        "payment_method" to "Payment Method",
        "expense_type" to "Expense Type",
        "tax_pct" to "Tax Percentage (VAT %)",
        "notes" to "Additional Notes",
        "is_recurring" to "Recurring Expense",
        "recurring_interval" to "Recurrence Interval",
        "save" to "Save",
        "delete" to "Delete",
        "search_placeholder" to "Search expenses, suppliers, notes...",
        "all" to "All",
        "export" to "Export Reports (CSV / JSON)",
        "export_success" to "Financial report exported to Downloads directory!",

        // Suppliers & Budgets
        "supplier_analytics" to "Supplier Analytics & Payables",
        "total_paid" to "Total Paid",
        "pending_balance" to "Pending Balance",
        "add_supplier" to "Add Supplier",
        "supplier_name" to "Supplier Name",
        "supplier_phone" to "Phone Number",
        "budgets_alloc" to "Departmental Budget Allocations",
        "add_budget" to "Set Category Budget",
        "limit_amount" to "Limit Amount",
        "category_limits" to "Category Budget Progress & Limits",

        // Settings
        "app_language" to "Application Language",
        "currency" to "Base Currency",
        "store_name" to "Store Name",
        "security_lock" to "Biometric Security / Passcode Lock",
        "backup_sync" to "Auto-backup & Cloud Synchronization",
        "about_copilot" to "Smart Store Finance Copilot - Enterprise Licensed 2026",
        "language_name" to "English",
        "toggle_language" to "التغيير إلى العربية"
    )
}
