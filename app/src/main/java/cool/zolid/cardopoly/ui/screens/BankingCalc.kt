package cool.zolid.cardopoly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.ui.SectionDivider
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography
import kotlin.math.roundToInt

private enum class CalcMode {
    DIFFERENCE, PERCENTAGE, PROPERTY_MORTGAGE
}

@Composable
fun BankingCalcScreen(navController: NavHostController) {
    var curentMode by remember { mutableStateOf(CalcMode.DIFFERENCE) }

    Scaffold(snackbarHost = { Snackbar.Host() },
        topBar = { StandardTopAppBar(title = "Banķiera kalkulators", navController) }) { pv ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(extraPadding(pv)), horizontalAlignment = Alignment.End
        ) {
            @Composable
            fun RadioButtonModeChoice(mode: CalcMode, text: String, enabled: Boolean = true) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = curentMode == mode,
                            onClick = { curentMode = mode },
                            enabled = enabled
                        )
                        .padding(horizontal = 10.dp)
                        .align(Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = curentMode == mode,
                        onClick = { curentMode = mode },
                        enabled = enabled
                    )
                    Text(
                        text = text,
                        style = Typography.bodyLarge,
                        modifier = Modifier.padding(start = 5.dp),
                        color = if (enabled) Color.Unspecified else Typography.bodyLarge.color.takeOrElse {
                            LocalContentColor.current
                        }.copy(0.38f)
                    )
                }
                Divider()
            }
            SectionDivider(text = "Režīms", false)
            RadioButtonModeChoice(mode = CalcMode.DIFFERENCE, text = "Starpība")
            RadioButtonModeChoice(mode = CalcMode.PERCENTAGE, text = "Procenti")
            RadioButtonModeChoice(mode = CalcMode.PROPERTY_MORTGAGE, text = "Īpašumu ķīla", false)
            SectionDivider(
                text = when (curentMode) {
                    CalcMode.DIFFERENCE -> "Starpība"
                    CalcMode.PERCENTAGE -> "Procenti"
                    CalcMode.PROPERTY_MORTGAGE -> "Īpašumu ķīla"
                }
            )
            var sourceValue by remember { mutableStateOf<Int?>(null) }
            var source2Value by remember { mutableStateOf<Int?>(null) }
            when (curentMode) {
                CalcMode.DIFFERENCE -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        TextField(value = sourceValue?.toString() ?: "",
                            onValueChange = {
                                sourceValue = it.toIntOrNull()
                            },
                            label = { Text("Ievade") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            suffix = { Text(text = MONEY) },
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = " - ")
                        TextField(value = source2Value?.toString() ?: "",
                            onValueChange = {
                                source2Value = it.toIntOrNull()
                            },
                            label = { Text("Ievade 2") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            suffix = { Text(text = MONEY) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                CalcMode.PERCENTAGE -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        TextField(value = sourceValue?.toString() ?: "", onValueChange = {
                            sourceValue = it.toIntOrNull()
                        }, label = { Text("Ievade") }, keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                        ), singleLine = true, modifier = Modifier.weight(1f)
                        )
                        Text(text = " % no ")
                        TextField(value = source2Value?.toString() ?: "",
                            onValueChange = {
                                source2Value = it.toIntOrNull()
                            },
                            label = { Text("Ievade 2") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            suffix = { Text(text = MONEY) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                CalcMode.PROPERTY_MORTGAGE -> TODO()
            }
            SectionDivider(text = "Rezultāts")
            when (curentMode) {
                CalcMode.DIFFERENCE -> {
                    if (sourceValue != null && source2Value != null) {
                        Text(
                            text = "${sourceValue!! - source2Value!!}$MONEY",
                            style = Typography.headlineMedium,
                            color = colorScheme.tertiary
                        )
                    }
                }

                CalcMode.PERCENTAGE -> {
                    if (sourceValue != null && source2Value != null && sourceValue!! > 0 && source2Value!! > 0) {
                        Text(
                            text = "${((sourceValue!!.toDouble() / 100) * source2Value!!.toDouble()).roundToInt()}$MONEY",
                            style = Typography.headlineMedium,
                            color = colorScheme.tertiary
                        )
                    }
                }

                CalcMode.PROPERTY_MORTGAGE -> TODO()
            }
        }
    }
}