package cool.zolid.cardopoly.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.Game
import cool.zolid.cardopoly.Player
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.navigateWithoutTrace
import cool.zolid.cardopoly.ui.AlertDialog
import cool.zolid.cardopoly.ui.Shapes
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.primaryButtonColors
import cool.zolid.cardopoly.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StartGameScreen(navController: NavHostController, cards_enabled: Boolean) {
    val addingPlayerNames = remember { mutableStateListOf<String>() }
    var addDialogOpen by remember { mutableStateOf(true) }
    var removeDialogOpen by remember { mutableStateOf<String?>(null) }

    if (addDialogOpen) {
        var playerName by remember {
            mutableStateOf("")
        }
        AlertDialog(
            onDismissRequest = { addDialogOpen = false },
            title = { Text("Pievienot spēlētāju") },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    TextField(
                        value = playerName,
                        onValueChange = {
                            playerName = it.trim()
                        },
                        label = { Text("Spēlētāja vārds") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(playerName in addingPlayerNames) {
                        Text(
                            text = "Kļūda: Spēlētājs ar šādu vārdu jau eksistē",
                            color = colorScheme.error,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        addingPlayerNames.add(playerName)
                        addDialogOpen = false
                    },
                    enabled = playerName !in addingPlayerNames
                ) {
                    Text("Apstiprināt".uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = { addDialogOpen = false }) {
                    Text("Atcelt".uppercase())
                }
            }
        )
    }
    if (removeDialogOpen != null) {
        AlertDialog(
            onDismissRequest = { removeDialogOpen = null },
            title = { Text("Vai tiešām noņemt spēlētāju \"$removeDialogOpen\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        addingPlayerNames.remove(removeDialogOpen)
                        removeDialogOpen = null
                    }
                ) {
                    Text("Apstiprināt".uppercase(), color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { removeDialogOpen = null }) {
                    Text("Atcelt".uppercase())
                }
            }
        )
    }
    Scaffold(topBar = {
        StandardTopAppBar(
            title = "Spēlētāji",
            navController = navController,
            subtitle = "Spēlēt bez kartēm"
        )
    }, snackbarHost = { Snackbar.Host() },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (addingPlayerNames.size < 8) {
                ExtendedFloatingActionButton(
                    onClick = { addDialogOpen = true },
                    shape = Shapes.fab,
                    containerColor = colorScheme.tertiaryContainer,
                    contentColor = colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add")
                    Text("Pievienot spēlētāju", modifier = Modifier.padding(start = 10.dp))
                }
            }
        }) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(extraPadding(pv))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = addingPlayerNames.isEmpty()) {
                    Text(
                        "Nav neviena spēlētāja",
                        style = Typography.titleMedium,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
                LazyColumn(userScrollEnabled = true) {
                    itemsIndexed(addingPlayerNames) { _, playerName ->
                        CompositionLocalProvider(
                            LocalMinimumInteractiveComponentEnforcement provides false,
                        ) {
                            ElevatedButton(
                                onClick = {
                                    removeDialogOpen = playerName
                                },
                                shape = Shapes.listItem,
                                contentPadding = PaddingValues(
                                    horizontal = 15.dp,
                                    vertical = 10.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 0.dp, vertical = 5.dp)
                                    .animateItemPlacement()
                            ) {
                                Box(Modifier.fillMaxWidth()) {
                                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.align(Alignment.CenterStart))
                                    Text(
                                        playerName,
                                        style = Typography.bodyLarge,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = addingPlayerNames.size > 1,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 }),
                modifier = Modifier.height(160.dp)
            ) {
                Button(
                    onClick = {
                        currentGame = Game(cards_enabled, addingPlayerNames.map { Player(it, null) })
                        navController.navigateWithoutTrace("game")
                    },
                    shape = Shapes.largeButton,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 15.dp, start = 8.dp, end = 8.dp, bottom = 90.dp
                        ),
                    colors = primaryButtonColors
                ) {
                    Text(
                        "Spēlēt!",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }
}