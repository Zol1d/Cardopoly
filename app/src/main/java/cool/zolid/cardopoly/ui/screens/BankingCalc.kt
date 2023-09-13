package cool.zolid.cardopoly.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.ui.SectionDivider
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography

private enum class CalcMode {
    DIFFERENCE,
    PERCENTAGE,
    PROPERTY_MORTGAGE
}

@Composable
fun BankingCalcScreen(navController: NavHostController) {
    var curentMode by remember { mutableStateOf(CalcMode.DIFFERENCE) }

    Scaffold(
        snackbarHost = { Snackbar.Host() },
        topBar = { StandardTopAppBar(title = "Banķiera kalkulators", navController) }) { pv ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(extraPadding(pv)), horizontalAlignment = Alignment.CenterHorizontally
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
            RadioButtonModeChoice(mode = CalcMode.PERCENTAGE, text = "Procenti / Daļa")
            RadioButtonModeChoice(mode = CalcMode.PROPERTY_MORTGAGE, text = "Īpašumu ķīla", false)
            when (curentMode) {
                CalcMode.DIFFERENCE -> {

                }

                CalcMode.PERCENTAGE -> {

                }

                CalcMode.PROPERTY_MORTGAGE -> TODO()
            }
        }
    }
}