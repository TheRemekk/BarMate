package com.example.drinkapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.drinkapp.data.Drink
import com.example.drinkapp.templates.DrinkApp
import com.example.drinkapp.ui.theme.DrinkAppTheme
import com.example.drinkapp.ui.theme.digitalFontFamily


class MainActivity : ComponentActivity() {
    private val drinkListViewModel by viewModels<DrinkListViewModel>()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            DrinkAppTheme {
                DrinkApp(title = "DrinkApp", drinkListViewModel) {
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
                                        onTimerClick = { time ->
                                            drinkListViewModel.navigateToShaker(time)
                                        }
                                    )

                                    "drinkShakingCounter" -> DrinkShakingCounter(
                                        drinkListViewModel,
                                        isPortrait,
                                        shakingTime = drinkListViewModel.timeCounter,
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
                                        onTimerClick = { time ->
                                            drinkListViewModel.navigateToShaker(
                                                time
                                            )
                                        },
                                    )

                                    "drinkShakingCounter" -> DrinkShakingCounter(
                                        drinkListViewModel = drinkListViewModel,
                                        isPortrait,
                                        shakingTime = drinkListViewModel.timeCounter,
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
    val drinks = drinkListViewModel.getDrinks().collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(drinks.value, key = { it.uid }) { drink ->
                DrinkItem(
                    drink = drink,
                    drinkListViewModel = drinkListViewModel,
                    isPortrait,
                    windowSizeClass,
                    onClick = { onItemClicked(drink) })
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
                    .padding(start = 8.dp, bottom = 4.dp)
            ) {
                if(drink.isFavourite == 1) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_fav_star),
                        contentDescription = "Logo",
                    )
                }
                else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_fav_empty_star),
                        contentDescription = "Logo",
                    )
                }
            }

            Text(
                text = drink.name,
                fontSize = 4.5.em, color = Color.Black,
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
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = drink.name,
                                    fontSize = 9.em,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
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
                Text(
                    text = drink.name,
                    fontSize = 9.em,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
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
    drinkListViewModel: DrinkListViewModel,
    isPortrait: Boolean,
    shakingTime: Int,
    onBackClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

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
                    val primaryColor = MaterialTheme.colorScheme.primary

                    val time: Int = drinkListViewModel.timeLeft
                    val minutes: Int = time / 60
                    val seconds: Int = time % 60

                    ShakerAnimation(drinkListViewModel.isRunning)
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        fontFamily = digitalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        fontSize = 20.em
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .padding(16.dp)
                    ) {
                        if (drinkListViewModel.timeLeft != 0) {
                            Button(
                                onClick = {
                                    if (!drinkListViewModel.hasCounterStarted && drinkListViewModel.timeLeft == shakingTime) {
                                        drinkListViewModel.setCounterStart(true)
                                    }
                                    drinkListViewModel.toggleTimer()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!drinkListViewModel.isRunning) primaryColor else Color.Red,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = if (!drinkListViewModel.hasCounterStarted) {
                                        "▶\uFE0F Start"
                                    } else {
                                        if (!drinkListViewModel.isRunning) "⏯\uFE0F Wznów" else "⏸\uFE0F Stop"
                                    }, fontSize = 5.em
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                drinkListViewModel.resetTimer(shakingTime)
                                drinkListViewModel.setCounterStart(false)
                            },
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text("Reset", fontSize = 5.em)
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
                    Text(text = "Powrót", fontSize = 5.em)
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

                val time: Int = drinkListViewModel.timeLeft
                val minutes: Int = time / 60
                val seconds: Int = time % 60

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.3f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ShakerAnimation(drinkListViewModel.isRunning)
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
                        if (drinkListViewModel.timeLeft != 0) {
                            Button(
                                onClick = {
                                    if (!drinkListViewModel.hasCounterStarted && drinkListViewModel.timeLeft == shakingTime) {
                                        drinkListViewModel.setCounterStart(true)
                                    }
                                    drinkListViewModel.toggleTimer()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!drinkListViewModel.isRunning) primaryColor else Color.Red,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = if (!drinkListViewModel.hasCounterStarted) {
                                        "▶\uFE0F Start"
                                    } else {
                                        if (!drinkListViewModel.isRunning) "⏯\uFE0F Wznów" else "⏸\uFE0F Stop"
                                    }, fontSize = 5.em
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                drinkListViewModel.resetTimer(shakingTime)
                                drinkListViewModel.setCounterStart(false)
                            },
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text("Reset", fontSize = 5.em)
                        }
                    }
                }
            }
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