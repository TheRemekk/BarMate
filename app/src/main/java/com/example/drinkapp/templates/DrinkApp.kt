package com.example.drinkapp.templates

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.em
import com.example.drinkapp.DrinkListViewModel
import com.example.drinkapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkApp(title: String, drinkListViewModel: DrinkListViewModel, content: @Composable () -> Unit) {
    val topAppBarColor = colorResource(id = R.color.blue)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            title,
                            color = Color.White,
                            fontSize = 5.em,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { drinkListViewModel.toggleSheet()  },
                            modifier = Modifier
                                .size(100.dp)
                                .padding(start = 8.dp, bottom = 4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = "Logo",
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topAppBarColor
                    ),
                )
                HorizontalDivider(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth(),
                    color = Color.Black,
                    thickness = 4.dp
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()

            val isSheetOpen = drinkListViewModel.getSheet()
            if (isSheetOpen) {
                ModalBottomSheet(
                    onDismissRequest = { drinkListViewModel.toggleSheet() },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "🍹 Aplikacja barmańska \"DrinkApp\"",
                            fontSize = 6.em
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Column {
                            Text(
                                text = "Aplikacja służąca do ułatwienia pracy barmana.",
                                fontSize = 4.em
                            )
                            Text(
                                text = "Tutaj znajdziesz przepisy na drinki, timer do mieszania koktajli i wiele więcej!",
                                fontSize = 4.em
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                        Button(
                            onClick = { drinkListViewModel.toggleSheet() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Zamknij")
                        }
                    }
                }
            }
        }
    }
}