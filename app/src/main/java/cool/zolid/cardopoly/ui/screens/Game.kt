package cool.zolid.cardopoly.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.Beep
import cool.zolid.cardopoly.HistoricPlayer
import cool.zolid.cardopoly.Json
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.NFCCardColorBindings
import cool.zolid.cardopoly.Player
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.StaticGame
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.gameRecoveryDataStore
import cool.zolid.cardopoly.navigateWithoutTrace
import cool.zolid.cardopoly.nfcApiSubscribers
import cool.zolid.cardopoly.ui.AlertDialog
import cool.zolid.cardopoly.ui.ScreenItem
import cool.zolid.cardopoly.ui.ScreenSelector
import cool.zolid.cardopoly.ui.Shapes
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.dialogCalculator
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString

private enum class BankOperation {
    ADD,
    REMOVE,
    TRANSFER
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun GameScreen(navController: NavHostController) {
    val ctx = LocalContext.current
    LaunchedEffect(true) {
        while (true) {
            if (currentGame != null) {
                ctx.gameRecoveryDataStore.edit {
                    it[stringPreferencesKey("savedgame")] =
                        Json.encodeToString(StaticGame(currentGame!!))
                }
            }
            delay(30000)
        }
    }
    var currentBankOperationDialog by remember { mutableStateOf<BankOperation?>(null) }
    var removePlayerDialog by remember { mutableStateOf<Player?>(null) }
    var exitDialogOpen by remember { mutableStateOf(false) }
    if (currentBankOperationDialog != null) {
        var sum by remember { mutableStateOf<Int?>(null) }
        var sumLockedIn by remember { mutableStateOf(false) }
        var cardTrasferFromUid by remember { mutableStateOf<String?>(null) }
        DisposableEffect(true) {
            fun processNFC(b64id: String) {
                if (sumLockedIn && currentGame?.players?.any { it.card == b64id } == true) {
                    when (currentBankOperationDialog) {
                        BankOperation.ADD -> {
                            currentGame!!.players.find { it.card == b64id }!!.money.intValue += sum!!
                            Snackbar.showSnackbarMsg("Darījums veiksmīgs")
                            Beep.moneyAdd()
                            currentBankOperationDialog = null
                        }

                        BankOperation.REMOVE -> {
                            val money = currentGame!!.players.find { it.card == b64id }!!.money
                            if (money.intValue < sum!!) {
                                Snackbar.showSnackbarMsg(
                                    "Darījums neveiksmīgs - nav pietiekamu līdzekļu",
                                    true
                                )
                                Beep.error()
                            } else {
                                money.intValue -= sum!!
                                Snackbar.showSnackbarMsg("Darījums veiksmīgs")
                                Beep.moneyRemove()
                            }
                            currentBankOperationDialog = null
                        }

                        BankOperation.TRANSFER -> {
                            if (cardTrasferFromUid == null) {
                                cardTrasferFromUid = b64id
                                Beep.moneyRemove()
                            } else if (cardTrasferFromUid != b64id) {
                                val moneyFromAcc =
                                    currentGame!!.players.find { it.card == cardTrasferFromUid }!!.money
                                if (moneyFromAcc.intValue < sum!!) {
                                    Snackbar.showSnackbarMsg(
                                        "Darījums neveiksmīgs - nav pietiekamu līdzekļu",
                                        true
                                    )
                                    Beep.error()
                                } else {
                                    moneyFromAcc.intValue -= sum!!
                                    currentGame!!.players.find { it.card == b64id }!!.money.intValue += sum!!
                                    Snackbar.showSnackbarMsg("Darījums veiksmīgs")
                                    Beep.moneyAdd()
                                }
                                currentBankOperationDialog = null
                            }
                        }

                        null -> {}
                    }
                }
            }
            nfcApiSubscribers.add(::processNFC)
            onDispose {
                nfcApiSubscribers.remove(::processNFC)
            }
        }
        AlertDialog(
            onDismissRequest = { currentBankOperationDialog = null },
            title = {
                Text(
                    "${
                        when (currentBankOperationDialog) {
                            BankOperation.ADD -> "Pieskaitīt"
                            BankOperation.REMOVE -> "Atņemt"
                            BankOperation.TRANSFER -> "Apmaiņa"
                            null -> ""
                        }
                    }${if (sumLockedIn) " - $sum$MONEY" else ""}"
                )
            },
            text = {
                if (!sumLockedIn) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val focusRequester = remember { FocusRequester() }
                        TextField(
                            value = sum?.toString() ?: "",
                            onValueChange = {
                                sum = it.toIntOrNull().takeIf { it != null && it > 0 }
                            },
                            label = { Text("Summa") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            suffix = { Text(text = MONEY) },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                        )
                        LaunchedEffect(true) {
                            focusRequester.requestFocus()
                        }
                        val dialogCalc = dialogCalculator(resultPaste = { sum = it },
                            initialExpr = { sum?.toString() ?: "" })
                        IconButton(
                            onClick = { dialogCalc() },
                            modifier = Modifier.requiredHeight(IntrinsicSize.Max)
                        ) {
                            Icon(painterResource(id = R.drawable.calculate), null)
                        }
                    }
                } else {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painterResource(id = R.drawable.round_contactless),
                            "NFC",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(bottom = 5.dp)
                        )
                        Text(
                            text = "Pietuviniet${if (currentBankOperationDialog == BankOperation.TRANSFER) (if (cardTrasferFromUid == null) " devēja" else " saņēmēja") else ""} karti tālruņa aizmugurei",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sumLockedIn = true
                    },
                    enabled = sum != null && !sumLockedIn
                ) {
                    Text("Apstiprināt".uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = { currentBankOperationDialog = null }) {
                    Text("Atcelt".uppercase())
                }
            }
        )
    }
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
    if (removePlayerDialog != null) {
        var confirmed by remember { mutableStateOf(false) }
        fun wipePlayer() {
            currentGame!!.loans.removeAll { it.from == removePlayerDialog!! || it.to == removePlayerDialog!! }
            currentGame!!.historicPlayers.add(
                HistoricPlayer(
                    removePlayerDialog!!.name,
                    removePlayerDialog!!.card
                )
            )
            currentGame!!.players.remove(removePlayerDialog)
            Beep.moneyRemove()
            removePlayerDialog = null
        }
        DisposableEffect(true) {
            fun processNFC(b64id: String) {
                if (!confirmed) return
                if (b64id != removePlayerDialog!!.card) {
                    Snackbar.showSnackbarMsg("Karte nesakrīt ar izvēlēto spēlētāju", true)
                    removePlayerDialog = null
                    return
                }
                wipePlayer()
            }
            nfcApiSubscribers.add(::processNFC)
            onDispose {
                nfcApiSubscribers.remove(::processNFC)
            }
        }
        AlertDialog(
            onDismissRequest = { removePlayerDialog = null },
            title = { Text("Vai tiešām beigt spēli spēlētājam ${removePlayerDialog?.name}?") },
            text = {
                if (confirmed) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painterResource(id = R.drawable.round_contactless),
                            "NFC",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(bottom = 5.dp),
                            tint = colorScheme.errorContainer
                        )
                        Text(
                            text = "Pietuviniet karti, lai pabeigtu spēli",
                            textAlign = TextAlign.Center,
                            color = colorScheme.error
                        )
                    }
                } else {
                    Text(
                        "Visi aizdevumi saistīti ar šo spēlētāju tiks dzēsti",
                        color = colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentGame?.cardsSupport == true) {
                            confirmed = true
                        } else {
                            wipePlayer()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = colorScheme.errorContainer
                    ),
                    enabled = !confirmed
                ) {
                    Text("Apstiprināt".uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = { removePlayerDialog = null }) {
                    Text("Atcelt".uppercase())
                }
            }
        )
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
                    itemsIndexed(currentGame?.players?.sortedByDescending { it.money.intValue }
                        ?: listOf()) { _, player ->
                        CompositionLocalProvider(
                            LocalMinimumInteractiveComponentEnforcement provides false
                        ) {
                            ElevatedButton(
                                onClick = {
                                    if (currentGame!!.players.size > 2) {
                                        removePlayerDialog = player
                                    } else {
                                        Snackbar.showSnackbarMsg(
                                            "Nevar dzēst spēlētāju: Aktīvo spēlētāju skaits nedrīkst būt mazāks par 2",
                                            true,
                                            SnackbarDuration.Long
                                        )
                                    }
                                },
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
                                        val intvalue by animateIntAsState(
                                            targetValue = player.money.intValue,
                                            label = "",
                                            animationSpec = spring(
                                                visibilityThreshold = 1,
                                                stiffness = Spring.StiffnessVeryLow
                                            )
                                        )
                                        Text(
                                            text = "$intvalue$MONEY",
                                            color = colorScheme.tertiary,
                                            style = Typography.bodyLarge,
                                            modifier = Modifier.align(Alignment.TopEnd)
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
                                        Column {
                                            Text(
                                                player.name,
                                                style = Typography.bodyLarge
                                            )
                                            if (currentGame?.loans?.any { it.to == player } == true) {
                                                Text(
                                                    "${currentGame?.loans?.count { it.to == player }} ${if (currentGame?.loans?.count { it.to == player } == 1) "aktīvs parāds" else "aktīvi parādi"}",
                                                    style = Typography.bodyMedium,
                                                    color = colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                LazyColumn(userScrollEnabled = true) {
                    itemsIndexed(currentGame?.historicPlayers ?: listOf()) { _, player ->
                        CompositionLocalProvider(
                            LocalMinimumInteractiveComponentEnforcement provides false
                        ) {
                            ElevatedButton(
                                onClick = {},
                                enabled = false,
                                shape = Shapes.listItem,
                                contentPadding = PaddingValues(
                                    horizontal = 13.dp,
                                    vertical = 7.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Box(Modifier.fillMaxWidth()) {
                                    if (currentGame?.cardsSupport == false) {
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
                                            style = Typography.bodyLarge,
                                            textDecoration = TextDecoration.LineThrough,
                                            fontStyle = FontStyle.Italic
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
                        if (currentGame?.cardsSupport == true) listOf(
                            ScreenItem(
                                null,
                                "Pieskaitīt",
                                R.drawable.add,
                                onClick = { currentBankOperationDialog = BankOperation.ADD }),
                            ScreenItem(
                                null,
                                "Atņemt",
                                R.drawable.remove,
                                onClick = { currentBankOperationDialog = BankOperation.REMOVE }),
                            ScreenItem(
                                null,
                                "Apmaiņa",
                                R.drawable.sync_alt,
                                onClick = { currentBankOperationDialog = BankOperation.TRANSFER }),
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