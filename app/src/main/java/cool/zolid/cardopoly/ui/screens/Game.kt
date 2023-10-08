package cool.zolid.cardopoly.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import androidx.compose.ui.graphics.Brush
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
import cool.zolid.cardopoly.LoanTerms
import cool.zolid.cardopoly.Log
import cool.zolid.cardopoly.LogType
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.NFCCardColorBindings
import cool.zolid.cardopoly.Player
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.StaticGame
import cool.zolid.cardopoly.StaticPlayer
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.gameRecoveryDataStore
import cool.zolid.cardopoly.globalSettings
import cool.zolid.cardopoly.globalSettingsDialogOpen
import cool.zolid.cardopoly.navigateWithoutTrace
import cool.zolid.cardopoly.nfcApiSubscribers
import cool.zolid.cardopoly.ui.AlertDialog
import cool.zolid.cardopoly.ui.ScreenItem
import cool.zolid.cardopoly.ui.ScreenSelector
import cool.zolid.cardopoly.ui.Shapes
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.dialogCalculator
import cool.zolid.cardopoly.ui.dialogRealEstateTaxCalculator
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlin.math.roundToInt

private enum class BankOperation {
    ADD,
    REMOVE,
    TRANSFER
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
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
        var realEstateTradeTax by remember { mutableStateOf(false) }
        var cardTrasferFromUid by remember { mutableStateOf<String?>(null) }
        DisposableEffect(true) {
            fun processNFC(b64id: String) {
                val player = currentGame?.players?.find { it.card == b64id }
                if (sumLockedIn && player != null) {
                    when (currentBankOperationDialog) {
                        BankOperation.ADD -> {
                            player.money.intValue += sum!!
                            Snackbar.showSnackbarMsg("Darījums veiksmīgs")
                            Beep.moneyAdd()
                            currentGame!!.logs.add(
                                Log(
                                    LogType.ADD_MONEY,
                                    StaticPlayer(player),
                                    null,
                                    sum!!,
                                    currentGame!!.lap.intValue,
                                )
                            )
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
                                currentGame!!.logs.add(
                                    Log(
                                        LogType.REMOVE_MONEY,
                                        StaticPlayer(player),
                                        null,
                                        sum!!,
                                        currentGame!!.lap.intValue,
                                    )
                                )
                            }
                            currentBankOperationDialog = null
                        }

                        BankOperation.TRANSFER -> {
                            if (cardTrasferFromUid == null) {
                                cardTrasferFromUid = b64id
                                Beep.moneyRemove()
                            } else if (cardTrasferFromUid != b64id) {
                                val fromPlayer =
                                    currentGame!!.players.find { it.card == cardTrasferFromUid }!!
                                if (fromPlayer.money.intValue < sum!!) {
                                    Snackbar.showSnackbarMsg(
                                        "Darījums neveiksmīgs - nav pietiekamu līdzekļu",
                                        true
                                    )
                                    Beep.error()
                                } else {
                                    fromPlayer.money.intValue -= sum!!
                                    currentGame!!.players.find { it.card == b64id }!!.money.intValue += sum!!
                                    Snackbar.showSnackbarMsg("Darījums veiksmīgs")
                                    Beep.moneyAdd()
                                    currentGame!!.logs.add(
                                        Log(
                                            LogType.TRANSFER_MONEY,
                                            StaticPlayer(fromPlayer),
                                            StaticPlayer(player),
                                            sum!!,
                                            currentGame!!.lap.intValue,
                                        )
                                    )
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
                    Column(Modifier.fillMaxWidth()) {
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
                                label = { Text(if (realEstateTradeTax) "Īpašuma vērtība" else "Summa") },
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
                            val dialogCalc = dialogCalculator(resultPaste = {
                                if (it > 0) {
                                    sum = it
                                }
                            },
                                initialExpr = { sum?.toString() ?: "" })
                            IconButton(
                                onClick = { dialogCalc() },
                                modifier = Modifier.requiredHeight(IntrinsicSize.Max)
                            ) {
                                Icon(painterResource(id = R.drawable.calculate), null)
                            }
                        }
                        if (globalSettings.realestateTaxPercent.intValue != 0 && currentBankOperationDialog == BankOperation.ADD) {
                            val calculator =
                                dialogRealEstateTaxCalculator(resultPaste = { sum = it })
                            TextButton(
                                onClick = { calculator() },
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = colorScheme.secondary,
                                    contentColor = colorScheme.onSecondary
                                ),
                                shape = Shapes.listItem
                            ) {
                                Text(text = "Nekustamo īpašumu nodokļi")
                            }
                        }
                        if (currentBankOperationDialog == BankOperation.REMOVE && currentGame?.optionalTradeTax == true) {
                            Card(
                                Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = realEstateTradeTax,
                                            onCheckedChange = {
                                                realEstateTradeTax = it
                                            })
                                        Text(
                                            text = "Īpašumu apmaiņas komisija",
                                            color = colorScheme.secondary
                                        )
                                    }
                                    AnimatedVisibility(sum != null && realEstateTradeTax) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp)
                                                .padding(bottom = 10.dp)
                                        ) {
                                            Text(
                                                text = "Summa ar komisiju:",
                                                style = Typography.bodyLarge
                                            )
                                            Text(
                                                "${((sum ?: 0) * (globalSettings.optionalTradeRealestateTaxPercent.intValue / 100f + 1f)).roundToInt()}$MONEY",
                                                color = colorScheme.tertiary,
                                                style = Typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        AnimatedVisibility(currentGame?.optionalTradeTax == true && sum != null && currentBankOperationDialog == BankOperation.TRANSFER) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "Ar komisiju:",
                                    style = Typography.bodyLarge
                                )
                                Text(
                                    "${((sum ?: 0) * (globalSettings.optionalTradeMoneyTaxPercent.intValue / 100f + 1f)).roundToInt()}$MONEY",
                                    color = colorScheme.tertiary,
                                    style = Typography.bodyLarge
                                )
                            }
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
                        if (currentBankOperationDialog == BankOperation.TRANSFER) {
                            sum = ((sum
                                ?: 0) * (globalSettings.optionalTradeMoneyTaxPercent.intValue / 100f + 1f)).roundToInt()
                        } else if (realEstateTradeTax) {
                            sum = ((sum
                                ?: 0) * (globalSettings.optionalTradeRealestateTaxPercent.intValue / 100f + 1f)).roundToInt()
                        }
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
                        currentGame = null
                        CoroutineScope(Dispatchers.IO).launch {
                            ctx.gameRecoveryDataStore.edit { it.clear() }
                        }
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
    fun nextPlayerToMove() {
        val currIndex =
            currentGame!!.players.indexOf(currentGame!!.playerToMove.value)
        currentGame!!.playerToMove.value =
            currentGame!!.players[if (currIndex == currentGame!!.players.size - 1) 0 else currIndex + 1]
        if (currIndex == currentGame!!.players.size - 1) {
            currentGame!!.lap.intValue += 1
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
            if (currentGame!!.playerToMove.value == removePlayerDialog) {
                nextPlayerToMove()
            }
            currentGame!!.players.remove(removePlayerDialog)
            Beep.moneyRemove()
            currentGame!!.logs.add(
                Log(
                    LogType.REMOVE_PLAYER,
                    StaticPlayer(removePlayerDialog!!),
                    null,
                    null,
                    currentGame!!.lap.intValue
                )
            )
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
    val bsss = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(SheetValue.Expanded, skipHiddenState = true))
    BottomSheetScaffold(scaffoldState = bsss, sheetContent = {
        Column(Modifier.padding(horizontal = 10.dp).background(Brush.verticalGradient(0f to colorScheme.surface, 0.2f to colorScheme.background))) {
            if (currentGame?.playerToMove?.value != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ElevatedCard(
                        onClick = {
                            val currIndex =
                                currentGame!!.players.indexOf(currentGame!!.playerToMove.value)
                            currentGame!!.playerToMove.value =
                                currentGame!!.players[if (currIndex == 0) currentGame!!.players.size - 1 else currIndex - 1]
                            if (currIndex == 0) {
                                currentGame!!.lap.intValue -= 1
                            }
                        },
                        modifier = Modifier.weight(0.48f),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary,
                            disabledContentColor = colorScheme.onSurface.copy(0.38f)
                        ),
                        enabled = !(currentGame!!.lap.intValue <= 1 && currentGame!!.players.indexOf(
                            currentGame!!.playerToMove.value
                        ) == 0),
                    ) {
                        Column(
                            Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }
                    ElevatedCard(
                        onClick = ::nextPlayerToMove,
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Column(
                            Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }
                }
            }
            ScreenSelector(
                rowList = listOf(
                    if (currentGame?.cardsSupport == true) listOf(
                        ScreenItem(
                            null,
                            null,
                            R.drawable.add,
                            onClick = { currentBankOperationDialog = BankOperation.ADD }),
                        ScreenItem(
                            null,
                            null,
                            R.drawable.remove,
                            onClick = { currentBankOperationDialog = BankOperation.REMOVE }),
                        ScreenItem(
                            null,
                            null,
                            R.drawable.sync_alt,
                            onClick = { currentBankOperationDialog = BankOperation.TRANSFER }),
                    ) else listOf(),
                ),
                navController = navController,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ElevatedCard(
                    onClick = { navController.navigate("loans") },
                    colors = CardDefaults.elevatedCardColors(contentColor = colorScheme.onTertiaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.request_quote),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(text = "Aizdevumi", style = Typography.bodyLarge)
                    }
                }
                ElevatedCard(
                    onClick = { navController.navigate(if (currentGame?.cardsSupport == true) "logs" else "bankingcalc") },
                    colors = CardDefaults.elevatedCardColors(contentColor = colorScheme.onTertiaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            painterResource(if (currentGame?.cardsSupport == true) R.drawable.history else R.drawable.calculate),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (currentGame?.cardsSupport == true) "Darījumi" else "Kalk.",
                            style = Typography.bodyLarge
                        )
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = { exitDialogOpen = true },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = Shapes.largeButton
                ) {
                    Text(
                        text = "Beigt",
                        modifier = Modifier.padding(horizontal = 10.dp),
                        style = Typography.bodyLarge
                    )
                }
                TextButton(
                    onClick = { globalSettingsDialogOpen = true },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = Shapes.largeButton
                ) {
                    Text(
                        text = "Iestatījumi",
                        modifier = Modifier.padding(horizontal = 10.dp),
                        style = Typography.bodyLarge
                    )
                }
            }
        }
    }, snackbarHost = { Snackbar.Host() }) { pv ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(extraPadding(pv, top = 10.dp))
        ) {
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentEnforcement provides false
            ) {
                LazyColumn(userScrollEnabled = true) {
                    itemsIndexed(currentGame?.players?.run {
                        if (globalSettings.sortPlayersByMoney.value) sortedByDescending { it.money.intValue } else this
                    }
                        ?: listOf()) { _, player ->
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
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = animateColorAsState(
                                    if (currentGame?.playerToMove?.value == player) colorScheme.tertiaryContainer else colorScheme.surface,
                                    label = ""
                                ).value,
                                contentColor = animateColorAsState(
                                    if (currentGame?.playerToMove?.value == player) colorScheme.primary else colorScheme.primary,
                                    label = ""
                                ).value
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
                                            style = Typography.bodyLarge,
                                            textAlign = if (currentGame?.cardsSupport == true) TextAlign.Start else TextAlign.Center
                                        )
                                        if (currentGame?.loans?.any { it.to == player } == true) {
                                            if (currentGame?.loans?.any { it.to == player && it.terms is LoanTerms.Laps && it.terms.paybackLap == currentGame!!.lap.intValue } == true) {
                                                Text(
                                                    "Parāds jāatmaksā šajā aplī",
                                                    style = Typography.bodyMedium,
                                                    color = colorScheme.primary,
                                                    textAlign = if (currentGame?.cardsSupport == true) TextAlign.Start else TextAlign.Center
                                                )
                                            } else if (currentGame?.loans?.any { it.to == player && it.terms is LoanTerms.Laps && it.terms.paybackLap < currentGame!!.lap.intValue } == true) {
                                                Text(
                                                    "Kavēts parāds",
                                                    style = Typography.bodyMedium,
                                                    color = colorScheme.error,
                                                    textAlign = if (currentGame?.cardsSupport == true) TextAlign.Start else TextAlign.Center
                                                )
                                            } else {
                                                Text(
                                                    "${currentGame?.loans?.count { it.to == player }} ${if (currentGame?.loans?.count { it.to == player } == 1) "aktīvs parāds" else "aktīvi parādi"}",
                                                    style = Typography.bodyMedium,
                                                    color = colorScheme.secondary,
                                                    textAlign = if (currentGame?.cardsSupport == true) TextAlign.Start else TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    itemsIndexed(currentGame?.historicPlayers ?: listOf()) { _, player ->
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
                                .padding(vertical = 3.dp),
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
    }
}