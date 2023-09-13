package cool.zolid.cardopoly.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.Loan
import cool.zolid.cardopoly.LoanTerms
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.Player
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.navigateWithoutTrace
import cool.zolid.cardopoly.ui.ExposedDropDownMenu
import cool.zolid.cardopoly.ui.SectionDivider
import cool.zolid.cardopoly.ui.Shapes
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.primaryButtonColors
import cool.zolid.cardopoly.ui.theme.Typography

@Composable
fun NewLoanScreen(navController: NavHostController) {
    val from = remember { mutableStateOf<Player?>(null) }
    val to = remember { mutableStateOf<Player?>(null) }
    val amount = remember { mutableStateOf<Int?>(null) }
    val amountToPayBack = remember { mutableStateOf<Int?>(null) }
    val terms = remember { mutableStateOf<String?>(null) }
    val customTerms = remember { mutableStateOf<String?>(null) }
    val lapTerms = remember { mutableStateOf<Int?>(null) }
    val notes = remember { mutableStateOf<String?>(null) }
    Scaffold(
        snackbarHost = { Snackbar.Host() },
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
                items = currentGame!!.players.filter { it != to.value },
                selectedItem = from,
                label = "Devējs",
                nullReplacement = "Izvēlieties devēju"
            )
            Box(
                Modifier
                    .fillMaxWidth()
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(270f)
                        .size(36.dp)
                        .align(Alignment.Center)
                )
                IconButton(
                    onClick = {
                        val tempPlayer = from.value
                        from.value = to.value
                        to.value = tempPlayer
                    }, modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painterResource(R.drawable.sync_alt),
                        contentDescription = null,
                        modifier = Modifier.rotate(90f),
                        tint = colorScheme.tertiary
                    )
                }
            }
            ExposedDropDownMenu(
                items = currentGame!!.players.filter { it != from.value },
                selectedItem = to,
                label = "Saņēmējs",
                nullReplacement = "Izvēlieties saņēmēju"
            )
            SectionDivider(text = "Summas")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(
                    value = amount.value?.toString() ?: "",
                    onValueChange = {
                        amount.value = it.toIntOrNull().takeIf { it != null && it > 0 }
                    },
                    label = { Text("Aizdevums") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    suffix = { Text(text = MONEY) },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = amountToPayBack.value?.toString() ?: "",
                    onValueChange = {
                        amountToPayBack.value = it.toIntOrNull().takeIf { it != null && it > 0 }
                    },
                    label = { Text("Atmaksa") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    suffix = { Text(text = MONEY) },
                    modifier = Modifier.weight(1f)
                )
            }
            AnimatedVisibility(amount.value != null && amountToPayBack.value != null && amountToPayBack.value!! < amount.value!!) {
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
                label = "Nosacījumu vieds",
                nullReplacement = "Izvēlieties nosacījumu viedu"
            )
            AnimatedVisibility(terms.value == "Pielāgots") {
                TextField(
                    value = customTerms.value ?: "",
                    onValueChange = {
                        customTerms.value = it.trim().takeUnless { it.isBlank() }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    label = { Text(text = "Nosacījumi") },
                    singleLine = false,
                )
            }
            AnimatedVisibility(terms.value == "Apļi") {
                TextField(
                    value = lapTerms.value?.toString() ?: "",
                    onValueChange = {
                        lapTerms.value = it.toIntOrNull().takeIf { it != null && it > 0 }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    label = { Text(text = "Apļu skaits") },
                    singleLine = true,
                )
            }
            SectionDivider(text = "Piezīmes")
            TextField(
                value = notes.value ?: "",
                onValueChange = {
                    notes.value = it.trim().takeUnless { it.isBlank() }
                },
                label = { Text("Piezīmes") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                singleLine = false,
            )
            Button(
                enabled = listOf(
                    from.value,
                    to.value,
                    amount.value,
                    amountToPayBack.value,
                    terms.value
                ).all { it != null } && if (terms.value!! == "Apļi") lapTerms.value != null else customTerms.value != null && amount.value!! <= amountToPayBack.value!!,
                onClick = {
                    currentGame!!.loans.add(
                        Loan(
                            from = from.value!!,
                            to = to.value!!,
                            amount = amount.value!!,
                            amountToPayBack = amountToPayBack.value!!,
                            terms = if (terms.value == "Apļi") LoanTerms.Laps(lapTerms.value!!) else LoanTerms.Custom(
                                customTerms.value!!
                            ),
                            notes = notes.value
                        )
                    )
                    navController.navigateWithoutTrace("game")
                },
                shape = Shapes.largeButton,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 15.dp, start = 8.dp, end = 8.dp, bottom = 10.dp
                    ),
                colors = primaryButtonColors
            ) {
                Text(
                    "Apstiprināt",
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}