package cool.zolid.cardopoly.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.ui.theme.Typography
import org.mariuszgromada.math.mxparser.Expression
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun dialogCalculator(
    resultPaste: (result: Int) -> Unit,
    initialExpr: () -> String = { "" },
    initiallyOpen: Boolean = false
): () -> Unit {
    var expression by remember { mutableStateOf(TextFieldValue("")) }
    var dialogOpen by remember { mutableStateOf(initiallyOpen) }
    if (dialogOpen) {
        LaunchedEffect(true) {
            if (expression.text != "") return@LaunchedEffect
            expression = TextFieldValue(
                initialExpr(),
                selection = TextRange(initialExpr().length)
            )
        }
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = { Text("Kalkulators") },
            text = {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${
                            if (!Expression(expression.text).calculate().isNaN()) Expression(
                                expression.text
                            ).calculate().roundToInt() else "--"
                        }$MONEY",
                        style = Typography.headlineMedium,
                        color = colorScheme.tertiary,
                        modifier = Modifier.align(Alignment.End)
                    )
                    val focusRequester = remember { FocusRequester() }
                    TextField(
                        value = expression,
                        onValueChange = {
                            expression = it
                        },
                        label = { Text("Darbība") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        textStyle = Typography.bodyLarge,
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .padding(vertical = 10.dp)
                    )
                    LaunchedEffect(true) {
                        focusRequester.requestFocus()
                    }
                    @Composable
                    fun ExpressionButton(text: Char) {
                        TextButton(
                            onClick = {
                                expression = TextFieldValue(
                                    expression.text.toMutableList().apply {
                                        add(
                                        expression.selection.start,
                                        text
                                    )
                                    }.joinToString(""),
                                    TextRange(expression.text.length + 1)
                                )
                            },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary
                            ),
                            shape = CircleShape
                        ) {
                            Text(text = text.toString(), style = Typography.bodyLarge)
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExpressionButton('+')
                        ExpressionButton('-')
                        ExpressionButton('×')
                        ExpressionButton('÷')
                        ExpressionButton('(')
                        ExpressionButton(')')
                        ExpressionButton('%')
                        TextButton(
                            onClick = {
                                expression = TextFieldValue("")
                            },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = colorScheme.errorContainer,
                                contentColor = colorScheme.onErrorContainer
                            ),
                            shape = CircleShape
                        ) {
                            Text(text = "C", style = Typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resultPaste(Expression(expression.text).calculate().roundToInt())
                        dialogOpen = false
                    },
                    enabled = !Expression(expression.text).calculate().isNaN()
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