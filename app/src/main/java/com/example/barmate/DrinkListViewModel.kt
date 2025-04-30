package com.example.barmate

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.barmate.data.Drink
import com.example.barmate.repositories.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DrinkListViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app.applicationContext)

    // Wprowadzanie drinków do bazy danych w przypadku ich braku podczas inicjalizacji
    init {
        viewModelScope.launch {
//            repo.deleteAll()
            loadAllDrinks()
        }
    }
    // Pasek wyszukiwania drinków

    var searchQuery by mutableStateOf("")
        private set

    var isOnlyFavorites by mutableStateOf(false)
        private set

    private val _filteredDrinks = MutableStateFlow<List<Drink>>(emptyList())
    val filteredDrinks: StateFlow<List<Drink>> = _filteredDrinks.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        reloadDrinks()
    }

    fun onToggleFavorites(onlyFav: Boolean) {
        isOnlyFavorites = onlyFav
        reloadDrinks()
    }

    private fun loadAllDrinks() {
        viewModelScope.launch {
            var drinks = getDrinks().first()
            if (drinks.isEmpty()) {
                populateDatabase()
            }

            reloadDrinks()
        }
    }

    // Aktualizowanie listy drinków w zależności od wyszukiwanego tekstu
    private fun reloadDrinks() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            repo.findDrinksByQuery("%$searchQuery%")
                .collect { drinks ->
                    _filteredDrinks.value = if (isOnlyFavorites) {
                        drinks.filter { it.isFavourite == 1 }
                    } else {
                        drinks
                    }
                }
        }
    }

    // Nawigacja pomiędzy okienkami
    var selectedGui by mutableStateOf("listOfDrinks")
        private set

    fun navigateBackToList() {
        selectedGui = "listOfDrinks"
    }

    fun navigateToShaker() {
        selectedGui = "drinkShakingCounter"
    }

    fun navigateBackToDetail() {
        selectedGui = "drinkDetail"
        resetTimer()
    }

    // Zarządzanie rozwijanym ModalBottomSheet (pop-up)
    var isSheetOpen by mutableStateOf(false)
        private set

    fun getSheet(): Boolean {
        return isSheetOpen
    }

    fun toggleSheet() {
        isSheetOpen = !isSheetOpen
    }

    // Ustawianie konkretnego drinka
    var selectedDrink by mutableStateOf<Drink?>(null)
        private set

    fun selectDrink(drink: Drink?) {
        selectedDrink = drink
        if (drink != null) {
            selectedGui = "drinkDetail"
            initializeTimer(drink.shakingTime)
        }
    }

    // Pobieranie drinku po nazwie
    suspend fun getDrinkByName(name: String): Drink? {
        return repo.getDrinkByName(name)
    }

    // Funkcja do pobierania listy wszystkich drinków
    fun getDrinks(): Flow<List<Drink>> {
        return repo.getAll()
    }

    // Ustawianie polubienia na konkretnym drinku
    fun toggleFavourite(drink: Drink) {
        viewModelScope.launch {
            val updatedDrink = drink.copy(isFavourite = if (drink.isFavourite == 1) 0 else 1)
            repo.updateDrink(updatedDrink)
        }
    }

    // Ustawianie alertu z informacją o ukończeniu tworzenia drinka
    var showDialog by mutableStateOf(false)
        private set

    fun toggleDialog() {
        showDialog = !showDialog
    }

    // Zarządzanie timerem (licznikiem czasu mieszania wybranego drinka)
    var initialTimerTime by mutableIntStateOf(0)
        private set

    var timerTimeLeft by mutableIntStateOf(0)
        private set

    var isTimerRunning by mutableStateOf(false)
        private set

    var hasTimerStarted by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    fun initializeTimer(time: Int) {
        initialTimerTime = time
        resetTimer()
    }

    fun startTimer(time: Int) {
        if (initialTimerTime == time) {
            hasTimerStarted = true
        }

        timerJob?.cancel()
        isTimerRunning = true

        timerJob = viewModelScope.launch {
            while (timerTimeLeft > 0) {
                delay(1000L)
                timerTimeLeft -= 1
            }

            isTimerRunning = false
            hasTimerStarted = false
            toggleDialog()
            resetTimer()
        }
    }

    fun stopTimer() {
        isTimerRunning = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        stopTimer()
        timerTimeLeft = initialTimerTime
        hasTimerStarted = false
    }

    // Funkcja wypełniająca bazę danych drinkami
    private fun populateDatabase() {
        val drinks = listOf(
            Drink(
                name = "Margarita",
                ingredients = "50ml Tequila, 20ml Triple Sec, 20ml Sok z limonki",
                desc = "Wstrząśnij wszystkie składniki z lodem, odcedź do schłodzonego kieliszka koktajlowego, opcjonalnie z solą na rancie.",
                imageResId = R.drawable.margarita,
                shakingTime = 40,
            ),
            Drink(
                name = "Mojito",
                ingredients = "40ml Rum, 6 listków Mięty, 20ml Sok z limonki, Soda",
                desc = "Rozgnieć miętę i limonkę w szklance, dodaj rum i kruszony lód, dopełnij wodą sodową i delikatnie wymieszaj.",
                imageResId = R.drawable.mojito,
                shakingTime = 20
            ),
            Drink(
                name = "Cosmopolitan",
                ingredients = "40ml Wódka, 15ml Triple Sec, 30ml Sok żurawinowy, 10ml Sok z limonki",
                desc = "Wstrząśnij wszystkie składniki z lodem i odcedź do schłodzonego kieliszka koktajlowego, udekoruj skórką z cytryny.",
                imageResId = R.drawable.cosmopolitan,
                shakingTime = 40
            ),
            Drink(
                name = "Sex on the Beach",
                ingredients = "40ml Wódka, 20ml Likier brzoskwiniowy, 40ml Sok żurawinowy, 40ml Sok pomarańczowy",
                desc = "Wstrząśnij wódkę i likier z lodem, odcedź do szklanki z lodem, dopełnij sokami i delikatnie wymieszaj.",
                imageResId = R.drawable.sex_on_the_beach,
                shakingTime = 30
            ),
            Drink(
                name = "Pina Colada",
                ingredients = "50ml Rum biały, 30ml Likier kokosowy, 50ml Sok ananasowy, 20ml Śmietanka kokosowa",
                desc = "Wstrząśnij wszystkie składniki z lodem i odcedź do szklanki typu hurricane wypełnionej lodem.",
                imageResId = R.drawable.pina_colada,
                shakingTime = 35
            ),
            Drink(
                name = "Tequila Sunrise",
                ingredients = "50ml Tequila, 100ml Sok pomarańczowy, 10ml Grenadyna",
                desc = "Wlej tequilę i sok do szklanki z lodem, delikatnie dodaj grenadynę, pozwalając jej opaść na dno.",
                imageResId = R.drawable.tequila_sunrise,
                shakingTime = 10
            ),
            Drink(
                name = "Long Island Iced Tea",
                ingredients = "20ml Wódka, 20ml Tequila, 20ml Rum biały, 20ml Gin, 20ml Triple Sec, 20ml Sok z cytryny, Cola",
                desc = "Wstrząśnij wszystkie alkohole i sok z lodem, odcedź do szklanki z lodem, dopełnij colą i delikatnie wymieszaj.",
                imageResId = R.drawable.long_island_iced_tea,
                shakingTime = 30
            ),
            Drink(
                name = "Gin and Tonic",
                ingredients = "50ml Gin, 150ml Tonik, Plasterek cytryny",
                desc = "Wlej gin do szklanki z lodem, dopełnij tonikiem, delikatnie wymieszaj i udekoruj plasterkiem cytryny.",
                imageResId = R.drawable.gin_and_tonic,
                shakingTime = 5
            ),
            Drink(
                name = "Cuba Libre",
                ingredients = "50ml Rum, 120ml Cola, 10ml Sok z limonki",
                desc = "Wlej rum do szklanki z lodem, dodaj sok z limonki, dopełnij colą i delikatnie wymieszaj.",
                imageResId = R.drawable.cuba_libre,
                shakingTime = 5
            ),
            Drink(
                name = "Whiskey Sour",
                ingredients = "50ml Whiskey, 20ml Sok z cytryny, 20ml Syrop cukrowy",
                desc = "Wstrząśnij wszystkie składniki z lodem i odcedź do szklanki z lodem, opcjonalnie udekoruj wisienką.",
                imageResId = R.drawable.whiskey_sour,
                shakingTime = 30
            ),
            Drink(
                name = "Blue Lagoon",
                ingredients = "50ml Wódka, 20ml Blue Curaçao, 100ml Sprite",
                desc = "Wstrząśnij wódkę i Blue Curaçao z lodem, odcedź do szklanki z lodem, dopełnij Spritem i delikatnie wymieszaj.",
                imageResId = R.drawable.blue_lagoon,
                shakingTime = 20
            ),
            Drink(
                name = "Aperol Spritz",
                ingredients = "60ml Aperol, 90ml Prosecco, 30ml Woda sodowa",
                desc = "Wlej Aperol i Prosecco do kieliszka z lodem, dopełnij wodą sodową i delikatnie wymieszaj.",
                imageResId = R.drawable.aperol_spritz,
                shakingTime = 10
            ),
            Drink(
                name = "Old Fashioned",
                ingredients = "50ml Bourbon, 1 Kostka cukru, 2 Dashes Angostura Bitters, Odrobina wody",
                desc = "Rozpuść cukier z bittersami i wodą w szklance, dodaj lód i bourbon, delikatnie wymieszaj.",
                imageResId = R.drawable.old_fashioned,
                shakingTime = 20
            ),
            Drink(
                name = "Negroni",
                ingredients = "30ml Gin, 30ml Campari, 30ml Słodki wermut",
                desc = "Wlej wszystkie składniki do szklanki z lodem i delikatnie wymieszaj, udekoruj skórką pomarańczy.",
                imageResId = R.drawable.negroni,
                shakingTime = 15
            )
        )

        CoroutineScope(viewModelScope.coroutineContext).launch {
            repo.insertAll(drinks)
        }
    }
}