package cool.zolid.cardopoly.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.Beep
import cool.zolid.cardopoly.Loan
import cool.zolid.cardopoly.LoanTerms
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.NFCCardColorBindings
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.nfcApiSubscribers
import cool.zolid.cardopoly.ui.AlertDialog
import cool.zolid.cardopoly.ui.Shapes
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LoansScreen(navController: NavHostController) {
    var viewDialogOpen by remember { mutableStateOf<Loan?>(null) }
    var payDialogOpen by remember { mutableStateOf<Loan?>(null) }

    if (viewDialogOpen != null) {
        AlertDialog(
            onDismissRequest = { viewDialogOpen = null },
            title = { Text("Aizdevums") },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.padding(end = 15.dp)) {
                            @Composable
                            fun TableText(text: String, color: Color = colorScheme.secondary) {
                                Text(
                                    text,
                                    style = Typography.bodyMedium,
                                    color = color,
                                    fontSize = 16.sp
                                )
                            }
                            TableText("Devējs")
                            TableText("Saņēmējs")
                            TableText("Aizdevuma summa")
                            TableText("Atmaksas summa")
                            TableText(
                                when (viewDialogOpen!!.terms) {
                                    is LoanTerms.Custom -> "Nosacījumi"
                                    is LoanTerms.Laps -> "Nosacījumi: Apļi"
                                }
                            )
                        }
                        Column {
                            @Composable
                            fun TableValue(value: String, color: Color = colorScheme.primary) {
                                Text(
                                    value,
                                    style = Typography.bodyMedium,
                                    color = color,
                                    fontSize = 16.sp
                                )
                            }
                            Row {
                                if (currentGame?.cardsSupport == true) {
                                    Icon(
                                        painterResource(id = R.drawable.credit_card),
                                        contentDescription = null,
                                        tint = NFCCardColorBindings[viewDialogOpen!!.from.card]
                                            ?: Color.Unspecified,
                                        modifier = Modifier
                                            .padding(end = 5.dp)
                                            .height(22.dp)
                                    )
                                }
                                TableValue(viewDialogOpen!!.from.name)
                            }
                            Row {
                                if (currentGame?.cardsSupport == true) {
                                    Icon(
                                        painterResource(id = R.drawable.credit_card),
                                        contentDescription = null,
                                        tint = NFCCardColorBindings[viewDialogOpen!!.to.card]
                                            ?: Color.Unspecified,
                                        modifier = Modifier
                                            .padding(end = 5.dp)
                                            .height(22.dp)
                                    )
                                }
                                TableValue(viewDialogOpen!!.to.name)
                            }
                            TableValue("${viewDialogOpen!!.amount}$MONEY", colorScheme.tertiary)
                            TableValue("${viewDialogOpen!!.amountToPayBack}$MONEY", colorScheme.tertiary)
                            TableValue(
                                when (viewDialogOpen!!.terms) {
                                    is LoanTerms.Custom -> (viewDialogOpen!!.terms as LoanTerms.Custom).terms
                                    is LoanTerms.Laps -> (viewDialogOpen!!.terms as LoanTerms.Laps).laps.toString()
                                }
                            )
                        }
                    }
                    if (!viewDialogOpen!!.notes.isNullOrBlank()) {
                        Text(
                            viewDialogOpen!!.notes ?: "",
                            style = Typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(top = 20.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewDialogOpen = null
                    },
                ) {
                    Text("Labi".uppercase())
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (currentGame!!.cardsSupport) {
                            payDialogOpen = viewDialogOpen
                        } else {
                            currentGame!!.loans.remove(viewDialogOpen)
                        }
                        viewDialogOpen = null
                    },
                    enabled = currentGame?.cardsSupport != true || ((viewDialogOpen?.to?.money?.intValue
                        ?: 0) >= (viewDialogOpen?.amountToPayBack ?: 0)),
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.tertiary)
                ) {
                    Text("Apmaksāt".uppercase())
                }
            }
        )
    }
    if (payDialogOpen != null) {
        var toTapped by remember { mutableStateOf(false) }
        DisposableEffect(true) {
            fun processNFC(b64id: String) {
                if (!toTapped) {
                    if (payDialogOpen!!.to.card == b64id) {
                        toTapped = true
                        Beep.moneyRemove()
                    } else {
                        Beep.error()
                        payDialogOpen = null
                        Snackbar.showSnackbarMsg("Aizdevuma saņēmēja karte neskrīt", true)
                    }
                } else {
                    if (payDialogOpen!!.from.card == b64id) {
                        currentGame!!.loans.remove(payDialogOpen)
                        payDialogOpen!!.to.money.intValue -= payDialogOpen!!.amountToPayBack
                        payDialogOpen!!.from.money.intValue += payDialogOpen!!.amountToPayBack
                        Snackbar.showSnackbarMsg("Darījums veiksmīgs")
                        Beep.moneyAdd()
                        payDialogOpen = null
                    } else {
                        Beep.error()
                        payDialogOpen = null
                        Snackbar.showSnackbarMsg("Aizdevuma devēja karte neskrīt", true)
                    }
                }
            }
            nfcApiSubscribers.add(::processNFC)
            onDispose {
                nfcApiSubscribers.remove(::processNFC)
            }
        }
        AlertDialog(
            onDismissRequest = { payDialogOpen = null },
            title = { Text("Atmaksāt aizdevumu") },
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
                        text = "Pietuviniet aizdevuma ${if (!toTapped) "saņēmēja" else "devēja"} karti tālruņa aizmugurei",
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { payDialogOpen = null },
                ) {
                    Text("Atcelt".uppercase())
                }
            }
        )
    }
    Scaffold(topBar = {
        StandardTopAppBar(
            title = "Aizdevumi",
            navController = navController
        )
    }, snackbarHost = { Snackbar.Host() },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("newloan") },
                shape = Shapes.fab,
                containerColor = colorScheme.tertiaryContainer,
                contentColor = colorScheme.onTertiaryContainer
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
                Text("Jauns aizdevums", modifier = Modifier.padding(start = 10.dp))
            }
        }) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(extraPadding(pv)), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = currentGame!!.loans.isEmpty()) {
                Text(
                    "Nav neviena aizdevuma",
                    style = Typography.titleMedium,
                )
            }
            LazyColumn(userScrollEnabled = true) {
                itemsIndexed(currentGame!!.loans) { _, loan ->
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentEnforcement provides false,
                    ) {
                        ElevatedButton(
                            onClick = {
                                viewDialogOpen = loan
                            },
                            shape = Shapes.listItem,
                            contentPadding = PaddingValues(
                                horizontal = 15.dp,
                                vertical = 10.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .animateItemPlacement()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(end = 10.dp)) {
                                    Row {
                                        if (currentGame?.cardsSupport == true) {
                                            Icon(
                                                painterResource(id = R.drawable.credit_card),
                                                contentDescription = null,
                                                tint = NFCCardColorBindings[loan.from.card]
                                                    ?: Color.Unspecified,
                                                modifier = Modifier
                                                    .padding(end = 5.dp)
                                                    .height(22.dp)
                                            )
                                        }
                                        Text(
                                            loan.from.name,
                                            style = Typography.bodyMedium,
                                        )
                                    }
                                    Row {
                                        Text(
                                            " └>  ",
                                            style = Typography.bodyMedium,
                                            color = colorScheme.secondary
                                        )
                                        if (currentGame?.cardsSupport == true) {
                                            Icon(
                                                painterResource(id = R.drawable.credit_card),
                                                contentDescription = null,
                                                tint = NFCCardColorBindings[loan.to.card]
                                                    ?: Color.Unspecified,
                                                modifier = Modifier
                                                    .padding(end = 5.dp)
                                                    .height(22.dp)
                                            )
                                        }
                                        Text(
                                            loan.to.name,
                                            style = Typography.bodyMedium,
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${loan.amount}$MONEY",
                                        style = Typography.bodyMedium,
                                        color = colorScheme.tertiary
                                    )
                                    Text(
                                        "${loan.amountToPayBack}$MONEY",
                                        style = Typography.bodyMedium,
                                        color = colorScheme.tertiary
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