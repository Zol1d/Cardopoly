package cool.zolid.cardopoly.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.globalSettings
import cool.zolid.cardopoly.ui.theme.Typography
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun dialogRealEstateTaxCalculator(
    resultPaste: (result: Int) -> Unit
): () -> Unit {
    val realEstateList = remember { mutableStateListOf<Int>() }
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
    var dialogOpen by remember { mutableStateOf(false) }
    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = { Text("Nek. īpašumu kalk.") },
            text = {
                Column(
                    Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "${
                            if (realEstateList.isNotEmpty()) realEstateList.sumOf { (it * (globalSettings.realestateTaxPercent.intValue / 100f + 1f)).roundToInt() } else "--"
                        }$MONEY",
                        style = Typography.headlineMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val focusRequester = remember { FocusRequester() }
                        TextField(
                            value = textFieldValue,
                            onValueChange = {
                                textFieldValue = it
                            },
                            label = { Text("Jauns nek. īpašums") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Go, keyboardType = KeyboardType.Number
                            ),
                            keyboardActions = KeyboardActions(onGo = {
                                if (textFieldValue.text.toIntOrNull() != null && textFieldValue.text.toInt() > 0) {
                                    realEstateList.add(textFieldValue.text.toInt())
                                }
                            }),
                            suffix = { Text(text = MONEY) },
                            singleLine = true,
                            textStyle = Typography.bodyLarge,
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .weight(1f)
                                .padding(vertical = 10.dp)
                        )
                        IconButton(
                            onClick = { realEstateList.add(textFieldValue.text.toInt()) },
                            modifier = Modifier.requiredHeight(IntrinsicSize.Max),
                            enabled = textFieldValue.text.toIntOrNull() != null && textFieldValue.text.toInt() > 0
                        ) {
                            Icon(painterResource(id = R.drawable.add), null)
                        }
                        LaunchedEffect(true) {
                            focusRequester.requestFocus()
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        for (realEstate in realEstateList) {
                            var alpha by remember { mutableFloatStateOf(0f) }
                            InputChip(
                                onClick = { realEstateList.remove(realEstate) },
                                label = { Text(realEstate.toString()) },
                                selected = true,
                                modifier = Modifier.alpha(
                                    animateFloatAsState(
                                        targetValue = alpha,
                                        label = ""
                                    ).value
                                ),
                                avatar = {
                                    Icon(
                                        painterResource(id = R.drawable.home),
                                        contentDescription = null,
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = null
                                    )
                                },
                            )
                            LaunchedEffect(true) {
                                alpha = 1f
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resultPaste(realEstateList.sumOf { (it * (globalSettings.realestateTaxPercent.intValue / 100f + 1f)).roundToInt() })
                        dialogOpen = false
                    },
                    enabled = realEstateList.isNotEmpty()
                ) {
                    Text("Ievietot".uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogOpen = false }) {
                    Text("Atcelt".uppercase())
                }
            }
        )
    }
    return {
        dialogOpen = true
    }
}