package cool.zolid.cardopoly.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.navigateWithoutTrace
import cool.zolid.cardopoly.ui.AlertDialog
import cool.zolid.cardopoly.ui.ScreenItem
import cool.zolid.cardopoly.ui.ScreenSelector
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography

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
                        containerColor = colorScheme.errorContainer,
                        contentColor = colorScheme.onErrorContainer
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
                .padding(extraPadding(pv)), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Cardopoly", style = Typography.headlineMedium, color = colorScheme.primary)
            ScreenSelector(
                itemList = listOf(
                    ScreenItem("loans", "Aizdevumi", R.drawable.request_quote),
                    ScreenItem("bankingcalc", "Banķiera kalkulators", R.drawable.account_balance)
                ), navController = navController, modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 60.dp),
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