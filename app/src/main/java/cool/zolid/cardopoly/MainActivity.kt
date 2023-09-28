package cool.zolid.cardopoly

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cool.zolid.cardopoly.ui.AlertDialog
import cool.zolid.cardopoly.ui.screens.BankingCalcScreen
import cool.zolid.cardopoly.ui.screens.CardsScreen
import cool.zolid.cardopoly.ui.screens.GameScreen
import cool.zolid.cardopoly.ui.screens.HomeScreen
import cool.zolid.cardopoly.ui.screens.LoansScreen
import cool.zolid.cardopoly.ui.screens.LogsScreen
import cool.zolid.cardopoly.ui.screens.NewLoanScreen
import cool.zolid.cardopoly.ui.screens.StartGameScreen
import cool.zolid.cardopoly.ui.theme.AppTheme
import cool.zolid.cardopoly.ui.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.mariuszgromada.math.mxparser.License
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.abs
import kotlinx.serialization.json.Json as DefaultJson

const val MONEY = " M\$"
val nfcNotAvialableDialogOpen: MutableState<Triple<String, String, Boolean>?> = mutableStateOf(null)
var NFCEnabled by mutableStateOf(false)
val nfcApiSubscribers = mutableListOf<(b64id: String) -> Unit>()
val MonopolyColors = mapOf(
    Color.Red to "Sarkanā",
    Color.Blue to "Zilā",
    Color.Green to "Zaļā",
    Color.Yellow to "Dzeltenā",
    Color.Cyan to "Gaiši zilā",
    Color.Magenta to "Violetā"
)
val Json = DefaultJson { allowStructuredMapKeys = true }
var NFCCardColorBindings = mapOf<String, @Serializable(with = ColorSerializer::class) Color>()

class ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeInt())
    override fun serialize(encoder: Encoder, value: Color) = encoder.encodeInt(value.toArgb())
}

val Context.nfcCardBindingDataStore: DataStore<Preferences> by preferencesDataStore(name = "cards")
val Context.gameRecoveryDataStore: DataStore<Preferences> by preferencesDataStore(name = "gamerecovery")

data class Player(val name: String, val card: String?, val money: MutableIntState) {
    override fun toString(): String = name
}

@Serializable
data class HistoricPlayer(val name: String, val card: String?) {
    override fun toString(): String = name
}

@Serializable
data class StaticPlayer(val name: String, val card: String?, val money: Int) {
    constructor(player: Player) : this(player.name, player.card, player.money.intValue)
}

@Serializable
sealed class LoanTerms {
    @Serializable
    data class Laps(val laps: Int) : LoanTerms()

    @Serializable
    data class Custom(val terms: String) : LoanTerms()
}

data class Loan(
    val from: Player,
    val to: Player,
    val amount: Int,
    val amountToPayBack: Int,
    val terms: LoanTerms,
    val notes: String?
) {
    init {
        require(from != to) { "Loan: \"from\" cant be equal to \"to\"" }
        require(amount <= amountToPayBack) { "Loan: \"amountToPayBack\" should be more or equal to \"amount\"" }
    }
}

@Serializable
data class StaticLoan(
    val from: StaticPlayer,
    val to: StaticPlayer,
    val amount: Int,
    val amountToPayBack: Int,
    val terms: LoanTerms,
    val notes: String?
) {
    constructor(loan: Loan) : this(
        StaticPlayer(loan.from),
        StaticPlayer(loan.to),
        loan.amount,
        loan.amountToPayBack,
        loan.terms,
        loan.notes
    )
}

@Serializable
enum class LogType {
    ADD_MONEY,
    REMOVE_MONEY,
    TRANSFER_MONEY,
    REMOVE_PLAYER,
    CREATE_LOAN,
    PAYBACK_LOAN
}

@Serializable
data class Log(
    val type: LogType,
    // Players can be static since their money wont be needed
    val player: StaticPlayer,
    val toPlayer: StaticPlayer?,
    val amount: Int?,
    val time: kotlinx.datetime.Instant = Clock.System.now()
)

data class Game(
    val cardsSupport: Boolean,
    val playerToMove: MutableState<Player?>,
    val lap: MutableIntState,
    val players: SnapshotStateList<Player>,
    val historicPlayers: SnapshotStateList<HistoricPlayer>,
    val loans: SnapshotStateList<Loan>,
    val logs: MutableList<Log>,
    val startMillis: Long = System.currentTimeMillis(),
) {
    init {
        require(players.size > 1) { "Game: Players cant be less than 2" }
    }
}

@Serializable
data class StaticGame(
    val cardsSupport: Boolean,
    val playerToMove: StaticPlayer?,
    val lap: Int,
    val players: List<StaticPlayer>,
    val historicPlayers: List<HistoricPlayer>,
    val startMillis: Long,
    val loans: List<StaticLoan>,
    val logs: List<Log>
) {
    constructor(game: Game) : this(
        game.cardsSupport,
        if (game.playerToMove.value != null) StaticPlayer(game.playerToMove.value!!) else null,
        game.lap.intValue,
        game.players.map { StaticPlayer(it) },
        game.historicPlayers,
        game.startMillis,
        game.loans.map { StaticLoan(it) }, game.logs
    )
}

var currentGame by mutableStateOf<Game?>(null)

fun NavController.navigateWithoutTrace(route: String) = CoroutineScope(Dispatchers.Main).launch {
    navigate(route) {
        popUpTo(0) {
            inclusive = true
        }
    }
}

object Beep {
    fun moneyAdd() {
        CoroutineScope(Dispatchers.IO).launch {
            ToneGenerator(
                AudioManager.STREAM_MUSIC,
                100
            ).startTone(ToneGenerator.TONE_DTMF_7, 150)
            delay(200)
            ToneGenerator(
                AudioManager.STREAM_MUSIC,
                100
            ).startTone(ToneGenerator.TONE_DTMF_6, 150)
        }
    }

    fun moneyRemove() {
        CoroutineScope(Dispatchers.IO).launch {
            ToneGenerator(
                AudioManager.STREAM_MUSIC,
                100
            ).startTone(ToneGenerator.TONE_DTMF_6, 150)
            delay(200)
            ToneGenerator(
                AudioManager.STREAM_MUSIC,
                100
            ).startTone(ToneGenerator.TONE_DTMF_7, 150)
        }
    }

    fun error() {
        CoroutineScope(Dispatchers.IO).launch {
            ToneGenerator(
                AudioManager.STREAM_MUSIC,
                100
            ).startTone(ToneGenerator.TONE_DTMF_0, 150)
            delay(200)
            ToneGenerator(
                AudioManager.STREAM_MUSIC,
                100
            ).startTone(ToneGenerator.TONE_DTMF_0, 150)
        }
    }
}

class MainActivity : ComponentActivity() {
    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(ExperimentalEncodingApi::class)
    private fun handleNFCEvent(tag: Tag) {
        if (nfcApiSubscribers.isNotEmpty()) {
            nfcApiSubscribers.last().invoke(Base64.encode(tag.id))
        }
    }

    override fun onStop() {
        super.onStop()
        if (currentGame != null) {
            runBlocking {
                gameRecoveryDataStore.edit {
                    it[stringPreferencesKey("savedgame")] =
                        Json.encodeToString(StaticGame(currentGame!!))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val adapter = NfcAdapter.getDefaultAdapter(this)
        nfcNotAvialableDialogOpen.value = if (adapter == null) {
            Triple(
                "NFC nav piejams",
                "Šī ierīce neatbalsta NFC, lielākā daļa aplikācijas nebūs strādājoša",
                false
            )
        } else if (!adapter.isEnabled) {
            Triple(
                "NFC nav ieslēgts",
                "NFC nav ieslēgts, tādēļ aplikācija var nedarboties pareizi",
                true
            )
        } else {
            adapter.enableReaderMode(this, ::handleNFCEvent, NfcAdapter.FLAG_READER_NFC_A, Bundle())
            null
        }
        NFCEnabled = nfcNotAvialableDialogOpen.value == null
    }

    override fun onPause() {
        super.onPause()
        NfcAdapter.getDefaultAdapter(this).disableReaderMode(this)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        License.iConfirmNonCommercialUse("Zolid")
        var gameRecoveryDialogOpen by mutableStateOf(false)
        CoroutineScope(Dispatchers.IO).launch {
            gameRecoveryDialogOpen =
                gameRecoveryDataStore.data.first().contains(stringPreferencesKey("savedgame"))
            NFCCardColorBindings = Json.decodeFromString(
                nfcCardBindingDataStore.data.first()[stringPreferencesKey("cards")] ?: return@launch
            )
        }
        setContent {
            AppTheme {
                val navController = rememberNavController()
                if (nfcNotAvialableDialogOpen.value != null) {
                    AlertDialog(
                        onDismissRequest = { nfcNotAvialableDialogOpen.value = null },
                        title = {
                            Text(
                                nfcNotAvialableDialogOpen.value?.first ?: "", fontSize = 20.sp
                            )
                        },
                        text = { Text(nfcNotAvialableDialogOpen.value?.second ?: "") },
                        icon = {
                            Icon(
                                Icons.Rounded.Warning,
                                contentDescription = null,
                                Modifier.size(40.dp),
                                tint = colorScheme.onErrorContainer
                            )
                        },
                        confirmButton = {
                            if (nfcNotAvialableDialogOpen.value?.third == true) {
                                TextButton(onClick = {
                                    nfcNotAvialableDialogOpen.value = null
                                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                                }) {
                                    Text("Atvērt iestatījumus".uppercase())
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { nfcNotAvialableDialogOpen.value = null }) {
                                Text("Labi".uppercase())
                            }
                        },
                    )
                }
                if (gameRecoveryDialogOpen) {
                    val cs = rememberCoroutineScope()
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Spēles atkopšana", fontSize = 20.sp) },
                        text = { Text("Krātuvē tika atrasti nepabeigtas spēles dati, vai vēlaties šo spēli turpināt?") },
                        icon = {
                            Icon(
                                Icons.Rounded.Warning,
                                contentDescription = null,
                                Modifier.size(40.dp),
                                tint = colorScheme.error
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                cs.launch {
                                    gameRecoveryDataStore.edit { prefs ->
                                        val staticGame = Json.decodeFromString<StaticGame>(
                                            prefs[stringPreferencesKey("savedgame")]!!
                                        )
                                        val players = staticGame.players.map {
                                            Player(
                                                it.name,
                                                it.card,
                                                mutableIntStateOf(it.money)
                                            )
                                        }
                                        // Associate player objects with loan player objects so money is synced
                                        currentGame = Game(
                                            staticGame.cardsSupport,
                                            mutableStateOf(if (staticGame.playerToMove != null) players.find { it1 -> it1.money.intValue == staticGame.playerToMove.money && it1.card == staticGame.playerToMove.card && it1.name == staticGame.playerToMove.name } else null),
                                            mutableIntStateOf(staticGame.lap),
                                            players.toMutableStateList(),
                                            staticGame.historicPlayers.toMutableStateList(),
                                            staticGame.loans.map {
                                                Loan(
                                                    players.find { it1 ->
                                                        it1.money.intValue == it.from.money && it1.card == it.from.card && it1.name == it.from.name
                                                    }!!,
                                                    players.find { it1 -> it1.money.intValue == it.to.money && it1.card == it.to.card && it1.name == it.to.name }!!,
                                                    it.amount,
                                                    it.amountToPayBack,
                                                    it.terms,
                                                    it.notes
                                                )
                                            }.toMutableStateList(),
                                            staticGame.logs.toMutableList(),
                                            staticGame.startMillis,
                                        )
                                    }
                                    gameRecoveryDialogOpen = false
                                }
                            }) {
                                Text("Atkopt datus".uppercase(), color = colorScheme.tertiary)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                cs.launch { gameRecoveryDataStore.edit { it.clear() } }
                                gameRecoveryDialogOpen = false
                            }) {
                                Text("Atcelt".uppercase())
                            }
                        },
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxSize(), color = colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CompositionLocalProvider(
                            LocalMinimumInteractiveComponentEnforcement provides false,
                        ) {
                            val surfaceColor by animateColorAsState(
                                targetValue = if (currentGame != null) colorScheme.tertiary else colorScheme.background,
                                label = ""
                            )
                            window.statusBarColor = surfaceColor.toArgb()
                            WindowCompat.getInsetsController(
                                window, LocalView.current
                            ).isAppearanceLightStatusBars = surfaceColor.luminance() > 0.25
                            if (currentGame != null) {
                                var timePassed by remember { mutableStateOf("00:00") }
                                LaunchedEffect(true) {
                                    while (true) {
                                        val diff = abs(
                                            System.currentTimeMillis() - (currentGame?.startMillis
                                                ?: 0)
                                        )
                                        val diffHours = diff / (60 * 60 * 1000)
                                        val diffMins = (diff % (60 * 60 * 1000)) / (60 * 1000)
                                        timePassed = "${diffHours}h ${diffMins}m"
                                        delay(60000)
                                    }
                                }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 5.dp),
                                    color = surfaceColor,
                                    shape = RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = 0.dp,
                                        bottomStart = 16.dp,
                                        bottomEnd = 16.dp
                                    ),
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Cardopoly - ${if (currentGame?.playerToMove != null) "aplis nr. ${currentGame?.lap?.intValue ?: 0}" else "spēle ${if (currentGame?.cardsSupport == true) "ar" else "bez"} kartēm"} | $timePassed",
                                            modifier = Modifier.padding(vertical = 5.dp),
                                            style = Typography.bodyMedium,
                                            fontSize = 16.sp,
                                            color = colorScheme.onTertiary
                                        )
                                    }
                                }
                            }
                        }
                        NavHost(navController = navController,
                            startDestination = if (currentGame != null) "game" else "home",
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None }) {
                            composable("home") { HomeScreen(navController) }
                            composable(
                                "startgame?cards_enabled={cards_enabled}",
                                arguments = listOf(navArgument("cards_enabled") {
                                    type = NavType.BoolType
                                })
                            ) {
                                StartGameScreen(
                                    navController, it.arguments!!.getBoolean("cards_enabled")
                                )
                            }
                            composable("game") { GameScreen(navController) }
                            composable("loans") { LoansScreen(navController) }
                            composable("cards") { CardsScreen(navController) }
                            composable("newloan") { NewLoanScreen(navController) }
                            composable("bankingcalc") { BankingCalcScreen(navController) }
                            composable("logs") { LogsScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}
