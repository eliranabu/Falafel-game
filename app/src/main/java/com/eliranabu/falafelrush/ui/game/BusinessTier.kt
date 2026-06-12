package com.eliranabu.falafelrush.ui.game

import androidx.compose.ui.graphics.Color

// --- progression model for Tycoon / Business Tiers ---
data class BusinessTier(
    val id: String,
    val titleHe: String,
    val emoji: String,
    val requiredCoins: Int,
    val requiredDay: Int,
    val color: Color,
    val description: String
)

fun getActiveBusinessTier(totalCoins: Int, day: Int): BusinessTier {
    return when {
        totalCoins >= 4000 && day >= 15 -> BusinessTier("EMPIRE", "אימפריית פלאפל עולמית", "🌍", 4000, 15, Color(0xFFD500F9), "סניפים בלונדון, ניו יורק וטוקיו! הטעמים והשיטה התפעולית שלך שולטים בעולם.")
        totalCoins >= 1400 && day >= 8 -> BusinessTier("BISTRO", "מסעדת גורמה פלאפל", "🏛️", 1400, 8, Color(0xFF00E5FF), "מסעדה מודרנית ויוקרתית עם לקוחות VIP קבועים ומנות מעושנות.")
        totalCoins >= 600 && day >= 4 -> BusinessTier("MARKET", "בסטה בשוק מחנה יהודה", "🏪", 600, 4, Color(0xFFFF9100), "רעש של בסטה, מוזיקת שוק ופיוז'ן של סלטים וטחינה סמיכה.")
        totalCoins >= 150 && day >= 2 -> BusinessTier("TRUCK", "פודטראק פלאפל מעוצב", "🚚", 150, 2, Color(0xFFFFD54F), "גלגלים, שירות קליל, מוזיקת רטרו והכנסות מעולות ברחבי העיר.")
        else -> BusinessTier("CART", "דוכן שכונתי פשוט", "🫓", 0, 1, Color(0xFFA1887F), "התחלת העסק הצנוע שלך ברחוב קטן. תהפוך את כדורי הפלאפל למכרה של זהב!")
    }
}

fun getNextBusinessTier(totalCoins: Int, day: Int): BusinessTier? {
    return when {
        totalCoins < 150 || day < 2 -> BusinessTier("TRUCK", "פודטראק פלאפל מעוצב", "🚚", 150, 2, Color(0xFFFFD54F), "")
        totalCoins < 600 || day < 4 -> BusinessTier("MARKET", "בסטה בשוק מחנה יהודה", "🏪", 600, 4, Color(0xFFFF9100), "")
        totalCoins < 1400 || day < 8 -> BusinessTier("BISTRO", "מסעדת גורמה פלאפל", "🏛️", 1400, 8, Color(0xFF00E5FF), "")
        totalCoins < 4000 || day < 15 -> BusinessTier("EMPIRE", "אימפריית פלאפל עולמית", "🌍", 4000, 15, Color(0xFFD500F9), "")
        else -> null
    }
}
