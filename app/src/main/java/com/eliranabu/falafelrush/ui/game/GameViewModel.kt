package com.eliranabu.falafelrush.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eliranabu.falafelrush.data.database.AppDatabase
import com.eliranabu.falafelrush.data.database.CustomerReview
import com.eliranabu.falafelrush.data.database.GameRepository
import com.eliranabu.falafelrush.data.database.GameSaveState
import com.eliranabu.falafelrush.data.reviews.LocalReviewGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

// Enum representing the overall game scene/state
enum class GameScreen {
    START_SCREEN,
    GAMEPLAY,
    DAY_SUMMARY,
    UPGRADES,
    REVIEWS
}

// Game ingredients representing a premium Falafel pita assembly
enum class Ingredient(val displayName: String, val emoji: String, val colorHex: Long) {
    PITA("פיתה", "🫓", 0xFFE5C185),
    FALAFEL("פלאפל חם", "🧆", 0xFF6D4C41),
    SALAD("סלט ישראלי", "🥗", 0xFF4CAF50),
    TAHINI("טחינה סמיכה", "🍶", 0xFFFFF9C4),
    CHIPS("צ'יפס זהוב", "🍟", 0xFFFFD54F),
    SPICY("חריף פיקנטי", "🌶️", 0xFFFF1744)
}

// Fixed order templates based on core game specs
enum class OrderPreset(val displayName: String, val ingredients: List<Ingredient>, val baseValue: Int) {
    ONLY_FALAFEL("פלאפל קלאסי", listOf(Ingredient.PITA, Ingredient.FALAFEL), 15),
    FALAFEL_TAHINI("פלאפל טחינה", listOf(Ingredient.PITA, Ingredient.FALAFEL, Ingredient.TAHINI), 20),
    FALAFEL_SALAD("פלאפל סלט", listOf(Ingredient.PITA, Ingredient.FALAFEL, Ingredient.SALAD), 20),
    FALAFEL_COMPLETE("שלם קומפלט", listOf(Ingredient.PITA, Ingredient.FALAFEL, Ingredient.SALAD, Ingredient.TAHINI), 30),
    FALAFEL_CHIPS("ספיישל צ'יפס", listOf(Ingredient.PITA, Ingredient.FALAFEL, Ingredient.CHIPS, Ingredient.TAHINI), 35),
    FALAFEL_SPICY("פיתה חריפה אש", listOf(Ingredient.PITA, Ingredient.FALAFEL, Ingredient.SALAD, Ingredient.TAHINI, Ingredient.SPICY), 38),
    DELUXE_MASTERY("פלאפל פרימיום דלוקס", listOf(Ingredient.PITA, Ingredient.FALAFEL, Ingredient.SALAD, Ingredient.TAHINI, Ingredient.CHIPS, Ingredient.SPICY), 55)
}

// 2026 Daily dynamic event simulations
enum class DailyEvent(
    val titleHe: String,
    val descriptionHe: String,
    val emoji: String,
    val colorHex: Long,
    val priceModifier: Float = 1.0f,
    val patienceModifier: Float = 1.0f,
    val customerSpawnMod: Float = 1.0f,
    val trashPenalty: Int = 0
) {
    NORMAL("יום עסקים שגרתי", "הכל כרגיל ברחוב. הטושים מושחזים והבריכה חמה.", "🫓", 0xFF00E5FF, 1.0f, 1.0f, 1.0f, 0),
    FALAFEL_DAY("יום הפלאפל הבינלאומי!", "טירוף פלאפל ארצי! ביקוש גבוה במיוחד והכנסות ברוטו X1.4 לכל מנה!", "🌍", 0xFFD500F9, 1.4f, 1.0f, 1.4f, 0),
    STORM("סופת גשם פתאומית", "גשם זלעפות ברחוב! לאנשים קר ואין להם סבלנות לעמוד בתור. סבלנות יורדת מהר ב-30%!", "⛈️", 0xFFFF1744, 0.9f, 0.7f, 0.8f, 0),
    CHEF_VISIT("מבקר המזון ניב גלבוע בדרך!", "המבקר הגדול בשכונה! השחקן חייב לשמור על ניקיון ודיוק. כל מנה שנזרקת גוררת קנס של 5- מטבעות!", "🧐", 0xFFFF9100, 1.15f, 0.9f, 0.9f, 5),
    STREET_PARTY("קרנבל מסיבת רחוב", "מוזיקה מחרישת אוזניים ורחוב חסום! לקוחות רעבים זורמים בכמות כפולה!", "🥳", 0xFF00E676, 1.0f, 1.1f, 1.8f, 0)
}

fun getEventForDay(day: Int): DailyEvent {
    // Determine daily event cycles deterministically
    return when (day % 5) {
        1 -> DailyEvent.NORMAL
        2 -> DailyEvent.STORM
        3 -> DailyEvent.FALAFEL_DAY
        4 -> DailyEvent.CHEF_VISIT
        0 -> DailyEvent.STREET_PARTY
        else -> DailyEvent.NORMAL
    }
}

// 2026 High Quality Customer Persona/Segment
enum class CustomerType(
    val typeName: String,
    val avatar: String,
    val description: String,
    val basePhrase: String,
    val patienceDecayScale: Float, // speed of patience loss
    val tipMultiplier: Float,
    val isVip: Boolean = false
) {
    STUDENT("סטודנט תפרן 🎓", "🎓", "סבלני מאוד, מבקש מחירי בסיס", "תוסיף קצת סלט אחי, תעלה לי את החיוך.", 0.6f, 0.8f),
    BUSINESSMAN("הייטקיסט לחוץ 💻", "👨‍💻", "תמיד ממהר, אבל מביא טיפ עצום!", "אני מאחר לדיילי. תן פה פיתה קומפלט בשנייה!", 1.6f, 1.8f),
    TOURIST("תייר סקרן 🎒", "🎒", "אוהב לצלם סרטונים, סבלנות שיא", "Oh! Dynamic Israel falafel? Can you make it perfect?", 0.4f, 1.2f),
    FAMILY("משפחה מורעבת 👨‍👩‍👧‍👦", "👨‍👩‍👧‍👦", "סלטים וסבלנות סבירה", "תעמיס פלאפל קומפלט שיספיק לכל הילדים!", 1.0f, 1.4f),
    CRITIC("מבקר מסעדות 🧐", "🧐", "אם המנה מדויקת, ייתן דירוג 5 כוכבים פצצה!", "אני בוחן את פריכות הכדורים והאקוסטיקה של הפיתה הזו.", 1.2f, 1.5f),
    CELEBRITY("סלב מקומי 👑", "⭐", "נדיר ביותר. מאבד סבלנות מהר מאוד ומפנק בטיפ ענק!", "שיאאו יא חביבי! תפנק אותי בקומפלט מהסרטים!", 2.2f, 4.0f, true)
}

// Interactive customer model
data class GameCustomer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatar: String,
    val description: String,
    val phrase: String,
    val requiredOrder: List<Ingredient>,
    val orderPreset: OrderPreset,
    val maxPatienceSec: Float,
    val currentPatience: Float, // ranges from 1.0 down to 0.0
    val isVip: Boolean = false,
    val tipScale: Float = 1.0f,
    val type: CustomerType = CustomerType.STUDENT
)

// Floating particle models for 2026 style physical sensory responses
data class GameParticle(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Long,
    val rotation: Float = 0f,
    val isCoin: Boolean = true,
    val emoji: String = "🪙"
)

// Comprehensive state for the entire app UI
data class GameUiState(
    val currentScreen: GameScreen = GameScreen.START_SCREEN,
    
    // Saved Player Progress
    val saveState: GameSaveState = GameSaveState(),
    val allHistoryReviews: List<CustomerReview> = emptyList(),
    
    // Live Game Day Variables
    val activeEvent: DailyEvent = DailyEvent.NORMAL,
    val dayTimeRemainingSec: Int = 120, // 2 minutes
    val activeCustomers: List<GameCustomer> = emptyList(),
    val preparedIngredients: List<Ingredient> = emptyList(), // what user added
    val comboStreak: Int = 0,
    val isRushHour: Boolean = false, // Rush hour triggers intense events
    
    // Day Ledger Stats
    val servedCountToday: Int = 0,
    val failedCountToday: Int = 0, // trash/errors
    val impatientLeftToday: Int = 0, // people who walked away
    val revenueEarnedToday: Int = 0,
    
    // Visual Feedback Indicators
    val screenShake: Float = 0f, // 0 to 12dp shake magnitude
    val activeFloatingReview: CustomerReview? = null,
    val showFloatingToast: String? = null,
    val particles: List<GameParticle> = emptyList(),
    
    val generatedTodayReviews: List<CustomerReview> = emptyList(),
    val feedbackMessage: String = "מוכן לעבודה! תעמיס פיתות 🫓"
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository
    
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Coroutine Jobs for game loop, customer spawners and particle systems
    private var gameLoopJob: Job? = null
    private var customerTimerJob: Job? = null
    private var particlePhysicsJob: Job? = null
    
    // Names database for authentic Israeli high-fidelity local characters
    private val firstNames = listOf("יוסי", "רוני", "מיכל", "אביב", "שמעון", "שושנה", "קובי", "ירדן", "חן", "קרן", "נתנאל", "יובל", "מאיר", "דברת", "נועם", "עומר", "לירז")

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
        
        // Listen to Database Updates
        viewModelScope.launch {
            repository.saveState.collect { dbState ->
                if (dbState != null) {
                    _uiState.update { it.copy(saveState = dbState) }
                } else {
                    // Create primary state if missing
                    repository.updateSaveState(GameSaveState())
                }
            }
        }
        
        viewModelScope.launch {
            repository.reviews.collect { dbReviews ->
                _uiState.update { it.copy(allHistoryReviews = dbReviews) }
            }
        }
        
        // Start physical particles ticker loop globally
        startParticleTickEngine()
    }

    // Set App Screen
    fun setScreen(screen: GameScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
        if (screen != GameScreen.GAMEPLAY) {
            stopDayJobs()
        }
    }

    // Reset All Game Saves
    fun resetFullGame() {
        viewModelScope.launch {
            repository.resetGame()
            _uiState.update { GameUiState(currentScreen = GameScreen.START_SCREEN) }
            showFeedback("המשחק אופס בהצלחה! התחלנו מחדש 🆕")
        }
    }

    // Purchase Upgrades (Dynamic costs and bonuses)
    fun purchaseUpgrade(type: String) {
        viewModelScope.launch {
            val currentState = _uiState.value.saveState
            var cost = 9999
            var updatedState: GameSaveState? = null
            
            when (type) {
                "SPEED" -> {
                    val currentLevel = currentState.speedUpgradeLevel
                    cost = currentLevel * 45 + 20
                    if (currentState.totalCoins >= cost) {
                        updatedState = currentState.copy(
                            totalCoins = currentState.totalCoins - cost,
                            speedUpgradeLevel = currentLevel + 1
                        )
                    }
                }
                "PRICE" -> {
                    val currentLevel = currentState.priceUpgradeLevel
                    cost = currentLevel * 60 + 35
                    if (currentState.totalCoins >= cost) {
                        updatedState = currentState.copy(
                            totalCoins = currentState.totalCoins - cost,
                            priceUpgradeLevel = currentLevel + 1
                        )
                    }
                }
                "PATIENCE" -> {
                    val currentLevel = currentState.patienceUpgradeLevel
                    cost = currentLevel * 50 + 25
                    if (currentState.totalCoins >= cost) {
                        updatedState = currentState.copy(
                            totalCoins = currentState.totalCoins - cost,
                            patienceUpgradeLevel = currentLevel + 1
                        )
                    }
                }
                "MARKETING" -> {
                    val currentLevel = currentState.marketingUpgradeLevel
                    cost = currentLevel * 80 + 50
                    if (currentState.totalCoins >= cost) {
                        updatedState = currentState.copy(
                            totalCoins = currentState.totalCoins - cost,
                            marketingUpgradeLevel = currentLevel + 1
                        )
                    }
                }
                "AUTO_SAUCE" -> {
                    val currentLevel = currentState.autoSauceUpgradeLevel
                    cost = (currentLevel + 1) * 150
                    if (currentState.totalCoins >= cost && currentLevel < 1) { // Unlock at max 1 for auto-tahini
                        updatedState = currentState.copy(
                            totalCoins = currentState.totalCoins - cost,
                            autoSauceUpgradeLevel = currentLevel + 1
                        )
                    }
                }
            }

            if (updatedState != null) {
                repository.updateSaveState(updatedState)
                triggerParticleBurst(300f, 600f, amount = 25, isCoin = false, emoji = "⭐")
                showFeedback("שדרוג בוצע בהצלחה! 🎉")
            } else {
                triggerScreenShake(4f)
                showFeedback("אין מספיק מטבעות פלאפל לשדרוג זה! 🪙")
            }
        }
    }

    // Show temporary fun feedback messages
    private fun showFeedback(msg: String) {
        _uiState.update { it.copy(feedbackMessage = msg) }
    }

    // Toggle Sound preference
    fun toggleSound() {
        viewModelScope.launch {
            val dbState = _uiState.value.saveState
            repository.updateSaveState(dbState.copy(soundEffectsEnabled = !dbState.soundEffectsEnabled))
        }
    }

    // Start a Fresh Gameplay Day (2 minutes of intense rush)
    fun startNewDay() {
        stopDayJobs()
        
        val nextDayEvent = getEventForDay(_uiState.value.saveState.currentDay)
        
        _uiState.update {
            it.copy(
                currentScreen = GameScreen.GAMEPLAY,
                activeEvent = nextDayEvent,
                dayTimeRemainingSec = 120,
                activeCustomers = emptyList(),
                preparedIngredients = emptyList(),
                comboStreak = 0,
                isRushHour = false,
                servedCountToday = 0,
                failedCountToday = 0,
                impatientLeftToday = 0,
                revenueEarnedToday = 0,
                generatedTodayReviews = emptyList(),
                feedbackMessage = "בוקר אור! הדוכן נפתח תחת אירוע: ${nextDayEvent.titleHe}! 🧆"
            )
        }

        // Spawn first customer immediately
        spawnCustomer()
        
        // Start live count down timer
        startGameLoop()
        
        // Start real-time customer patience and spawning intervals 
        startCustomerScheduler()
    }

    private fun stopDayJobs() {
        gameLoopJob?.cancel()
        customerTimerJob?.cancel()
    }

    private fun startGameLoop() {
        gameLoopJob = viewModelScope.launch {
            while (_uiState.value.dayTimeRemainingSec > 0) {
                delay(1000)
                val newTime = _uiState.value.dayTimeRemainingSec - 1
                
                // Periodic Rush Hour (lasts 15 seconds)
                // Rush Hour 1: 90s remaining (30s after start)
                // Rush Hour 2: 30s remaining (90s after start)
                val wasRushHour = _uiState.value.isRushHour
                val isNowRushHour = (newTime in 75..90) || (newTime in 15..30)
                
                _uiState.update { 
                    it.copy(
                        dayTimeRemainingSec = newTime,
                        isRushHour = isNowRushHour
                    ) 
                }
                
                if (isNowRushHour && !wasRushHour) {
                    triggerScreenShake(8f)
                    triggerParticleBurst(400f, 300f, amount = 25, isCoin = false, emoji = "🚨")
                    showFeedback("🚨 שעת עומס מטורפת! לקוחות זורמים במהירות כפולה ומשאירים טיפים משוגעים! 🚨")
                } else if (!isNowRushHour && wasRushHour) {
                    showFeedback("נשמנו לרווחה... שעת העומס חלפה בינתיים 😌")
                }
            }
            // Day Finished! Process End of Day
            endGameplayDay()
        }
    }

    private fun startCustomerScheduler() {
        customerTimerJob = viewModelScope.launch {
            while (true) {
                delay(100) // update patience/timers frequently for fluid movement at 60fps
                
                // Decay patience of existing customers
                var customerLeft = false
                val updatedCustomers = _uiState.value.activeCustomers.mapNotNull { customer ->
                    // Speeds up decay unless patience/speed upgrade level is high
                    val upgradeSlowdown = 1.0f + (_uiState.value.saveState.patienceUpgradeLevel * 0.15f)
                    val eventPatienceMod = _uiState.value.activeEvent.patienceModifier
                    // Decay scale is affected by the customer archetype & the active Daily Event modifier
                    val decayRate = (0.1f * customer.type.patienceDecayScale) / (customer.maxPatienceSec * upgradeSlowdown * eventPatienceMod)
                    val newPatience = customer.currentPatience - decayRate
                    
                    if (newPatience <= 0f) {
                        customerLeft = true
                        _uiState.update { it.copy(impatientLeftToday = it.impatientLeftToday + 1, comboStreak = 0) }
                        triggerScreenShake(7f)
                        null // Customer walks away angry
                    } else {
                        customer.copy(currentPatience = newPatience)
                    }
                }
                
                if (customerLeft) {
                    showFeedback("אוי לא! לקוח התייאש מההמתנה ועזב! 😡")
                }

                _uiState.update { it.copy(activeCustomers = updatedCustomers) }

                // Potentially spawn new customers up to limits
                val marketingLevel = _uiState.value.saveState.marketingUpgradeLevel
                val isRush = _uiState.value.isRushHour
                val maxCustomersLimit = if (marketingLevel >= 3) 5 else 4
                
                // Spawning check is significantly accelerated during Rush Hour & active Daily Events
                val baseSpawnChance = if (isRush) 0.08f else 0.025f // 8% vs 2.5% chance every 100ms
                val spawnChance = baseSpawnChance * _uiState.value.activeEvent.customerSpawnMod
                if (updatedCustomers.size < maxCustomersLimit && Random.nextFloat() < spawnChance) {
                    spawnCustomer()
                }
            }
        }
    }

    private fun spawnCustomer() {
        val currentActive = _uiState.value.activeCustomers
        val marketingLevel = _uiState.value.saveState.marketingUpgradeLevel
        val isRush = _uiState.value.isRushHour
        
        val maxCustomersLimit = if (marketingLevel >= 3) 5 else 4
        if (currentActive.size >= maxCustomersLimit) return

        // 1. Dynamic Customer Type Probability
        val customerType = when (Random.nextFloat()) {
            in 0f..(0.04f + marketingLevel * 0.02f) -> CustomerType.CELEBRITY
            in 0.06f..(0.13f + marketingLevel * 0.01f) -> CustomerType.CRITIC
            in 0.14f..0.34f -> CustomerType.BUSINESSMAN
            in 0.35f..0.55f -> CustomerType.TOURIST
            in 0.56f..0.76f -> CustomerType.STUDENT
            else -> CustomerType.FAMILY
        }

        // 2. Select Preset fitting the customer archetype
        val preset = when (customerType) {
            CustomerType.STUDENT -> listOf(OrderPreset.ONLY_FALAFEL, OrderPreset.FALAFEL_SALAD, OrderPreset.FALAFEL_SPICY).random()
            CustomerType.TOURIST -> listOf(OrderPreset.ONLY_FALAFEL, OrderPreset.FALAFEL_TAHINI, OrderPreset.FALAFEL_CHIPS).random()
            CustomerType.BUSINESSMAN -> listOf(OrderPreset.FALAFEL_COMPLETE, OrderPreset.DELUXE_MASTERY).random()
            CustomerType.CRITIC -> listOf(OrderPreset.FALAFEL_COMPLETE, OrderPreset.DELUXE_MASTERY, OrderPreset.FALAFEL_SPICY).random()
            CustomerType.CELEBRITY -> listOf(OrderPreset.FALAFEL_COMPLETE, OrderPreset.DELUXE_MASTERY).random()
            CustomerType.FAMILY -> listOf(OrderPreset.FALAFEL_COMPLETE, OrderPreset.FALAFEL_SALAD, OrderPreset.FALAFEL_CHIPS).random()
        }
        
        val firstName = firstNames.random()
        val isVip = customerType.isVip || (customerType == CustomerType.CRITIC)
        val tipScale = if (customerType.isVip) 3.0f else (if (isRush) 1.5f else 1.0f)
        val nameWithLabel = if (isVip) "👑 $firstName" else firstName

        // Max patience in seconds
        val patientLevelMultiplier = 1.0f + (_uiState.value.saveState.patienceUpgradeLevel * 0.20f)
        val basePatience = (10 + Random.nextInt(8)) * patientLevelMultiplier

        val newCustomer = GameCustomer(
            name = nameWithLabel,
            avatar = customerType.avatar,
            description = customerType.description,
            phrase = customerType.basePhrase,
            requiredOrder = preset.ingredients,
            orderPreset = preset,
            maxPatienceSec = basePatience,
            currentPatience = 1.0f,
            isVip = isVip,
            tipScale = tipScale,
            type = customerType
        )

        _uiState.update {
            it.copy(
                activeCustomers = it.activeCustomers + newCustomer,
                feedbackMessage = "[${customerType.typeName}] ${newCustomer.name}: \"${newCustomer.phrase}\""
            )
        }
    }

    // Active Pita Preparation station taps
    fun tapAddIngredient(ingredient: Ingredient) {
        val currentList = _uiState.value.preparedIngredients
        
        // Cannot add ingredients if double tapping or full pita
        if (currentList.size >= 8) {
            triggerScreenShake(3f)
            return
        }

        // Automatic tahini/automated machine dispenser mechanics! 
        val hasAutoSauce = _uiState.value.saveState.autoSauceUpgradeLevel > 0
        val isAddingFalafel = ingredient == Ingredient.FALAFEL
        
        val updatedList = mutableListOf<Ingredient>().apply {
            addAll(currentList)
            add(ingredient)
            // Auto add Tahini dispenser
            if (isAddingFalafel && hasAutoSauce && !currentList.contains(Ingredient.TAHINI)) {
                add(Ingredient.TAHINI)
            }
        }

        _uiState.update { it.copy(preparedIngredients = updatedList) }
        showFeedback("נוסף למנה: ${ingredient.displayName} ${ingredient.emoji}")
        
        // Particle burst at bottom cooking locations
        triggerParticleBurst(
            x = when(ingredient) {
                Ingredient.PITA -> 100f
                Ingredient.FALAFEL -> 200f
                Ingredient.SALAD -> 300f
                Ingredient.TAHINI -> 400f
                Ingredient.CHIPS -> 500f
                Ingredient.SPICY -> 600f
            },
            y = 800f,
            amount = 8,
            isCoin = false,
            emoji = ingredient.emoji
        )
    }

    // Empty the pita pocket (Waste mechanics)
    fun tapTrashPita() {
        val prepared = _uiState.value.preparedIngredients
        if (prepared.isEmpty()) return
        
        val activeEvent = _uiState.value.activeEvent
        val penalty = activeEvent.trashPenalty

        _uiState.update {
            val netPremium = (it.revenueEarnedToday - penalty).coerceAtLeast(0)
            it.copy(
                preparedIngredients = emptyList(),
                failedCountToday = it.failedCountToday + 1,
                comboStreak = 0,
                revenueEarnedToday = netPremium,
                feedbackMessage = if (penalty > 0) {
                    "אוי לא! פקח הבריאות ראה אותך זורק אוכל! קנס של $penalty- מטבעות! 🤬"
                } else {
                    "אופס! המנה נזרקה לפח האשפה 🗑️"
                }
            )
        }
        
        triggerScreenShake(8f)
        triggerParticleBurst(450f, 750f, amount = 15, isCoin = false, emoji = "💥")
    }

    // Submit assembled pita to first customer in query queue
    fun tapServePita() {
        val prepared = _uiState.value.preparedIngredients
        val customers = _uiState.value.activeCustomers

        if (customers.isEmpty()) {
            triggerScreenShake(4f)
            showFeedback("אין אף לקוח בתור להגיש לו! 📭")
            return
        }

        val targetCustomer = customers.first() // oldest customer in the stack
        
        // Check if recipe is accurate. To be accurate, ALL required ingredients must be present,
        // and no extra useless ingredients (though order of tapping can be dynamic for custom gameplay!)
        val required = targetCustomer.requiredOrder.toSet()
        val generated = prepared.toSet()

        if (required == generated) {
            // Recipe matches! Compute earnings!
            val marketingMultiplier = 1.0f + (_uiState.value.saveState.marketingUpgradeLevel * 0.25f)
            val recipePremium = _uiState.value.saveState.priceUpgradeLevel * 4 // +4 coins per level
            
            // Core calculations
            val basePay = targetCustomer.orderPreset.baseValue + recipePremium
            val speedBonus = if (targetCustomer.currentPatience > 0.6f) 15 else 0
            val premiumVipTip = if (targetCustomer.isVip) 35 else 0
            val isRush = _uiState.value.isRushHour
            val rushMultiplier = if (isRush) 1.5f else 1.0f
            val eventMultiplier = _uiState.value.activeEvent.priceModifier
            
            val totalEarned = ((basePay + speedBonus + premiumVipTip) * targetCustomer.tipScale * targetCustomer.type.tipMultiplier * marketingMultiplier * rushMultiplier * eventMultiplier).toInt()
            
            val newStreak = _uiState.value.comboStreak + 1
            
            _uiState.update {
                it.copy(
                    activeCustomers = customers.drop(1), // remove served
                    preparedIngredients = emptyList(), // clear workspace
                    servedCountToday = it.servedCountToday + 1,
                    revenueEarnedToday = it.revenueEarnedToday + totalEarned,
                    comboStreak = newStreak,
                    feedbackMessage = "סרביס מושלם! ${targetCustomer.name} נהנה ונתן טיפ שווה של $totalEarned מטבעות! 💰"
                )
            }

            // Explode real physics-based particles matching customer type!
            triggerScreenShake(3f)
            val particleEmoji = when (targetCustomer.type) {
                CustomerType.CELEBRITY -> "👑"
                CustomerType.CRITIC -> "🧐"
                CustomerType.BUSINESSMAN -> "💸"
                CustomerType.TOURIST -> "🎒"
                else -> "🪙"
            }
            triggerParticleBurst(300f, 400f, amount = 18, isCoin = true, emoji = particleEmoji)
            
            // Auto spin-up immediate replacements
            if (_uiState.value.activeCustomers.isEmpty()) {
                spawnCustomer()
            }
        } else {
            // Bad mix! Customer gets disappointed, but continues waiting!
            triggerScreenShake(9f)
            triggerParticleBurst(300f, 400f, amount = 10, isCoin = false, emoji = "🤮")
            
            _uiState.update {
                it.copy(
                    preparedIngredients = emptyList(), // clear and lose ingredients
                    failedCountToday = it.failedCountToday + 1,
                    comboStreak = 0,
                    feedbackMessage = "מתכון לא נכון! ${targetCustomer.name} מעקם את האף: \"זה לא מה שהזמנתי!\" ❌"
                )
            }
        }
    }

    // Process gameplay day conclusion, saving scores and generating local reviews
    private fun endGameplayDay() {
        stopDayJobs()

        val todayServed = _uiState.value.servedCountToday
        val todayFailed = _uiState.value.failedCountToday
        val todayLeft = _uiState.value.impatientLeftToday
        val todayCoins = _uiState.value.revenueEarnedToday
        val currentDayNum = _uiState.value.saveState.currentDay

        _uiState.update { it.copy(currentScreen = GameScreen.DAY_SUMMARY) }

        viewModelScope.launch {
            // Save newly-earned cash directly inside Room Database!
            val updatedSave = _uiState.value.saveState.copy(
                totalCoins = _uiState.value.saveState.totalCoins + todayCoins,
                currentDay = currentDayNum + 1
            )
            repository.updateSaveState(updatedSave)

            // Fully-offline Hebrew review generation based on today's performance
            val reviewList = LocalReviewGenerator.generateReviews(
                day = currentDayNum,
                servedCount = todayServed,
                errorCount = todayFailed,
                leftCount = todayLeft,
                coinsEarned = todayCoins
            )

            // Persist reviews inside Room
            reviewList.forEach { review ->
                repository.insertReview(review)
            }

            _uiState.update { it.copy(generatedTodayReviews = reviewList) }
        }
    }

    // Particle Spray generator physics
    private fun triggerParticleBurst(x: Float, y: Float, amount: Int, isCoin: Boolean, emoji: String) {
        val currentParticles = _uiState.value.particles.toMutableList()
        val random = Random
        
        repeat(amount) {
            val vx = (random.nextFloat() - 0.5f) * 16f
            val vy = -(random.nextFloat() * 12f + 4f)
            currentParticles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    size = (random.nextFloat() * 20f + 16f),
                    color = if (isCoin) 0xFFFFEE58 else 0xFFEF5350,
                    isCoin = isCoin,
                    emoji = emoji
                )
            )
        }
        
        _uiState.update { it.copy(particles = currentParticles.take(60)) } // Limit particles to preserve 2026 rendering latency
    }

    private fun startParticleTickEngine() {
        particlePhysicsJob?.cancel()
        particlePhysicsJob = viewModelScope.launch {
            while (true) {
                delay(16) // tick every ~16ms (60 FPS visual smoothness)
                val current = _uiState.value.particles
                if (current.isEmpty()) continue
                
                val moved = current.mapNotNull { particle ->
                    val newX = particle.x + particle.vx
                    // Add smooth gravity pulling particles downwards!
                    val newVy = particle.vy + 0.6f 
                    val newY = particle.y + newVy
                    val newRot = particle.rotation + 5f
                    
                    // Kill particle if goes off screen
                    if (newY > 1200f || newX < -100f || newX > 800f) {
                        null
                    } else {
                        particle.copy(
                            x = newX,
                            y = newY,
                            vy = newVy,
                            rotation = newRot
                        )
                    }
                }
                
                // Decay the screen shake value slightly too for cohesive spring return
                val currentShake = _uiState.value.screenShake
                val nextShake = if (currentShake > 0.1f) currentShake * 0.85f else 0f

                _uiState.update {
                    it.copy(
                        particles = moved,
                        screenShake = nextShake
                    )
                }
            }
        }
    }

    private fun triggerScreenShake(mag: Float) {
        _uiState.update { it.copy(screenShake = mag) }
    }

    override fun onCleared() {
        stopDayJobs()
        particlePhysicsJob?.cancel()
        super.onCleared()
    }
}
