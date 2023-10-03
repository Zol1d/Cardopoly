package cool.zolid.cardopoly.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.Beep
import cool.zolid.cardopoly.Loan
import cool.zolid.cardopoly.LoanTerms
import cool.zolid.cardopoly.Log
import cool.zolid.cardopoly.LogType
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.Player
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.StaticPlayer
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.navigateWithoutTrace
import cool.zolid.cardopoly.nfcApiSubscribers
import cool.zolid.cardopoly.ui.AlertDialog
import cool.zolid.cardopoly.ui.ExposedDropDownMenu
import cool.zolid.cardopoly.ui.SegmentedButtons
import cool.zolid.cardopoly.ui.Shapes
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.dialogCalculator
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.setKeyboardSupport
import cool.zolid.cardopoly.ui.theme.Typography

@Composable
fun NewLoanScreen(navController: NavHostController) {
    val view = LocalView.current
    DisposableEffect(true) {
        view.setKeyboardSupport(true)
        onDispose {
            view.setKeyboardSupport(false)
        }
    }
    var from by remember { mutableStateOf<Player?>(null) }
    var to by remember { mutableStateOf<Player?>(null) }
    var amount by remember { mutableStateOf<Int?>(null) }
    var amountToPayBack by remember { mutableStateOf<Int?>(null) }
    var terms by remember { mutableIntStateOf(1) }
    var customTerms by remember { mutableStateOf<String?>(null) }
    var irlLapTerms by remember { mutableStateOf<Int?>(null) }
    var lapTerms by remember { mutableStateOf<Int?>(null) }
    var notes by remember { mutableStateOf<String?>(null) }
    var payDialogOpen by remember { mutableStateOf(false) }
    val confirmBtnShown by remember {
        derivedStateOf {
            listOf(
                if (currentGame?.cardsSupport == true) true else from,
                if (currentGame?.cardsSupport == true) true else to,
                amount,
                amountToPayBack,
                terms
            ).all { it != null } && (if (terms == 0) customTerms != null else if (terms == 1) irlLapTerms != null else lapTerms != null) && (amount!! <= amountToPayBack!!)
        }
    }

    fun makeLoan() {
        if (currentGame!!.cardsSupport) {
            payDialogOpen = false
            if ((amount ?: from?.money?.intValue ?: 0) > (from?.money?.intValue ?: amount ?: 0)) {
                Snackbar.showSnackbarMsg("Darījums neizdevās - nav pietiekmau līdzekļu", true)
                Beep.error()
                return
            }
            from!!.money.intValue -= amount!!
            to!!.money.intValue += amount!!
            Snackbar.showSnackbarMsg("Darījums veiksmīgs")
            Beep.moneyAdd()
            currentGame!!.logs.add(
                Log(
                    LogType.CREATE_LOAN,
                    StaticPlayer(from!!),
                    StaticPlayer(to!!),
                    amount!!,
                    currentGame!!.lap.intValue
                )
            )
        }
        currentGame!!.loans.add(
            Loan(
                from = from!!,
                to = to!!,
                amount = amount!!,
                amountToPayBack = amountToPayBack!!,
                terms = if (terms == 0) LoanTerms.Custom(customTerms!!) else if (terms == 1) LoanTerms.IRLLaps(
                    irlLapTerms!!
                ) else LoanTerms.Laps(lapTerms!! + currentGame!!.lap.intValue),
                notes = notes
            )
        )
        navController.navigateWithoutTrace("game")
    }

    if (payDialogOpen) {
        DisposableEffect(true) {
            fun processNFC(b64id: String) {
                if (currentGame?.players?.filter { it.card != from?.card && it.card != to?.card }
                        ?.any { it.card == b64id } == true) {
                    if (from == null) {
                        from = currentGame!!.players.find { it.card == b64id }!!
                        Beep.moneyRemove()
                    } else if (to == null) {
                        to = currentGame!!.players.find { it.card == b64id }!!
                        makeLoan()
                    }
                }
            }
            nfcApiSubscribers.add(::processNFC)
            onDispose {
                nfcApiSubscribers.remove(::processNFC)
            }
        }
        AlertDialog(
            onDismissRequest = {
                from = null
                to = null
                payDialogOpen = false
            },
            title = { Text("Apstiprināt aizdevumu") },
            text = {
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
                        text = "Pietuviniet ${if (from == null) "devēja" else "saņēmēja"} karti tālruņa aizmugurei",
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        from = null
                        to = null
                        payDialogOpen = false
                    },
                ) {
                    Text("Atcelt".uppercase())
                }
            }
        )
    }
    Scaffold(snackbarHost = { Snackbar.Host() },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if (confirmBtnShown) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (currentGame?.cardsSupport == true) {
                            payDialogOpen = true
                        } else {
                            makeLoan()
                        }
                    },
                    shape = Shapes.fab,
                    elevation = FloatingActionButtonDefaults.elevation(10.dp, 10.dp, 10.dp, 14.dp)
                ) {
                    Text(
                        "Apstiprināt",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        },
        topBar = { StandardTopAppBar(title = "Jauns aizdevums", navController) }) { pv ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(extraPadding(pv))
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SegmentedButtons(
                itemsList = mutableListOf(
                    "Pielāgots",
                    "Galda apļi"
                ).apply { if (currentGame?.playerToMove?.value != null) add("Apļi") },
                onSelectedItem = {
                    terms = it
                },
                initialSelectionIndex = terms,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
            if (currentGame?.cardsSupport != true) {
                ExposedDropDownMenu(
                    items = currentGame!!.players,
                    selectedItem = from,
                    onSelectedItem = {
                        if (it == to) {
                            to = from
                        }
                        from = it
                    },
                    label = "Devējs",
                    nullReplacement = "Izvēlieties devēju",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(270f)
                        .size(36.dp)
                        .align(Alignment.CenterHorizontally)
                )
                ExposedDropDownMenu(
                    items = currentGame!!.players,
                    selectedItem = to,
                    onSelectedItem = {
                        if (it == from) {
                            from = to
                        }
                        to = it
                    },
                    label = "Saņēmējs",
                    nullReplacement = "Izvēlieties saņēmēju",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 20.dp)
                )
            }
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val focusRequester = remember { FocusRequester() }
                Column(Modifier.weight(1f)) {
                    TextField(
                        value = amount?.toString() ?: "",
                        onValueChange = {
                            amount = it.toIntOrNull().takeIf { it != null && it > 0 }
                        },
                        label = { Text("Summa") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        suffix = { Text(text = MONEY) },
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                    val dialogCalc = dialogCalculator(resultPaste = { if (it > 0) amount = it },
                        initialExpr = { amount?.toString() ?: "" })
                    Button(
                        onClick = { dialogCalc() },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.secondary,
                            contentColor = colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 4.dp
                        ),
                        contentPadding = PaddingValues(vertical = 2.dp, horizontal = 10.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.calculate),
                            null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                LaunchedEffect(true) {
                    if (currentGame?.cardsSupport == true) {
                        focusRequester.requestFocus()
                    }
                }
                Column(Modifier.weight(1f)) {
                    TextField(
                        value = amountToPayBack?.toString() ?: "",
                        onValueChange = {
                            amountToPayBack =
                                it.toIntOrNull().takeIf { it != null && it > 0 }
                        },
                        label = { Text("Atmaksa") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        suffix = { Text(text = MONEY) },
                    )
                    val dialogCalc =
                        dialogCalculator(resultPaste = { if (it > 0) amountToPayBack = it },
                            initialExpr = {
                                amount?.toString() ?: amountToPayBack?.toString() ?: ""
                            })
                    Button(
                        onClick = { dialogCalc() },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.secondary,
                            contentColor = colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 4.dp
                        ),
                        contentPadding = PaddingValues(vertical = 2.dp, horizontal = 10.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.calculate),
                            null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            AnimatedVisibility(
                currentGame?.cardsSupport == true && (amount ?: from?.money?.intValue
                ?: 0) > (from?.money?.intValue ?: amount ?: 0)
            ) {
                Text(
                    text = "Kļūda: Devējam nav pietiekamu līdzekļu priekš aizdevuma",
                    color = colorScheme.error,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            AnimatedVisibility(amount != null && amountToPayBack != null && amountToPayBack!! < amount!!) {
                Text(
                    text = "Kļūda: Aizdevuma summa nevar būt lielāka par atmaksas summu",
                    color = colorScheme.error,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            when (terms) {
                0 -> {
                    TextField(
                        value = customTerms ?: "",
                        onValueChange = {
                            customTerms = it.trim().takeUnless { it.isBlank() }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        label = { Text(text = "Nosacījumi") },
                        singleLine = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp)
                    )
                }

                1 -> {
                    TextField(
                        value = irlLapTerms?.toString() ?: "",
                        onValueChange = {
                            irlLapTerms = it.toIntOrNull().takeIf { it != null && it > 0 }
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                        ),
                        label = { Text(text = "Galda apļi") },
                        singleLine = true,
                        modifier = Modifier
                            .width(140.dp)
                            .align(Alignment.End)
                            .padding(top = 5.dp)
                    )
                }

                2 -> {
                    TextField(
                        value = lapTerms?.toString() ?: "",
                        onValueChange = {
                            lapTerms = it.toIntOrNull().takeIf { it != null && it > 0 }
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                        ),
                        label = { Text(text = "Apļi") },
                        singleLine = true,
                        modifier = Modifier
                            .width(140.dp)
                            .align(Alignment.End)
                            .padding(top = 5.dp)
                    )
                }
            }
            TextField(
                value = notes ?: "",
                onValueChange = {
                    notes = it.trim().takeUnless { it.isBlank() }
                },
                label = { Text("Piezīmes") },
                singleLine = false,
                modifier = (if (confirmBtnShown) Modifier.padding(bottom = 80.dp) else Modifier)
                    .padding(
                        top = 30.dp
                    )
                    .fillMaxWidth()
            )
        }
    }
}