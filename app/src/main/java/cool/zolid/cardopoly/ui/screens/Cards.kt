package cool.zolid.cardopoly.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavHostController
import cool.zolid.cardopoly.Json
import cool.zolid.cardopoly.MonopolyColors
import cool.zolid.cardopoly.NFCCardColorBindings
import cool.zolid.cardopoly.R
import cool.zolid.cardopoly.nfcApiSubscribers
import cool.zolid.cardopoly.nfcCardBindingDataStore
import cool.zolid.cardopoly.ui.AlertDialog
import cool.zolid.cardopoly.ui.ExposedDropDownMenu
import cool.zolid.cardopoly.ui.Shapes
import cool.zolid.cardopoly.ui.Snackbar
import cool.zolid.cardopoly.ui.StandardTopAppBar
import cool.zolid.cardopoly.ui.extraPadding
import cool.zolid.cardopoly.ui.theme.Typography
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalEncodingApi::class
)
@Composable
fun CardsScreen(navController: NavHostController) {
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val localNFCCardColorBindings = remember {
        mutableStateMapOf(*NFCCardColorBindings.entries.map { Pair(it.key, it.value) }
            .toTypedArray())
    }
    DisposableEffect(true) {
        fun processNFC(b64id: String) {
            if (b64id in localNFCCardColorBindings.keys) {
                Snackbar.showSnackbarMsg(
                    "Karte atrasta - ${MonopolyColors[localNFCCardColorBindings[b64id]]}",
                    containerColor = localNFCCardColorBindings[b64id]
                )
            } else {
                Snackbar.showSnackbarMsg("Karte nav reģistrēta", true)
            }
        }
        nfcApiSubscribers.add(::processNFC)
        onDispose {
            nfcApiSubscribers.remove(::processNFC)
        }
    }
    var addDialogOpen by remember { mutableStateOf(false) }
    var removeDialogOpen by remember { mutableStateOf<String?>(null) }

    if (addDialogOpen) {
        var cardColor by remember { mutableStateOf<String?>(null) }
        var cardUid by remember { mutableStateOf<String?>(null) }
        DisposableEffect(true) {
            fun processNFC(b64id: String) {
                if (cardUid == null && b64id !in localNFCCardColorBindings) {
                    cardUid = b64id
                }
            }
            nfcApiSubscribers.add(::processNFC)
            onDispose {
                nfcApiSubscribers.remove(::processNFC)
            }
        }
        AlertDialog(
            onDismissRequest = { addDialogOpen = false },
            title = { Text("Pievienot karti") },
            text = {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExposedDropDownMenu(
                        items = MonopolyColors.filter { it.value != cardColor && it.key !in localNFCCardColorBindings.values }.values,
                        selectedItem = cardColor,
                        label = "Krāsa",
                        onSelectedItem = { cardColor = it },
                        nullReplacement = "Izvēlieties krāsu",
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    if (cardUid == null) {
                        Icon(
                            painterResource(id = R.drawable.round_contactless),
                            "NFC",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(bottom = 5.dp)
                        )
                        Text(text = "Pietuviniet karti tālruņa aizmugurei")
                    } else {
                        Text(
                            text = "Karte nolasīta",
                            color = colorScheme.tertiary,
                            style = Typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        localNFCCardColorBindings[cardUid!!] =
                            MonopolyColors.filterValues { it == cardColor!! }.keys.first()
                        NFCCardColorBindings = localNFCCardColorBindings.toMap()
                        coroutineScope.launch {
                            ctx.nfcCardBindingDataStore.edit {
                                it[stringPreferencesKey("cards")] =
                                    Json.encodeToString(NFCCardColorBindings)
                            }
                        }
                        addDialogOpen = false
                    },
                    enabled = cardColor != null && cardUid != null
                ) {
                    Text("Apstiprināt".uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = { addDialogOpen = false }) {
                    Text("Atcelt".uppercase())
                }
            }
        )
    }
    if (removeDialogOpen != null) {
        AlertDialog(
            onDismissRequest = { removeDialogOpen = null },
            title = { Text("Vai tiešām noņemt karti \"${MonopolyColors[localNFCCardColorBindings[removeDialogOpen]]}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        localNFCCardColorBindings.remove(removeDialogOpen)
                        NFCCardColorBindings = localNFCCardColorBindings.toMap()
                        coroutineScope.launch {
                            ctx.nfcCardBindingDataStore.edit {
                                it[stringPreferencesKey("cards")] =
                                    Json.encodeToString(NFCCardColorBindings)
                            }
                        }
                        removeDialogOpen = null
                    }
                ) {
                    Text("Apstiprināt".uppercase(), color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { removeDialogOpen = null }) {
                    Text("Atcelt".uppercase())
                }
            }
        )
    }
    Scaffold(topBar = {
        StandardTopAppBar(
            title = "Kartes",
            navController = navController,
        )
    }, snackbarHost = { Snackbar.Host() },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (localNFCCardColorBindings.size < MonopolyColors.size) {
                ExtendedFloatingActionButton(
                    onClick = { addDialogOpen = true },
                    shape = Shapes.fab,
                    containerColor = colorScheme.tertiaryContainer,
                    contentColor = colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add")
                    Text("Pievienot karti", modifier = Modifier.padding(start = 10.dp))
                }
            }
        }) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(extraPadding(pv)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = localNFCCardColorBindings.isEmpty()) {
                Text(
                    "Nav nevienas kartes",
                    style = Typography.titleMedium,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            LazyColumn(userScrollEnabled = true) {
                itemsIndexed(localNFCCardColorBindings.toList()) { _, (tagId, cardColor) ->
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentEnforcement provides false,
                    ) {
                        ElevatedButton(
                            onClick = {
                                removeDialogOpen = tagId
                            },
                            shape = Shapes.listItem,
                            contentPadding = PaddingValues(
                                horizontal = 13.dp,
                                vertical = 7.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .animateItemPlacement()
                        ) {
                            Box(Modifier.fillMaxWidth()) {
                                Icon(
                                    painterResource(id = R.drawable.credit_card),
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    tint = cardColor
                                )
                                Text(
                                    MonopolyColors[cardColor]
                                        ?: "Kļūda: Krāsa nav atļauto sarakstā",
                                    style = Typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}