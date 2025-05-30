package com.example.barmate

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.barmate.data.Drink
import com.example.barmate.templates.BarMateTopBar
import com.example.barmate.ui.theme.BarMateTheme
import com.example.barmate.ui.theme.digitalFontFamily

class MainActivity : ComponentActivity() {
    private val drinkListViewModel by viewModels<DrinkListViewModel>()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            BarMateTheme {
                BarMateTopBar(drinkListViewModel) {
                    val configuration = LocalConfiguration.current
                    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

                    if (isPortrait) {
                        val width = when (windowSizeClass.widthSizeClass) {
                            WindowWidthSizeClass.Compact -> 1f
                            WindowWidthSizeClass.Medium -> 0.7f
                            WindowWidthSizeClass.Expanded -> 0.7f
                            else -> 1f
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(width)
                                    .align(Alignment.Center),
                            ) {
                                when (drinkListViewModel.selectedGui) {
                                    "listOfDrinks" -> DrinkList(
                                        drinkListViewModel,
                                        isPortrait,
                                        windowSizeClass
                                    ) { drink ->
                                        drinkListViewModel.selectDrink(drink)
                                    }

                                    "drinkDetail" -> DrinkDetail(
                                        drink = drinkListViewModel.selectedDrink,
                                        isPortrait,
                                        onBackClick = {
                                            drinkListViewModel.navigateBackToList()
                                            drinkListViewModel.selectDrink(null)
                                        },
                                        onTimerClick = { drinkListViewModel.navigateToShaker() }
                                    )

                                    "drinkShakingCounter" -> DrinkShakingCounter(
                                        drink = drinkListViewModel.selectedDrink,
                                        drinkListViewModel,
                                        isPortrait,
                                        onBackClick = { drinkListViewModel.navigateBackToDetail() }
                                    )
                                }
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.weight(0.4f)) {
                                DrinkList(drinkListViewModel, isPortrait, windowSizeClass) {
                                    drinkListViewModel.selectDrink(it)
                                }
                            }

                            Column(
                                modifier = Modifier.weight(0.6f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when (drinkListViewModel.selectedGui) {
                                    "drinkDetail" -> DrinkDetail(
                                        drink = drinkListViewModel.selectedDrink,
                                        isPortrait,
                                        onBackClick = { drinkListViewModel.selectDrink(null) },
                                        onTimerClick = { drinkListViewModel.navigateToShaker() }
                                    )

                                    "drinkShakingCounter" -> DrinkShakingCounter(
                                        drink = drinkListViewModel.selectedDrink,
                                        drinkListViewModel = drinkListViewModel,
                                        isPortrait,
                                        onBackClick = { drinkListViewModel.navigateBackToDetail() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DrinkList(
    drinkListViewModel: DrinkListViewModel,
    isPortrait: Boolean,
    windowSizeClass: WindowSizeClass,
    onItemClicked: (Drink) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        TextField(
            value = drinkListViewModel.searchQuery,
            onValueChange = { query -> drinkListViewModel.onSearchQueryChanged(query) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Wyszukaj drink") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (drinkListViewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { drinkListViewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                    IconButton(onClick = {
                        drinkListViewModel.onToggleFavorites(!drinkListViewModel.isOnlyFavorites)
                    }) {
                        val icon = if (drinkListViewModel.isOnlyFavorites) {
                            painterResource(id = R.drawable.ic_fav_star)
                        } else {
                            painterResource(id = R.drawable.ic_fav_empty_star)
                        }

                        Icon(painter = icon, contentDescription = "Favorites Filter")
                    }
                }
            }
        )

        val drinks = drinkListViewModel.filteredDrinks.collectAsState(initial = emptyList())

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(drinks.value, key = { it.uid }) { drink ->
                DrinkItem(
                    drink = drink,
                    drinkListViewModel = drinkListViewModel,
                    isPortrait,
                    windowSizeClass,
                    onClick = { onItemClicked(drink)  })
            }
        }
    }
}


@Composable
fun DrinkItem(
    drink: Drink,
    drinkListViewModel: DrinkListViewModel,
    isPortrait: Boolean,
    windowSizeClass: WindowSizeClass,
    onClick: (Drink) -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val heightDivider = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> if (isPortrait) 8 else 4
        WindowWidthSizeClass.Medium -> if (isPortrait) 10 else 6
        WindowWidthSizeClass.Expanded -> if (isPortrait) 10 else 6
        else -> 8
    }

    val heightOfCards = screenHeight / heightDivider
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightOfCards)
            .padding(8.dp)
            .clickable { onClick(drink) },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7BA8DB)
        ),
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
        ) {
            IconButton(
                onClick = { drinkListViewModel.toggleFavourite(drink) },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(50.dp)
            ) {
                val icon = if (drink.isFavourite == 1) {
                    painterResource(id = R.drawable.ic_fav_star)
                } else {
                    painterResource(id = R.drawable.ic_fav_empty_star)
                }

                Image(
                    painter = icon,
                    contentDescription = "Favourite",
                )
            }

            Text(
                text = drink.name,
                fontSize = 4.em, color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )

            Image(
                painter = painterResource(id = drink.imageResId),
                contentDescription = drink.name,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
fun DrinkDetail(
    drink: Drink?,
    isPortrait: Boolean,
    onBackClick: () -> Unit,
    onTimerClick: (Int) -> Unit
) {
    if (drink != null) {
        val drinkIngredientsList = drink.ingredients.split(",").map { it.trim() }

        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Column {
                            Spacer(modifier = Modifier.height(screenHeight * 0.03f))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = drink.name,
                                        fontSize = 7.5.em,
                                        fontWeight = FontWeight.Bold,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )

                                    SendSmsFab(drink.ingredients)
                                }
                            }

                            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                            Text(text = "🍸 Składniki", fontSize = 5.5.em)
                            Spacer(modifier = Modifier.height(screenHeight * 0.007f))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                drinkIngredientsList.forEach { ingredient ->
                                    Text(
                                        text = "❖ $ingredient",
                                        fontSize = 3.75.em
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                            Text(text = "📖 Opis", fontSize = 5.5.em)
                            Spacer(modifier = Modifier.height(screenHeight * 0.007f))
                            Text(text = drink.desc, fontSize = 3.75.em)
                            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = { onTimerClick(drink.shakingTime) },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .padding(8.dp)
                                ) {
                                    Text(text = "⏳ Timer (${drink.shakingTime}s)", fontSize = 5.em)
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .padding(8.dp)
                    ) {
                        Text(text = "Powrót", fontSize = 5.em)
                    }
                }
            }
        } else {
            val scrollState = rememberScrollState()
            val isScrolled = scrollState.value > 0
            val canScrollMore = scrollState.value < scrollState.maxValue

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(screenHeight * 0.03f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = drink.name,
                            fontSize = 7.5.em,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        SendSmsFab(drink.ingredients)
                    }
                }
                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true) // <--- WAŻNE fill = true
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(end = 4.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "🍸 Składniki", fontSize = 5.em)
                            Spacer(modifier = Modifier.height(8.dp))
                            drinkIngredientsList.forEach { ingredient ->
                                Text(text = "❖ $ingredient", fontSize = 3.5.em)
                            }
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "📖 Opis", fontSize = 5.em)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = drink.desc, fontSize = 3.5.em)
                        }
                    }

                    if (isScrolled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .align(Alignment.TopCenter)
                        )
                    }

                    if (canScrollMore) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                                .align(Alignment.BottomCenter)
                        )
                    }
                }

                Button(
                    onClick = { onTimerClick(drink.shakingTime) },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "⏳ Timer (${drink.shakingTime}s)", fontSize = 5.em)
                }

                Spacer(
                    modifier = Modifier
                        .height(16.dp)
                        .weight(0.2f, fill = true)
                )
            }
        }
    }
}

@Composable
fun DrinkShakingCounter(
    drink: Drink?,
    drinkListViewModel: DrinkListViewModel,
    isPortrait: Boolean,
    onBackClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val primaryColor = MaterialTheme.colorScheme.primary

    val isRunning = drinkListViewModel.isTimerRunning
    val hasStarted = drinkListViewModel.hasTimerStarted
    val time: Int = drinkListViewModel.timerTimeLeft
    val minutes: Int = time / 60
    val seconds: Int = time % 60

    if (drink != null) {
        val buttonLabel = when {
            !hasStarted -> "▶\uFE0F Start"
            isRunning  -> "⏸\uFE0F Stop"
            else -> "⏯\uFE0F Wznów"
        }

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        ShakerAnimation(isRunning)
                        Spacer(modifier = Modifier.height(48.dp))
                        Text(
                            text = "%02d:%02d".format(minutes, seconds),
                            fontFamily = digitalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 20.em
                        )

                        Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .padding(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    when {
                                        !isRunning -> drinkListViewModel.startTimer(time)
                                        else -> drinkListViewModel.stopTimer()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isRunning) primaryColor else Color.Red,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(buttonLabel, fontSize = 4.5.em)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = {
                                    drinkListViewModel.resetTimer()
                                },
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text("Reset", fontSize = 4.5.em)
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp)
                    ) {
                        Text(text = "Powrót", fontSize = 4.5.em)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary

                    val minutes: Int = time / 60
                    val seconds: Int = time % 60

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.3f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ShakerAnimation(isRunning)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.7f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "%02d:%02d".format(minutes, seconds),
                            fontFamily = digitalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 24.em
                        )

                        Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Button(
                                onClick = {
                                    when {
                                        !isRunning -> drinkListViewModel.startTimer(time)
                                        else -> drinkListViewModel.stopTimer()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isRunning) primaryColor else Color.Red,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(buttonLabel, fontSize = 4.5.em)
                            }

                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = {
                                    drinkListViewModel.resetTimer()
                                },
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text("Reset", fontSize = 4.5.em)
                            }
                        }
                    }
                }
            }
        }
        if (drinkListViewModel.showDialog) {
            DrinkReadyAlert(drink, drinkListViewModel)
        }
    }
}

@Composable
fun ShakerAnimation(isRunning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "shaker_animation")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    val offsetX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    if (isRunning) {
        Image(
            painter = painterResource(id = R.drawable.ic_shaker),
            contentDescription = "Shaker",
            modifier = Modifier
                .size(256.dp)
                .graphicsLayer {
                    translationY = offsetY
                    translationX = offsetX
                    rotationZ = rotation
                }
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.ic_shaker),
            contentDescription = "Shaker",
            modifier = Modifier.size(256.dp)
        )
    }
}

@Composable
fun DrinkReadyAlert(
    drink: Drink,
    drinkListViewModel: DrinkListViewModel
) {
    AlertDialog(
        onDismissRequest = { drinkListViewModel.toggleDialog() },
        title = { Text("Gotowe!") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BouncingImageAnimation(drink, durationSeconds = 3)
            }
        },
        confirmButton = {
            TextButton(onClick = { drinkListViewModel.toggleDialog() }) {
                Text("OK")
            }
        }
    )
}

@Composable
fun BouncingImageAnimation(drink: Drink, durationSeconds: Int) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        val startTime = withFrameNanos { it }
        var elapsed: Long
        val totalDurationNanos = durationSeconds * 1_000_000_000L

        do {
            elapsed = withFrameNanos { it } - startTime
            val progress = elapsed / totalDurationNanos.toFloat()

            val dynamicTarget = 1.4f - (0.4f * progress).coerceIn(0f, 1f)

            scale.animateTo(
                targetValue = dynamicTarget,
                animationSpec = tween(150, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(150, easing = FastOutSlowInEasing)
            )
        } while (elapsed < totalDurationNanos)
    }

    Image(
        painter = painterResource(id = drink.imageResId),
        contentDescription = "Bouncing shaker",
        modifier = Modifier
            .size(150.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    )
}

@Composable
fun SendSmsFab(ingredients: String) {
    val context = LocalContext.current
    val message = "Składniki drinka:\n$ingredients"

    FloatingActionButton(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:")
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
        }
    ) {
        Icon(imageVector = Icons.Default.Send, contentDescription = "Wyślij SMS")
    }
}
