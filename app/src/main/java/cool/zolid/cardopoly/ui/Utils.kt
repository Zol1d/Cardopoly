package cool.zolid.cardopoly.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import cool.zolid.cardopoly.ui.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Snackbar {
    private var containerColorState by mutableStateOf<Color?>(null)
    private var errorState by mutableStateOf<Boolean>(false)
    private val hostState = SnackbarHostState()
    fun showSnackbarMsg(
        msg: String,
        error: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short,
        containerColor: Color? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            errorState = error
            containerColorState = containerColor
            hostState.showSnackbar(msg, duration = duration)
        }
    }

    @Composable
    fun Host(modifier: Modifier = Modifier) {
        val containerColor = if (errorState) colorScheme.errorContainer else containerColorState
            ?: colorScheme.tertiary
        SnackbarHost(
            hostState = hostState,
            snackbar = {
                Snackbar(
                    it,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = containerColor,
                    contentColor = contentColorFor(backgroundColor = containerColor),
                    modifier = modifier
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTopAppBar(
    title: String,
    navController: NavController?,
    subtitle: String? = null,
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    TopAppBar(
        title = {
            if (subtitle == null) {
                Text(title, fontSize = 22.sp, overflow = TextOverflow.Ellipsis, maxLines = 1)
            } else {
                Column(verticalArrangement = Arrangement.Bottom) {
                    Text(
                        title,
                        fontSize = 22.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        subtitle,
                        fontSize = 14.sp,
                        color = colorScheme.secondary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        },
        navigationIcon = {
            if (navController != null) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Atpakaļ",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = colorScheme.primary
        ),
        actions = actions,
    )
}

@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties()
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest,
        confirmButton,
        modifier.width(LocalConfiguration.current.screenWidthDp.times(0.9).dp),
        dismissButton,
        icon,
        title,
        text,
        shape,
        containerColor,
        iconContentColor,
        titleContentColor,
        textContentColor,
        tonalElevation,
        DialogProperties(
            properties.dismissOnBackPress,
            properties.dismissOnClickOutside,
            properties.securePolicy,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = properties.decorFitsSystemWindows
        )
    )
}

fun extraPadding(
    paddingValues: PaddingValues,
    start: Dp = 10.dp,
    end: Dp = 10.dp,
    top: Dp = 0.dp,
    bottom: Dp = 0.dp
): PaddingValues {
    return PaddingValues(
        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + start,
        end = paddingValues.calculateEndPadding(
            LayoutDirection.Ltr
        ) + end,
        top = paddingValues.calculateTopPadding() + top,
        bottom = paddingValues.calculateBottomPadding() + bottom
    )
}

object Shapes {
    val fab = RoundedCornerShape(10.dp)
    val listItem = RoundedCornerShape(4.dp)
    val largeButton = fab
}

val primaryButtonColors
    @Composable
    get() = ButtonDefaults.buttonColors(
        containerColor = colorScheme.primary,
        contentColor = colorScheme.onPrimary
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> ExposedDropDownMenu(
    items: Collection<T>,
    selectedItem: T?,
    onSelectedItem: (item: T) -> Unit,
    nullReplacement: String?,
    label: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
    ) {
        TextField(
            value = selectedItem?.toString() ?: nullReplacement ?: "",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    null,
                    Modifier.rotate(
                        animateFloatAsState(
                            targetValue = if (expanded) 180f else 0f,
                            label = ""
                        ).value
                    )
                )
            },
            modifier = Modifier.menuAnchor(),
            label = { Text(text = label ?: "") }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item.toString()) },
                    onClick = {
                        onSelectedItem(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ColumnScope.SectionDivider(text: String, padding: Boolean = true) {
    Text(
        text = text, style = Typography.bodyLarge, modifier = Modifier
            .align(Alignment.Start)
            .padding(top = if (padding) 10.dp else 0.dp)
    )
    Divider()
}

@Composable
fun ColumnScope.CategoryCard(text: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer
        )
    ) {
        Text(
            text = text, style = Typography.bodyLarge, modifier = Modifier
                .align(Alignment.Start)
//            .padding(top = if (padding) 10.dp else 0.dp)
        )
        content()
    }
}

object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(0.0f,0.0f,0.0f,0.0f)
}
