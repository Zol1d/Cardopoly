package cool.zolid.cardopoly.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.Beep
import cool.zolid.cardopoly.Game
import cool.zolid.cardopoly.MonopolyColors
import cool.zolid.cardopoly.NFCCardColorBindings
import cool.zolid.cardopoly.Player
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.globalSettings
import cool.zolid.cardopoly.navigateWithoutTrace
import cool.zolid.cardopoly.nfcApiSubscribers
import cool.zolid.cardopoly.ui.AlertDialog
import cool.zolid.cardopoly.ui.Shapes
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.primaryButtonColors
import cool.zolid.cardopoly.ui.theme.Typography
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StartGameScreen(navController: NavHostController, cards_enabled: Boolean) {
    val addingPlayers = remember { mutableStateListOf<Player>() }
    var addDialogOpen by remember { mutableStateOf(true) }
    var removeDialogOpen by remember { mutableStateOf<Player?>(null) }

    if (addDialogOpen) {
        var playerName by remember { mutableStateOf("") }
        var cardUid by remember { mutableStateOf<String?>(null) }
        if (cards_enabled) {
            DisposableEffect(true) {
                fun processNFC(b64id: String) {
                    if (cardUid == null && b64id in NFCCardColorBindings && b64id !in addingPlayers.map { it.card }) {
                        cardUid = b64id
                        Beep.moneyAdd()
                    }
                }
                nfcApiSubscribers.add(::processNFC)
                onDispose {
                    nfcApiSubscribers.remove(::processNFC)
                }
            }
        }
        AlertDialog(
            onDismissRequest = { addDialogOpen = false },
            title = { Text("Pievienot spēlētāju") },
            text = {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val focusRequester = remember { FocusRequester() }
                    TextField(
                        value = playerName,
                        onValueChange = {
                            playerName = it.trim()
                        },
                        label = { Text("Spēlētāja vārds") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (cards_enabled) 10.dp else 0.dp)
                            .focusRequester(focusRequester)
                    )
                    LaunchedEffect(true) {
                        focusRequester.requestFocus()
                    }
                    AnimatedVisibility(playerName in addingPlayers.map { it.name }) {
                        Text(
                            text = "Kļūda: Spēlētājs ar šādu vārdu jau eksistē",
                            color = colorScheme.error,
                            modifier = Modifier.padding(bottom = if (cards_enabled) 10.dp else 0.dp)
                        )
                    }
                    if (cardUid == null && cards_enabled) {
                        Icon(
                            painterResource(id = R.drawable.round_contactless),
                            "NFC",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(bottom = 5.dp)
                        )
                        Text(
                            text = "Pietuviniet karti tālruņa aizmugurei",
                            textAlign = TextAlign.Center
                        )
                    } else if (cards_enabled) {
                        Text(
                            text = "${MonopolyColors[NFCCardColorBindings[cardUid]]} karte",
                            color = NFCCardColorBindings[cardUid] ?: colorScheme.tertiary,
                            style = Typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        addingPlayers.add(Player(playerName, cardUid, mutableIntStateOf(1500)))
                        addDialogOpen = false
                    },
                    enabled = playerName !in addingPlayers.map { it.name } && playerName.isNotBlank() && (!cards_enabled || cardUid != null)
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
                        addingPlayers.remove(removeDialogOpen)
                        removeDialogOpen = null
                    },
                    colors = ButtonDefaults.textButtonColors(containerColor = colorScheme.errorContainer)
                ) {
                    Text("Apstiprināt".uppercase())
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
            title = if (cards_enabled) "Spēlēt ar kartēm" else "Spēlēt bez kartēm",
            navController = navController,
        )
    }, snackbarHost = { Snackbar.Host() },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (addingPlayers.size < 8 && (!cards_enabled || addingPlayers.size < NFCCardColorBindings.size)) {
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
        var playerMoveSys by remember { mutableStateOf(false) }
        var optionalTradeTax by remember { mutableStateOf(false) }
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
                Card(Modifier.padding(bottom = 15.dp)) {
                    Column(Modifier.padding(vertical = 5.dp, horizontal = 15.dp).fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gājienu sistēma",
                                modifier = Modifier.weight(1f),
                                color = colorScheme.secondary
                            )
                            Switch(
                                checked = playerMoveSys,
                                onCheckedChange = {
                                    playerMoveSys = it
                                })
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Neoblig. apmaiņas komisijas",
                                modifier = Modifier.weight(1f),
                                color = colorScheme.secondary
                            )
                            Switch(
                                checked = optionalTradeTax,
                                onCheckedChange = {
                                    optionalTradeTax = it
                                })
                        }
                        AnimatedVisibility(optionalTradeTax && (globalSettings.optionalTradeMoneyTaxPercent.intValue == 0 || globalSettings.optionalTradeRealestateTaxPercent.intValue == 0)) {
                            Text(
                                text = "Pārliecinieties, ka iestatījumos esat iestatījis atbilstošas komisijas",
                                color = colorScheme.tertiary
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = addingPlayers.isEmpty()) {
                    Text(
                        "Nav neviena spēlētāja",
                        style = Typography.titleMedium,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
                val listState = rememberReorderableLazyListState(onMove = { from, to ->
                    addingPlayers.add(to.index, addingPlayers.removeAt(from.index))
                })
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentEnforcement provides false,
                ) {
                    LazyColumn(
                        userScrollEnabled = true,
                        state = listState.listState,
                        modifier = Modifier.reorderable(listState).weight(1f)
                    ) {
                        items(items = addingPlayers, key = { it.name }) { player ->
                            ReorderableItem(
                                reorderableState = listState,
                                key = player.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) { isDragging ->
                                Row(Modifier.fillMaxWidth()) {
                                    AnimatedVisibility(
                                        visible = playerMoveSys
                                    ) {
                                        Box(Modifier.padding(end = 5.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(MaterialTheme.shapes.extraSmall)
                                                    .width(30.dp)
                                                    .height(40.dp)
                                                    .detectReorder(listState),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painterResource(id = R.drawable.drag_indicator),
                                                    contentDescription = null,
                                                )
                                            }
                                        }
                                    }
                                    ElevatedButton(
                                        onClick = {
                                            removeDialogOpen = player
                                        },
                                        shape = Shapes.listItem,
                                        contentPadding = PaddingValues(
                                            horizontal = 13.dp,
                                            vertical = 7.dp
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(
                                                animateDpAsState(
                                                    targetValue = if (isDragging) 18.dp else 0.dp,
                                                    label = ""
                                                ).value
                                            )
                                    ) {
                                        Box(Modifier.fillMaxWidth()) {
                                            if (cards_enabled) {
                                                Icon(
                                                    painterResource(id = R.drawable.credit_card),
                                                    contentDescription = null,
                                                    modifier = Modifier.align(Alignment.CenterStart),
                                                    tint = NFCCardColorBindings[player.card]
                                                        ?: Color.Unspecified
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Rounded.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.align(Alignment.CenterStart)
                                                )
                                            }
                                            Text(
                                                player.name,
                                                style = Typography.bodyLarge,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = addingPlayers.size > 1,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 }),
                modifier = Modifier.height(160.dp)
            ) {
                Button(
                    onClick = {
                        currentGame = Game(
                            cards_enabled,
                            optionalTradeTax,
                            mutableStateOf(if (playerMoveSys) addingPlayers[0] else null),
                            mutableIntStateOf(1),
                            addingPlayers,
                            mutableStateListOf(),
                            mutableStateListOf(),
                            mutableListOf()
                        )
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