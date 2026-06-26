package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    /**
     * Extracts structured financial expenses from raw text, speech transcripts, or receipt OCR.
     */
    suspend fun extractExpenses(userInput: String, bitmap: Bitmap? = null): List<ExtractedExpense> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is not configured!")
            return@withContext emptyList()
        }

        val prompt = if (bitmap != null) {
            "Analyze this receipt or product image, read all text, and extract the financial expenses. Return only valid JSON."
        } else {
            "Extract all financial expenses from the following text: \"$userInput\""
        }

        val systemInstruction = """
            You are the Chief Financial Officer (CFO) and Core AI Extraction Engine for "Smart Store Finance Copilot".
            Extract the financial expenses from the user's input (can be text or a receipt/product image).
            You MUST return a JSON object containing a key "expenses" which is a list of objects.
            Each expense object MUST have the following schema:
            {
                "title": "String (Description in Arabic, e.g., 'تركيب رفوف' or 'شراء أجهزة كاشير')",
                "amount": Double (Total amount spent),
                "category": "String (Must be one of: 'Business Establishment', 'Furniture', 'Equipment', 'Electronics', 'Inventory', 'Rent', 'Utilities', 'Salaries', 'Maintenance', 'Marketing', 'Taxes', 'Miscellaneous')",
                "supplierName": "String? (Extracted supplier name if found, in Arabic or English)",
                "paymentMethod": "String (One of: 'Cash', 'Card', 'Bank Transfer')",
                "expenseType": "String (One of: 'Establishment', 'Operating', 'Inventory', 'Recurring')",
                "taxPercentage": Double (Usually 15.0 for Saudi VAT, or 0.0 if not specified),
                "confidenceScore": Double (Between 0.0 and 1.0 representing your confidence),
                "notes": "String? (Any secondary useful context in Arabic)",
                "isRecurring": Boolean (true if this is a recurring subscription, internet, rent, electricity, etc.),
                "recurringInterval": "String? (One of: 'Monthly', 'Weekly', 'Daily' or null)"
            }
            Rules:
            1. Keep the title and notes in Arabic if the user's language is Arabic.
            2. Split multiple expenses if they are listed together (e.g., 'shelves for 1200, chairs for 600' -> two separate expenses).
            3. Never guess values. If the amount is not clear, assign confidenceScore < 0.5.
            4. If the image is a product, categorize it as 'Inventory', 'Equipment', or 'Furniture' and fill in standard fields.
            Return ONLY the valid JSON block. Do not include markdown code block formatting (like ```json).
        """.trimIndent()

        try {
            val rootJson = JSONObject()
            
            // System instruction
            val sysParts = JSONArray().put(JSONObject().put("text", systemInstruction))
            rootJson.put("systemInstruction", JSONObject().put("parts", sysParts))

            // Contents
            val contentParts = JSONArray()
            contentParts.put(JSONObject().put("text", prompt))

            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                
                val inlineData = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Data)
                }
                contentParts.put(JSONObject().put("inlineData", inlineData))
            }

            val contentObj = JSONObject().put("parts", contentParts)
            rootJson.put("contents", JSONArray().put(contentObj))

            // Generation config for JSON
            val genConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1) // Low temperature for high precision
            }
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody(mediaTypeJson)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API Call failed: Code=${response.code}, Msg=${response.message}")
                    return@withContext emptyList()
                }

                val bodyStr = response.body?.string() ?: ""
                Log.d(TAG, "Raw API Response: $bodyStr")

                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val textResponse = parts?.optJSONObject(0)?.optString("text") ?: ""

                Log.d(TAG, "Extracted Text Response: $textResponse")

                return@withContext parseExtractedExpenses(textResponse)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    /**
     * Chat model for conversational Financial Strategic Consulting & Advisor.
     */
    suspend fun getStrategicAdvice(history: List<Pair<String, String>>, financialContext: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "خطأ: لم يتم تهيئة مفتاح API الذكاء الاصطناعي بشكل صحيح. يرجى تهيئته عبر لوحة Secrets."
        }

        val systemInstruction = """
            You are "Advisor Copilot", an elite Certified Public Accountant (CPA) and Chief Financial Officer (CFO) with decades of retail experience.
            You are speaking with a retail store owner in Saudi Arabia. 
            Arabic is your primary language. Speak in a highly professional, encouraging, objective, and supportive business tone.
            Use proper financial terms like: التدفقات النقدية (Cash Flow), ميزانية التأسيس (Establishment Budget), التكلفة التشغيلية (Operating Costs), الربحية (Profitability), نقطة التعادل (Break-even Point).

            You are given a current snapshot of their financial database below:
            $financialContext

            Guidelines:
            1. Give precise, actionable recommendations based ONLY on the actual numbers provided.
            2. Never invent or assume data that is not in the context.
            3. Detect cost-reduction opportunities (e.g., if utilities or logistics costs seem high).
            4. Offer monthly budget forecasts and suggestions for inventory optimization.
            5. Address the user directly in Arabic with a clean, bulleted layout.
        """.trimIndent()

        try {
            val rootJson = JSONObject()
            
            // System instruction
            val sysParts = JSONArray().put(JSONObject().put("text", systemInstruction))
            rootJson.put("systemInstruction", JSONObject().put("parts", sysParts))

            // Contents (including history)
            val contentsArray = JSONArray()
            
            for (turn in history) {
                val role = if (turn.first == "user") "user" else "model"
                val textPart = JSONObject().put("text", turn.second)
                val partsArray = JSONArray().put(textPart)
                val contentObj = JSONObject().apply {
                    put("role", role)
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
            }

            rootJson.put("contents", contentsArray)

            val requestBody = rootJson.toString().toRequestBody(mediaTypeJson)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "عذراً، حدث خطأ في الاتصال بالخادم الذكي. الرمز: ${response.code}"
                }

                val bodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val answer = parts?.optJSONObject(0)?.optString("text") ?: ""

                return@withContext answer
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getStrategicAdvice: ${e.message}", e)
            return@withContext "عذراً، واجهت مشكلة أثناء تحليل البيانات المالية: ${e.message}"
        }
    }

    private fun parseExtractedExpenses(jsonStr: String): List<ExtractedExpense> {
        val list = mutableListOf<ExtractedExpense>()
        try {
            // Clean markdown code blocks if the model returned them despite system prompt
            var cleaned = jsonStr.trim()
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substringAfter("\n").substringBeforeLast("```")
            }
            cleaned = cleaned.trim()

            val rootObj = JSONObject(cleaned)
            val array = rootObj.optJSONArray("expenses") ?: JSONArray()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ExtractedExpense(
                        title = obj.optString("title", "مصروف مستخلص"),
                        amount = obj.optDouble("amount", 0.0),
                        category = obj.optString("category", "Miscellaneous"),
                        supplierName = if (obj.isNull("supplierName")) null else obj.optString("supplierName"),
                        paymentMethod = obj.optString("paymentMethod", "Cash"),
                        expenseType = obj.optString("expenseType", "Operating"),
                        taxPercentage = obj.optDouble("taxPercentage", 15.0),
                        confidenceScore = obj.optDouble("confidenceScore", 1.0),
                        notes = if (obj.isNull("notes")) null else obj.optString("notes"),
                        isRecurring = obj.optBoolean("isRecurring", false),
                        recurringInterval = if (obj.isNull("recurringInterval")) null else obj.optString("recurringInterval")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing extracted expenses: ${e.message}. JSON was: $jsonStr", e)
        }
        return list
    }
}

data class ExtractedExpense(
    val title: String,
    val amount: Double,
    val category: String,
    val supplierName: String?,
    val paymentMethod: String,
    val expenseType: String,
    val taxPercentage: Double,
    val confidenceScore: Double,
    val notes: String?,
    val isRecurring: Boolean,
    val recurringInterval: String?
)
