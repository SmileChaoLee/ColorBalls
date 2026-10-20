package com.smile.reversi.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smile.colorballs_main.R
import com.smile.colorballs_main.views.CbComposable
import com.smile.colorballs_main.views.ui.theme.DarkGreen
import com.smile.colorballs_main.views.ui.theme.DarkRed
import com.smile.colorballs_main.views.ui.theme.Yellow2

private val TitleColor = Color(0xff0099cc)
private val LabelColor = Color.Blue
private val ActionButtonColor = Color.White
private val ActionButtonTextColor = Color.White

/** Page frame shared by the Nearby and Online two-player screens: title on top, content below. */
@Composable
fun TwoPlayersFrame(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Yellow2),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).weight(1.6f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = TitleColor,
                fontWeight = FontWeight.Bold,
                fontSize = CbComposable.mFontSize * 1.2f
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun TwoPlayersLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = LabelColor,
        fontSize = CbComposable.mFontSize,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 10.dp)
    )
}

/** Labeled text field drawn on the gradient bar image. */
@Composable
fun TwoPlayersTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TwoPlayersLabel(label, Modifier.fillMaxWidth())
        // wraps the name (a bit wider than the text) and grows taller when the name needs more lines
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 10.dp)
                .heightIn(min = 48.dp)
                .paint(painterResource(R.drawable.reversi_edittext_bar),
                    contentScale = ContentScale.FillBounds),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                textStyle = TextStyle(color = Color.Black, fontSize = CbComposable.mFontSize,
                    textAlign = TextAlign.Center),
                cursorBrush = SolidColor(Color.Black),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun TwoPlayersButton(
    text: String,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val w = minOf(maxWidth * 0.5f, 220.dp)
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .width(w)
                .aspectRatio(200f / 80f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ActionButtonColor,
                contentColor = ActionButtonTextColor,
                disabledContainerColor = ActionButtonColor,
                disabledContentColor = ActionButtonTextColor
            ),
            border = BorderStroke(2.dp, textColor),
            contentPadding = PaddingValues(0.dp)
        ) {
            FitButtonText(text, textColor)
        }
    }
}

@Composable
fun CreateButton(onClick: () -> Unit, enabled: Boolean = true) =
    TwoPlayersButton(
        stringResource(R.string.createStr), DarkGreen, onClick, enabled = enabled
    )

@Composable
fun JoinButton(onClick: () -> Unit, enabled: Boolean = true) =
    TwoPlayersButton(
        stringResource(R.string.joinStr), DarkGreen, onClick, enabled = enabled
    )

@Composable
fun LeaveButton(onClick: () -> Unit) =
    TwoPlayersButton(stringResource(R.string.leaveStr), DarkRed, onClick)

/** First page: player name, then Create / Join / Leave. */
@Composable
fun TwoPlayersEntry(
    title: String,
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    message: String,
    busy: Boolean,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit
) {
    TwoPlayersFrame(title) {
      BoxWithConstraints(Modifier.fillMaxWidth().weight(4.5f)) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = maxHeight).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
        TwoPlayersTextField(
            label = stringResource(R.string.playerNameLabel),
            value = playerName,
            onValueChange = onPlayerNameChange
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CreateButton(onCreate, enabled = !busy)
            JoinButton(onJoin, enabled = !busy)
            LeaveButton(onLeave)
        }
        }
      }
      MessageArea(message, Modifier.weight(1.1f))
    }
}

/** Centered message area; wraps cleanly with a clear line gap and shrinks the font to fit. */
@Composable
private fun MessageArea(message: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (message.isEmpty()) return@BoxWithConstraints
        val text = message.replace("_", "_\u200B").replace(":", ":\u200B")
        var scale by remember(text, maxWidth, maxHeight) { mutableStateOf(1f) }
        val fontSize = (CbComposable.mFontSize.value * scale).sp
        Text(
            text = text,
            color = Color.Black,
            fontSize = fontSize,
            lineHeight = fontSize * 1.3f,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = { r ->
                val h = r.size.height
                val maxPx = constraints.maxHeight
                if ((r.didOverflowWidth || h > maxPx) && scale > 0.3f) scale *= 0.9f
            }
        )
    }
}

data class ListEntry(val id: String, val text: String)

/** Small button for the bottom row of the list pages; share the row width equally. */
@Composable
fun RowScope.ListPageButton(
    text: String,
    textColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 6.dp)
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ActionButtonColor,
            contentColor = ActionButtonTextColor,
            disabledContainerColor = ActionButtonColor,
            disabledContentColor = ActionButtonTextColor
        ),
        border = BorderStroke(2.dp, textColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        FitButtonText(text, textColor)
    }
}

@Composable
private fun FitButtonText(text: String, color: Color) {
    var fontSize by remember(text) { mutableStateOf(CbComposable.mFontSize) }
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { result ->
            if ((result.didOverflowWidth || result.didOverflowHeight) && fontSize > 8.sp) {
                fontSize = (fontSize.value * 0.9f).sp
            }
        }
    )
}

/**
 * Page with the player name, a selectable list (opponents or created games) and a button row.
 * Used for "create" (opponents asking to join) and "join" (games to join).
 */
@Composable
fun TwoPlayersListPage(
    title: String,
    playerName: String,
    entries: List<ListEntry>,
    selectedId: String?,
    onSelect: (ListEntry) -> Unit,
    message: String,
    bannerId: String,
    buttons: @Composable RowScope.() -> Unit
) {
    TwoPlayersFrame(title) {
        Spacer(Modifier.weight(0.3f))
        TwoPlayersTextField(
            label = stringResource(R.string.playerNameLabel),
            value = playerName,
            onValueChange = {},
            readOnly = true
        )
        Box(
            modifier = Modifier
                .padding(top = 20.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .weight(4f)
                .background(Color(0xffd4d28a))
                .border(BorderStroke(2.dp, DarkRed))
        ) {
            LazyColumn(Modifier.fillMaxSize()) {
                items(entries, key = { it.id }) { e ->
                    Text(
                        text = e.text,
                        color = Color.Black,
                        fontSize = CbComposable.mFontSize,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (e.id == selectedId) Color(0x660000ff) else Color.Transparent)
                            .clickable { onSelect(e) }
                            .padding(10.dp)
                    )
                }
            }
        }
        MessageArea(message, Modifier.weight(1.2f))
        Box(Modifier.weight(2f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            CbComposable.ShowAdmobBanner(modifier = Modifier.padding(top = 0.dp), bannerId, 0)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            content = buttons
        )
    }
}
