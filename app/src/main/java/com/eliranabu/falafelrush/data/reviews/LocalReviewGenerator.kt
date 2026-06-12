package com.eliranabu.falafelrush.data.reviews

import com.eliranabu.falafelrush.data.database.CustomerReview

/**
 * Fully-offline Hebrew review generator.
 * Replaces the old Gemini API call: zero network, zero cost, zero privacy risk.
 * Templates are bucketed by the player's daily performance tier and support
 * {served}/{coins}/{left}/{errors} placeholders for personalized snark.
 */
object LocalReviewGenerator {

    private enum class PerformanceTier { PERFECT, GOOD, MESSY, DISASTER }

    // Reviewer persona pool (name + emoji baked in, matching the game's existing style)
    private val reviewerNames = listOf(
        "משה כהן 🥙", "שיר לוי 💅", "אלירן הגולש 🏄‍♂️", "יוסי המשפץ 🛠️",
        "נועה הסטודנטית 📚", "רסר שמעון 👮", "חיים המבקר 🕵️", "סבתא גאולה חמוצים 👵",
        "דודו מהמילואים 🪖", "אבי הנהג 🚕", "מירי מהוועד 📋", "תומר הפודקאסטר 🎙️",
        "ליאת מהסטודיו 🧘‍♀️", "ציון השכן 🏠", "ג'וני התייר 🎒", "אורי הביצועיסט 💻"
    )

    private val perfectTemplates = listOf(
        "אין דברים כאלה!!! {served} מנות בלי טעות אחת. הבן אדם מכונה. עליתי 2 קילו ושווה.",
        "הגעתי סקפטי, יצאתי מאמין. פלאפל ברמת מישלן של שכונה. אפס טעויות היום, בדקתי.",
        "השף הזה לא בנאדם. {served} פיתות מושלמות ברצף. תבדקו אותו אם הוא רובוט.",
        "חיכיתי 30 שניות וקיבלתי יצירת אמנות. {coins} שקל שווה כל אגורה. חוזר מחר עם כל המשפחה.",
        "הטחינה זורמת כמו נהר, הכדורים פריכים כמו ביסקוויט. דוכן עם נשמה. 10/10.",
        "ביקשתי קומפלט וקיבלתי קומפלט. אתם לא מבינים כמה זה נדיר בארץ הזאת.",
        "פעם ראשונה בחיים שאני לא מוצא על מה להתלונן. מתסכל. אבל טעים."
    )

    private val goodTemplates = listOf(
        "פלאפל טוב מאוד, שירות סביר. {served} מנות יצאו היום וזה ניכר שיש קצב. ממליץ.",
        "הכדורים חמים והפיתה טרייה. היו רגעים של לחץ בדוכן אבל בסוף הכל הסתדר.",
        "אחלה פלאפל יציב כמו בלוק איטונג. סגר לי את הצהריים בכיף וטיפטף על החולצה החדשה.",
        "המתנתי בתור יותר זמן מהתור לטסט לרכב. הפלאפל היה חם ופריך אז אני סולח.",
        "ביקשתי בלי סלט ושמו לי חצי גינה בפנים. הבעלים צעק עליי שזה בריא לי. לפחות הכדורים מדהימים.",
        "הכדורים כל כך חמים ששרפו לי את הלשון, אבל הטעם אלוהי! אחזור בטוח.",
        "מקום סולידי. {coins} מטבעות הכנסתי לקופה שלו היום ואני לא מתחרט. כמעט.",
        "השירות פה עצלן כמו החיילים שלי בשמירה אבל הטעם מדוגם חבל על הזמן. פלאפל תקני."
    )

    private val messyTemplates = listOf(
        "הפיתה לא הייתה מאוזנת ארכיטקטונית וקרסה בביס השלישי. הטעם גן עדן, הלוגיסטיקה טעונת שיפור.",
        "ראיתי {errors} מנות עפות לפח מול העיניים שלי. מצד שני, מה שהגיע אליי היה טעים. סע לאט.",
        "רציתי פיתה קומפלט עם טחינה, קיבלתי פיתה ממוטטת מהרוטב אבל זה מנחם ברמות של סוף סמסטר.",
        "בלגן בדוכן, צעקות, טחינה על התקרה. אבל יש פה פוטנציאל, מבטיח לחזור לבדוק שוב.",
        "{left} אנשים עזבו את התור לפניי. אני נשארתי כי אין לי חיים. הפלאפל בסדר גמור דווקא.",
        "הזמנתי חריף, קיבלתי מתוק. הזמנתי צ'יפס, קיבלתי מבט עצוב. חוויה מעניינת.",
        "סבתא שלי מכינה יותר מהר, אבל בשבילך זה נחמד. נו, לפחות הילד עובד ולא בבטלה."
    )

    private val disasterTemplates = listOf(
        "עמדתי בתור {left} דקות... סליחה, {left} אנשים עזבו לפניי. גם אני עזבתי. באתי רק לכתוב את הביקורת.",
        "ראיתי במו עיניי {errors} מנות נזרקות לפח. הפח אכל היום יותר טוב ממני.",
        "אדוני בעל הדוכן, באהבה: תשקול קריירה בהייטק. הפלאפל שלך עשה לי טראומה.",
        "הזמנתי פיתה. קיבלתי כאוס עטוף בלאפה של אכזבה. כוכב אחד וגם זה בנדיבות.",
        "השירות איטי, ההזמנות מתפספסות, והמלצר (שהוא גם הטבח, שהוא גם הקופאי) על סף קריסה.",
        "חצי מהתור התאדה מרוב ייאוש. החצי השני נשאר רק בשביל הדרמה. אני נשארתי בשביל הביקורת הזאת.",
        "מקווה שמחר יום יותר טוב, כי היום? היום היה סרט אימה עם טחינה."
    )

    /** Rating heuristic ported from the old static-fallback logic. */
    private fun tierFor(served: Int, errors: Int, left: Int): PerformanceTier = when {
        errors == 0 && left == 0 && served > 0 -> PerformanceTier.PERFECT
        left > 2 || errors > 2 -> if (left + errors >= served) PerformanceTier.DISASTER else PerformanceTier.MESSY
        else -> PerformanceTier.GOOD
    }

    fun generateReviews(
        day: Int,
        servedCount: Int,
        errorCount: Int,
        leftCount: Int,
        coinsEarned: Int
    ): List<CustomerReview> {
        val tier = tierFor(servedCount, errorCount, leftCount)
        val pool = when (tier) {
            PerformanceTier.PERFECT -> perfectTemplates
            PerformanceTier.GOOD -> goodTemplates
            PerformanceTier.MESSY -> messyTemplates
            PerformanceTier.DISASTER -> disasterTemplates
        }

        val ratings = when (tier) {
            PerformanceTier.PERFECT -> listOf(5, 5, 5)
            PerformanceTier.GOOD -> listOf(5, 4, 4)
            PerformanceTier.MESSY -> listOf(3, 3, 2)
            PerformanceTier.DISASTER -> listOf(1, 2, 1)
        }

        val names = reviewerNames.shuffled().take(3)
        val comments = pool.shuffled().take(3)

        return comments.mapIndexed { index, template ->
            CustomerReview(
                customerName = names[index],
                customerEmoji = "",
                rating = ratings[index],
                comment = template
                    .replace("{served}", servedCount.toString())
                    .replace("{coins}", coinsEarned.toString())
                    .replace("{left}", leftCount.toString())
                    .replace("{errors}", errorCount.toString()),
                dayNumber = day
            )
        }
    }
}
