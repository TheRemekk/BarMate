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
                DrinkApp(title = "DrinkApp") {
                    val configuration = LocalConfiguration.current
                    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

                    if (isPortrait) {
                        when (drinkListViewModel.selectedGui) {
                            "listOfDrinks" -> DrinkList(drinkListViewModel, isPortrait, windowSizeClass) { drink ->
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
                    } else {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.weight(0.4f)) {
                                DrinkList(drinkListViewModel, isPortrait, windowSizeClass) {
                                    drinkListViewModel.selectDrink(it)
                                }
                            }

                            Column(modifier = Modifier.weight(0.6f),
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
                DrinkItem(drink = drink, isPortrait, windowSizeClass, onClick = { onItemClicked(drink) })
            }
        }
    }
}


@Composable
fun DrinkItem(drink: Drink, isPortrait: Boolean, windowSizeClass: WindowSizeClass, onClick: (Drink) -> Unit) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
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
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = drink.name, fontSize = 4.5.em, color = Color.Black)
            }

            Image(
                painter = painterResource(id = drink.imageResId),
                contentDescription = drink.name,
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
        val bullet = "\u2022"

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Column {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = drink.name, fontSize = 8.em,  color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(40.dp))

                            Text(text = "Składniki", fontSize = 5.em)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                drinkIngredientsList.forEach { ingredient ->
                                    Text(
                                        text = "$bullet $ingredient",
                                        fontSize = 3.5.em
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(28.dp))

                            Text(text = "Opis", fontSize = 5.em)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = drink.desc, fontSize = 3.5.em)
                            Spacer(modifier = Modifier.height(28.dp))

                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = { onTimerClick(drink.shakingTime) },
                                    modifier = Modifier.fillMaxWidth(0.5f)
                                ) {
                                    Text(text = "⏳ Timer (${drink.shakingTime}s)")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Wróć do listy")
                }
            }
        } else {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = drink.name,
                    fontSize = 8.em,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Składniki", fontSize = 5.em)
                        Spacer(modifier = Modifier.height(8.dp))
                        drinkIngredientsList.forEach { ingredient ->
                            Text(
                                text = "$bullet $ingredient",
                                fontSize = 3.5.em
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Opis", fontSize = 5.em)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = drink.desc,
                            fontSize = 3.5.em
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Button(onClick = { onTimerClick(drink.shakingTime) }) {
                        Text("⏳ Timer (${drink.shakingTime}s)")
                    }
                }
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
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (!drinkListViewModel.hasCounterStarted) {
                                        "▶\uFE0F Start"
                                    } else {
                                        if (!drinkListViewModel.isRunning) "⏯\uFE0F Wznów" else "⏸\uFE0F Stop"
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                drinkListViewModel.resetTimer(shakingTime)
                                drinkListViewModel.setCounterStart(false)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }

            Button(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Powrót")
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
                        fontSize = 20.em
                    )

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
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (!drinkListViewModel.hasCounterStarted) {
                                        "▶\uFE0F Start"
                                    } else {
                                        if (!drinkListViewModel.isRunning) "⏯\uFE0F Wznów" else "⏸\uFE0F Stop"
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                drinkListViewModel.resetTimer(shakingTime)
                                drinkListViewModel.setCounterStart(false)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
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