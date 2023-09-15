package cool.zolid.cardopoly.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.NFCCardColorBindings
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.navigateWithoutTrace
import cool.zolid.cardopoly.ui.AlertDialog
import cool.zolid.cardopoly.ui.NoRippleTheme
import cool.zolid.cardopoly.ui.ScreenItem
import cool.zolid.cardopoly.ui.ScreenSelector
import cool.zolid.cardopoly.ui.Shapes
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameScreen(navController: NavHostController) {
    var exitDialogOpen by remember { mutableStateOf(false) }
    if (exitDialogOpen) {
        AlertDialog(
            onDismissRequest = { exitDialogOpen = false },
            title = { Text("Vai tiešām beigt spēli?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Save game and display post-match popup
                        currentGame = null
                        navController.navigateWithoutTrace("home")
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = colorScheme.errorContainer
                    )
                ) {
                    Text("Apstiprināt".uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = { exitDialogOpen = false }) {
                    Text("Atcelt".uppercase())
                }
            }
        )
    }
    BackHandler {
        if (currentGame != null) {
            exitDialogOpen = true
        }
    }
    Scaffold(snackbarHost = { Snackbar.Host() }) { pv ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(extraPadding(pv, top = 10.dp)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LazyColumn(userScrollEnabled = true) {
                    itemsIndexed(currentGame?.players ?: listOf()) { _, player ->
                        CompositionLocalProvider(
                            LocalMinimumInteractiveComponentEnforcement provides false,
                            LocalRippleTheme provides NoRippleTheme
                        ) {
                            ElevatedButton(
                                onClick = {},
                                shape = Shapes.listItem,
                                contentPadding = PaddingValues(
                                    horizontal = 13.dp,
                                    vertical = 7.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .animateItemPlacement()
                            ) {
                                Box(Modifier.fillMaxWidth()) {
                                    if (currentGame?.cardsSupport == true) {
                                        Text(
                                            text = "${player.money}$MONEY",
                                            color = colorScheme.tertiary,
                                            style = Typography.bodyLarge,
                                            modifier = Modifier.align(Alignment.CenterEnd)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Rounded.Person,
                                            contentDescription = null,
                                            modifier = Modifier.align(Alignment.CenterStart)
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.align(if (currentGame?.cardsSupport == true) Alignment.CenterStart else Alignment.Center)
                                    ) {
                                        if (currentGame?.cardsSupport == true) {
                                            Icon(
                                                painterResource(id = R.drawable.credit_card),
                                                contentDescription = null,
                                                tint = NFCCardColorBindings[player.card]
                                                    ?: Color.Unspecified
                                            )
                                        }
                                        Text(
                                            player.name,
                                            style = Typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Column {
                ScreenSelector(
                    rowList = listOf(
                        if (currentGame?.cardsSupport == true)listOf(
                            ScreenItem(null, "Piskaitīt", R.drawable.add, onClick = {}),
                            ScreenItem(null, "Atņemt", R.drawable.remove, onClick = {}),
                            ScreenItem(null, "Apmaiņa", R.drawable.sync_alt, onClick = {}),
                        ) else listOf(),
                        listOf(
                            ScreenItem("loans", "Aizdevumi", R.drawable.request_quote),
                            ScreenItem(
                                "bankingcalc",
                                "Kalkulators",
                                R.drawable.calculate
                            )
                        )
                    ), navController = navController, modifier = Modifier
                        .fillMaxWidth()
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { exitDialogOpen = true },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = colorScheme.error,
                            contentColor = colorScheme.onError
                        )
                    ) {
                        Text(
                            text = "Beigt",
                            style = Typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }
            }
        }
    }
}