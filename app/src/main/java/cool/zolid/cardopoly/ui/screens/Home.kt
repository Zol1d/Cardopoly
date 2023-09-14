package cool.zolid.cardopoly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.BuildConfig
import cool.zolid.cardopoly.NFCEnabled
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.ui.ScreenItem
import cool.zolid.cardopoly.ui.ScreenSelector
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.extraPadding

@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(topBar = {
        StandardTopAppBar(title = "Cardopoly", null)
    }, snackbarHost = { Snackbar.Host() }) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(extraPadding(pv)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ScreenSelector(
                rowList = listOf(
                    listOf(ScreenItem(
                        "startgame?cards_enabled=true",
                        "Jauna spēle ar kartēm",
                        R.drawable.play_circle,
                        NFCEnabled
                    )),
                    listOf(ScreenItem(
                        "startgame?cards_enabled=false",
                        "Jauna spēle bez kartēm",
                        R.drawable.play_circle
                    )),
                    listOf(ScreenItem("cards", "Kartes", R.drawable.cards, NFCEnabled)),
                ), navController = navController, modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    "Cardopoly versija ${BuildConfig.VERSION_NAME}, būvējums #${BuildConfig.VERSION_CODE}",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = Color.Gray
                )
            }
        }
    }
}