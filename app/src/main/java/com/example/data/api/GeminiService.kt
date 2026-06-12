package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.database.CustomerReview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // Sarcastic and funny pre-written Hebrew reviews for fallback in case Gemini is offline or rate-limited
    private val staticFallbackReviews = listOf(
        Pair("משה כהן 🥙", "הכדורים כל כך חמים ששרפו לי את הלשון, אבל הטעם אלוהי! אחזור בטוח."),
        Pair("שיר לוי 💅", "ביקשתי בלי סלט ושמו לי חצי גינה בפנים. הבעלים צעק עליי 'זה בריא לך!', לפחות הכדורים מדהימים."),
        Pair("אלירן הגולש 🏄‍♂️", "המתנתי בתור יותר זמן מהתור לטסט לרכב. הפלאפל היה חם ופריך אז אני סולח."),
        Pair("יוסי המשפץ 🛠️", "אחלה פלאפל יציב כמו בלוק איטונג. סגר לי את הצהריים בכיף וטיפטף על החולצה החדשה."),
        Pair("נועה הסטודנטית 📚", "רציתי פיתה קומפלט עם טחצינה, קיבלתי פיתה ממוטטת מהרוטב אבל זה מנחם ברמות של סוף סמסטר."),
        Pair("רסר שמעון 👮", "השירות פה עצלן כמו החיילים שלי בשמירה אבל הטעם מדוגם חבל על הזמן. פלאפל תקני."),
        Pair("חיים המבקר 🕵️", "הפיתה לא הייתה מאוזנת ארכיטקטונית וקרסה בביס השלישי. הטעם גן עדן, הלוגיסטיקה טעונת שיפור."),
        Pair("סבתא גאולה חמוצים 👵", "הפלאפל שלי יותר טוב, אבל בשבילך זה נחמד. נו, לפחות הילד עובד ולא בבטלה.")
    )

    suspend fun generateReviews(
        day: Int,
        servedCount: Int,
        errorCount: Int,
        leftCount: Int,
        statsDetails: String = ""
    ): List<CustomerReview> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is holding placeholder or empty. Using fallback static reviews.")
            return@withContext getFallbackReviews(day, servedCount, errorCount, leftCount)
        }

        val prompt = """
            אתה משמש כמחולל ביקורות הומוריסטיות בעברית למשחק מובייל בשם 'Falafel Rush' (ריצת הפלאפל).
            השחקן סיים כעת את יום מספר ${day} בדוכן הפלאפל שלו.
            הנה נתוני היום הנוכחי של השחקן:
            - לקוחות שקיבלו מנה מושלמת בהצלחה: ${servedCount} לקוחות
            - לקוחות שנזרקה להם המנה כי השחקן טעה במרכיבים: ${errorCount} לקוחות
            - לקוחות שהתייאשו מההמתנה ועזבו את התור בזעם: ${leftCount} לקוחות
            פרטי יום נוספים: ${statsDetails}

            אנא חולל 3 ביקורות לקוחות משעשעות ודמיוניות בפורמט JSON בלבד, המתאימות לביצועים אלו.
            הביקורות צריכות להיות בסגנון גוגל מפות (Google Maps) או פייסבוק, קצרות, קולעות, מצחיקות ועושות שימוש בסלנג ישראלי עשיר.
            אם השחקן היה מושלם (0 טעויות, 0 עזיבות), הביקורות צריכות להיות נלהבות ברמת פולחן אישיות. 
            אם השחקן עשה הרבה טעויות או אנשים עזבו, הביקורות צריכות להיות סרקסטיות וקורעות מצחוק על השירות והבלגן בדוכן.

            חייב להחזיר רק מערך JSON תקין (strict JSON array) של אובייקטים, ללא כתיבת קוד Markdown (כלומר בלי סימני ```json או טקסט מקדים/מסיים כלשהו).
            מבנה אובייקט במערך:
            [
              {
                "name": "שם הלקוח בתוספת אימוג'י מתאים",
                "rating": 1-5 (מספר כוכבים מ-1 עד 5 לפי רמת שביעות רצונו בהקשר לנתונים),
                "comment": "תוכן הביקורת המשעשעת בעברית"
              }
            ]
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(
                        JSONObject().put("text", prompt)
                    ))
                ))
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)
            
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Unsuccessful API call to Gemini: ${response.code} ${response.message}")
                return@withContext getFallbackReviews(day, servedCount, errorCount, leftCount)
            }

            val bodyString = response.body?.string() ?: ""
            val jsonResponse = JSONObject(bodyString)
            val text = jsonResponse
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            Log.d(TAG, "Gemini Raw Response: $text")
            
            // Clean markdown syntax wrapping if any
            val cleanedText = text.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonArray = JSONArray(cleanedText)
            val list = mutableListOf<CustomerReview>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CustomerReview(
                        customerName = obj.getString("name"),
                        customerEmoji = "", // Included inside name
                        rating = obj.getInt("rating").coerceIn(1, 5),
                        comment = obj.getString("comment"),
                        dayNumber = day
                    )
                )
            }
            return@withContext list
        } catch (e: Exception) {
            Log.e(TAG, "Error generating or parsing Gemini reviews, falling back: ${e.message}", e)
            return@withContext getFallbackReviews(day, servedCount, errorCount, leftCount)
        }
    }

    private fun getFallbackReviews(
        day: Int,
        servedCount: Int,
        errorCount: Int,
        leftCount: Int
    ): List<CustomerReview> {
        val selected = staticFallbackReviews.shuffled().take(3)
        return selected.mapIndexed { index, (name, comment) ->
            // Calculate a plausible rating based on served vs failed ratio
            var rating = 4
            if (leftCount > 2 || errorCount > 2) {
                rating = when (index) {
                    0 -> 1
                    1 -> 2
                    else -> 3
                }
            } else if (leftCount == 0 && errorCount == 0) {
                rating = 5
            } else {
                rating = if (index == 0) 5 else 4
            }

            CustomerReview(
                customerName = name,
                customerEmoji = "",
                rating = rating,
                comment = comment,
                dayNumber = day
            )
        }
    }
}
