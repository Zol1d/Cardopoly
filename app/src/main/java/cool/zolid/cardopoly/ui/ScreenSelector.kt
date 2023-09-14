package cool.zolid.cardopoly.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cool.zolid.cardopoly.ui.theme.Typography

data class ScreenItem(
    val route: String?,
    val label: String,
    @DrawableRes val icon: Int,
    val enabled: Boolean = true,
    val onClick: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSelector(
    rowList: List<List<ScreenItem>>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        modifier = modifier
    ) {
        for (row in rowList) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (screenItem in row) {
                    ElevatedCard(
                        onClick = {
                            if (screenItem.route != null) {
                                navController.navigate(screenItem.route)
                            }
                            screenItem.onClick()
                        }, modifier = Modifier
                            .fillMaxWidth().weight(1f),
                        enabled = screenItem.enabled,
                        colors = CardDefaults.elevatedCardColors(contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                painterResource(screenItem.icon),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .padding(top = 5.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    screenItem.label,
                                    textAlign = TextAlign.Center,
                                    style = Typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}