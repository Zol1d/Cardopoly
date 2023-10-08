package cool.zolid.cardopoly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.BuildConfig
import cool.zolid.cardopoly.NFCCardColorBindings
import cool.zolid.cardopoly.NFCEnabled
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.globalSettingsDialogOpen
import cool.zolid.cardopoly.ui.ScreenItem
import cool.zolid.cardopoly.ui.ScreenSelector
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography

@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(snackbarHost = { Snackbar.Host() }) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(extraPadding(pv)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cardopoly",
                    style = Typography.headlineLarge,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(top = 40.dp),
                    fontWeight = FontWeight.Bold
                )
                ScreenSelector(
                    rowList = listOf(
                        listOf(
                            ScreenItem(
                                "startgame?cards_enabled=true",
                                "Jauna spēle ar kartēm",
                                R.drawable.play_circle,
                                NFCEnabled && NFCCardColorBindings.size > 1
                            )
                        ),
                        listOf(
                            ScreenItem(
                                "startgame?cards_enabled=false",
                                "Jauna spēle bez kartēm",
                                R.drawable.play_circle
                            )
                        ),
                        listOf(ScreenItem("cards", "Kartes", R.drawable.cards, NFCEnabled)),
                        listOf(ScreenItem(null, "Iestatījumi", R.drawable.settings, onClick = { globalSettingsDialogOpen = true }))
                    ),
                    navController = navController,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    "Izstrādāja Zolids īstiem Monopoly faniem",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
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