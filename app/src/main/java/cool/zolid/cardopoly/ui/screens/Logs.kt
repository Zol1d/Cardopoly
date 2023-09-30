package cool.zolid.cardopoly.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.LogType
import cool.zolid.cardopoly.MONEY
import cool.zolid.cardopoly.NFCCardColorBindings
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.StaticPlayer
import cool.zolid.cardopoly.currentGame
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun LogsScreen(navController: NavHostController) {
    Scaffold(topBar = {
        StandardTopAppBar(
            title = "Darījumi",
            navController = navController
        )
    }, snackbarHost = { Snackbar.Host() }) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(extraPadding(pv, start = 15.dp, end = 15.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = currentGame?.logs.isNullOrEmpty()) {
                Text(
                    "Nav neviena ieraksta",
                    style = Typography.titleMedium,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            @Composable
            fun CardPlayer(player: StaticPlayer) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        painterResource(id = R.drawable.credit_card),
                        contentDescription = null,
                        tint = NFCCardColorBindings[player.card]
                            ?: Color.Unspecified
                    )
                    Text(player.name)
                }
            }

            LazyColumn(
                userScrollEnabled = true,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(currentGame?.logs?.reversed() ?: emptyList()) { _, log ->
                    when (log.type) {
                        LogType.ADD_MONEY, LogType.REMOVE_MONEY -> {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CardPlayer(player = log.player)
                                Text(
                                    text = "${if (log.type == LogType.ADD_MONEY) "+" else "-"}${log.amount!!}$MONEY",
                                    color = if (log.type == LogType.ADD_MONEY) colorScheme.tertiary else colorScheme.error
                                )
                            }
                        }

                        LogType.TRANSFER_MONEY, LogType.CREATE_LOAN, LogType.PAYBACK_LOAN -> {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    CardPlayer(player = log.player)
                                    Icon(
                                        Icons.Rounded.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(25.dp),
                                        tint = colorScheme.primary
                                    )
                                    CardPlayer(player = log.toPlayer!!)
                                }
                                Text(text = "${log.amount!!}$MONEY", color = colorScheme.tertiary)
                            }
                        }

                        LogType.REMOVE_PLAYER -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(text = "Spēlētājs", color = colorScheme.primary)
                                CardPlayer(player = log.player)
                                Text(text = "pameta spēli", color = colorScheme.primary)
                            }
                        }
                    }

                    Text(
                        text = "${
                            log.time.toLocalDateTime(TimeZone.currentSystemDefault())
                                .toJavaLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                        }${if (currentGame?.playerToMove?.value != null) " | ${log.lap}. aplis" else ""} | ${
                            when (log.type) {
                                LogType.ADD_MONEY -> "Pieskaitījums no valsts kases"
                                LogType.REMOVE_MONEY -> "Atskaitījums uz valsts kasi"
                                LogType.TRANSFER_MONEY -> "Pārskaitījums starp spēlētājiem"
                                LogType.REMOVE_PLAYER -> "Spēlētājs pametis spēli"
                                LogType.CREATE_LOAN -> "Izsniegts aizdevums"
                                LogType.PAYBACK_LOAN -> "Atmaksāts aizdevums"
                            }
                        }",
                        style = Typography.bodySmall,
                        color = colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )

                    Divider()
                }
            }
        }
    }
}