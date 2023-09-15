package cool.zolid.cardopoly.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.Loan
import cool.zolid.cardopoly.LoanTerms
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.Player
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.navigateWithoutTrace
import cool.zolid.cardopoly.ui.ExposedDropDownMenu
import cool.zolid.cardopoly.ui.SectionDivider
import cool.zolid.cardopoly.ui.Shapes
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLoanScreen(navController: NavHostController) {
    var from by remember { mutableStateOf<Player?>(null) }
    var to by remember { mutableStateOf<Player?>(null) }
    var amount by remember { mutableStateOf<Int?>(null) }
    var amountToPayBack by remember { mutableStateOf<Int?>(null) }
    var terms by remember { mutableStateOf<String?>(null) }
    var customTerms by remember { mutableStateOf<String?>(null) }
    var lapTerms by remember { mutableStateOf<Int?>(null) }
    var notes by remember { mutableStateOf<String?>(null) }
    val confirmBtnShown by remember {
        derivedStateOf {
            listOf(
                from, to, amount, amountToPayBack, terms
            ).all { it != null } && (if (terms!! == "Apļi") lapTerms != null else customTerms != null) && (amount!! <= amountToPayBack!!)
        }
    }
    Scaffold(snackbarHost = { Snackbar.Host() },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if (confirmBtnShown) {
                ExtendedFloatingActionButton(
                    onClick = {
                        currentGame!!.loans.add(
                            Loan(
                                from = from!!,
                                to = to!!,
                                amount = amount!!,
                                amountToPayBack = amountToPayBack!!,
                                terms = if (terms == "Apļi") LoanTerms.Laps(lapTerms!!) else LoanTerms.Custom(
                                    customTerms!!
                                ),
                                notes = notes
                            )
                        )
                        navController.navigateWithoutTrace("game")
                    }, shape = Shapes.fab
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SectionDivider(text = "Spēlētāji", false)
            ExposedDropDownMenu(
                items = currentGame!!.players, selectedItem = from, onSelectedItem = {
                    if (it == to) {
                        to = from
                    }
                    from = it
                }, label = "Devējs", nullReplacement = "Izvēlieties devēju"
            )
            Icon(
                Icons.Rounded.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .rotate(270f)
                    .size(36.dp)
            )
            ExposedDropDownMenu(
                items = currentGame!!.players, selectedItem = to, onSelectedItem = {
                    if (it == from) {
                        from = to
                    }
                    to = it
                }, label = "Saņēmējs", nullReplacement = "Izvēlieties saņēmēju"
            )
            SectionDivider(text = "Summas")
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    TextField(value = amount?.toString() ?: "", onValueChange = {
                        amount = it.toIntOrNull().takeIf { it != null && it > 0 }
                    }, label = { Text("Aizdevums") }, keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                    ), singleLine = true, suffix = { Text(text = MONEY) })
                    TextButton(
                        onClick = { /*TODO*/ },
                        shape = Shapes.listItem,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = colorScheme.secondary,
                            contentColor = colorScheme.onSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "%", style = Typography.bodyLarge)
                    }
                }
                Column(Modifier.weight(1f)) {
                    TextField(
                        value = amountToPayBack?.toString() ?: "",
                        onValueChange = {
                            amountToPayBack = it.toIntOrNull().takeIf { it != null && it > 0 }
                        },
                        label = { Text("Atmaksa") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        suffix = { Text(text = MONEY) },
                    )
                    TextButton(
                        onClick = { /*TODO*/ },
                        shape = Shapes.listItem,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = colorScheme.secondary,
                            contentColor = colorScheme.onSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "%", style = Typography.bodyLarge)
                    }
                }
            }

            AnimatedVisibility(amount != null && amountToPayBack != null && amountToPayBack!! < amount!!) {
                Text(
                    text = "Kļūda: Aizdevuma summa nevar būt lielāka par atmaksas summu",
                    color = colorScheme.error,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            SectionDivider(text = "Nosacījumi")
            ExposedDropDownMenu(
                items = remember { listOf("Apļi", "Pielāgots") },
                selectedItem = terms,
                onSelectedItem = { terms = it },
                label = "Nosacījumu vieds",
                nullReplacement = "Izvēlieties nosacījumu viedu"
            )
            AnimatedVisibility(terms == "Pielāgots") {
                TextField(
                    value = customTerms ?: "",
                    onValueChange = {
                        customTerms = it.trim().takeUnless { it.isBlank() }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    label = { Text(text = "Nosacījumi") },
                    singleLine = false,
                )
            }
            AnimatedVisibility(terms == "Apļi") {
                TextField(
                    value = lapTerms?.toString() ?: "",
                    onValueChange = {
                        lapTerms = it.toIntOrNull().takeIf { it != null && it > 0 }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                    ),
                    label = { Text(text = "Apļu skaits") },
                    singleLine = true,
                )
            }
            SectionDivider(text = "Piezīmes")
            TextField(value = notes ?: "",
                onValueChange = {
                    notes = it.trim().takeUnless { it.isBlank() }
                },
                label = { Text("Piezīmes") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                singleLine = false,
                modifier = if (confirmBtnShown) Modifier.padding(bottom = 80.dp) else Modifier
            )
        }
    }
}