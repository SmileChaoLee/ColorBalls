package com.smile.reversi.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smile.colorballs_main.BaseApp
import com.smile.colorballs_main.R
import com.smile.colorballs_main.views.CbComposable
import com.smile.colorballs_main.views.ui.theme.ColorBallsTheme
import com.smile.colorballs_main.views.ui.theme.DarkGreen
import com.smile.colorballs_main.views.ui.theme.DarkRed
import com.smile.colorballs_main.views.ui.theme.Yellow2
import com.smile.reversi.constants.ReversiConstants
import com.smile.reversi.network.ConnectionState
import com.smile.reversi.network.GameTransport
import com.smile.reversi.network.NearbyTransport
import com.smile.reversi.network.NetPermissions
import com.smile.reversi.network.Role
import com.smile.reversi.network.RoomTransport
import com.smile.reversi.network.TransportHolder
import kotlinx.coroutines.launch

/**
 * Two-player entry screens ("Nearby Two Players" and "Online Two Players").
 * Page 1: player name + Create / Join / Leave. Page 2: list of opponents (create) or games (join).
 * Opens the game screen once connected.
 */
class LobbyActivity : ComponentActivity() {

    companion object {
        const val EXTRA_MODE = "LobbyMode"
    }

    private enum class Step { ENTRY, HOSTING, JOINING }

    private val bannerId: String
        get() = (application as? BaseApp)?.getAdMobBannerID() ?: ""

    private var handedOff = false
    private var pending: GameTransport? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        lockPortraitOnPhones()
        super.onCreate(savedInstanceState)
        val mode = intent.getIntExtra(EXTRA_MODE, ReversiConstants.ONLINE_NEARBY)
        setContent {
            ColorBallsTheme {
                Scaffold(containerColor = Yellow2) { padding ->
                    Column(Modifier.padding(padding)) {
                        if (mode == ReversiConstants.ONLINE_NEARBY) NearbyLobby() else RoomLobby()
                    }
                }
            }
        }
    }

    private fun startGame(mode: Int, transport: GameTransport, role: Role, name: String) {
        if (handedOff) return
        handedOff = true
        TransportHolder.set(transport, role, name)
        Intent(this, ReversiActivity::class.java).also {
            it.putExtra(ReversiConstants.PLAY_MODE, mode)
            startActivity(it)
        }
        finish()
    }

    private fun setPending(t: GameTransport?) {
        pending?.close()
        pending = t
    }

    @Composable
    private fun WatchState(
        transport: GameTransport?, mode: Int, role: Role, name: String,
        onFailed: (String) -> Unit
    ) {
        transport ?: return
        val state by transport.state.collectAsState()
        LaunchedEffect(state) {
            when (val s = state) {
                ConnectionState.Connected -> startGame(mode, transport, role, name)
                is ConnectionState.Failed -> onFailed(s.reason)
                else -> {}
            }
        }
    }

    @Composable
    private fun NearbyLobby() {
        val deniedStr = stringResource(R.string.permissionDenied)
        val emptyName = stringResource(R.string.playerNameEmpty)
        val failedStr = stringResource(R.string.connectionFailed)
        val waitingStr = stringResource(R.string.waitingForOpponent)
        val searchingStr = stringResource(R.string.searching)
        var name by rememberSaveable { mutableStateOf(Build.MODEL ?: "") }
        var step by remember { mutableStateOf(Step.ENTRY) }
        var transport by remember { mutableStateOf<NearbyTransport?>(null) }
        var wantRole by remember { mutableStateOf(Role.HOST) }
        var message by remember { mutableStateOf("") }
        var selected by remember { mutableStateOf<String?>(null) }

        fun cancel() {
            setPending(null)
            transport = null
            selected = null
            step = Step.ENTRY
        }

        fun begin(role: Role) {
            message = ""
            selected = null
            val t = NearbyTransport(this@LobbyActivity, role, name.trim())
            setPending(t)
            transport = t
            if (role == Role.HOST) {
                step = Step.HOSTING
                t.startHosting()
            } else {
                step = Step.JOINING
                t.startDiscovery()
            }
        }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result.values.all { it }) begin(wantRole) else message = deniedStr
        }

        fun request(role: Role) {
            if (name.isBlank()) {
                message = emptyName
                return
            }
            wantRole = role
            launcher.launch(NetPermissions.nearby())
        }

        BackHandler(enabled = step != Step.ENTRY) { cancel() }
        WatchState(transport, ReversiConstants.ONLINE_NEARBY, wantRole, name.trim()) {
            cancel()
            message = "$failedStr: $it"
        }

        val t = transport
        when {
            step == Step.HOSTING && t != null -> {
                val requests by t.requests.collectAsState()
                val entries = requests.map { ListEntry(it.id, it.name) }
                TwoPlayersListPage(
                    title = stringResource(R.string.createNearbyGame),
                    playerName = name,
                    entries = entries,
                    selectedId = selected,
                    onSelect = { selected = it.id },
                    message = if (entries.isEmpty()) waitingStr else "",
                    bannerId = bannerId
                ) {
                    ListPageButton(stringResource(R.string.startStr), DarkGreen, onClick = {
                        requests.firstOrNull { it.id == selected }?.let { t.acceptRequest(it) }
                    }, enabled = requests.any { it.id == selected })
                    ListPageButton(stringResource(R.string.refreshStr), Color.Blue, onClick = {
                        selected = null
                        t.refreshHosting()
                    })
                    ListPageButton(stringResource(R.string.cancelStr), DarkRed, onClick = { cancel() })
                }
            }
            step == Step.JOINING && t != null -> {
                val found by t.endpoints.collectAsState()
                TwoPlayersListPage(
                    title = stringResource(R.string.joinNearbyGame),
                    playerName = name,
                    entries = found.map { ListEntry(it.id, it.name) },
                    selectedId = selected,
                    onSelect = { e ->
                        if (selected == null) {
                            selected = e.id
                            found.firstOrNull { it.id == e.id }?.let { t.connectTo(it) }
                        }
                    },
                    message = if (selected != null) waitingStr else if (found.isEmpty()) searchingStr else "",
                    bannerId = bannerId
                ) {
                    ListPageButton(stringResource(R.string.refreshStr), Color.Blue, onClick = {
                        selected = null
                        t.refreshDiscovery()
                    })
                    ListPageButton(stringResource(R.string.cancelStr), DarkRed, onClick = { cancel() })
                }
            }
            else -> TwoPlayersEntry(
                title = stringResource(R.string.nearbyTwoPlayers),
                playerName = name,
                onPlayerNameChange = { name = it },
                message = message,
                busy = false,
                onCreate = { request(Role.HOST) },
                onJoin = { request(Role.GUEST) },
                onLeave = { finish() }
            )
        }
    }

    @Composable
    private fun RoomLobby() {
        val emptyName = stringResource(R.string.playerNameEmpty)
        val failedStr = stringResource(R.string.connectionFailed)
        val waitingStr = stringResource(R.string.waitingForOpponent)
        val searchingStr = stringResource(R.string.searchingOnline)
        var name by rememberSaveable { mutableStateOf(Build.MODEL ?: "") }
        var step by remember { mutableStateOf(Step.ENTRY) }
        var transport by remember { mutableStateOf<RoomTransport?>(null) }
        var busy by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("") }
        var selected by remember { mutableStateOf<String?>(null) }
        var refreshKey by remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()

        fun cancel() {
            setPending(null)
            transport = null
            selected = null
            step = Step.ENTRY
        }

        BackHandler(enabled = step != Step.ENTRY) { cancel() }
        WatchState(transport, ReversiConstants.ONLINE_ROOM,
            transport?.role ?: Role.HOST, name.trim()) {
            cancel()
            message = "$failedStr: $it"
        }

        val t = transport
        when {
            step == Step.HOSTING && t != null -> {
                val requests by t.requests.collectAsState()
                TwoPlayersListPage(
                    title = stringResource(R.string.createOnlineGame),
                    playerName = name,
                    entries = requests.map { ListEntry(it.uid, it.name) },
                    selectedId = selected,
                    onSelect = { selected = it.id },
                    message = if (requests.isEmpty()) waitingStr else "",
                    bannerId = bannerId
                ) {
                    ListPageButton(stringResource(R.string.startStr), DarkGreen, onClick = {
                        requests.firstOrNull { it.uid == selected }?.let { t.accept(it) }
                    }, enabled = requests.any { it.uid == selected })
                    ListPageButton(stringResource(R.string.refreshStr), Color.Blue, onClick = {
                        selected = null
                    })
                    ListPageButton(stringResource(R.string.cancelStr), DarkRed, onClick = { cancel() })
                }
            }
            step == Step.JOINING -> {
                var rooms by remember { mutableStateOf<List<RoomTransport.OpenRoom>>(emptyList()) }
                LaunchedEffect(refreshKey) {
                    RoomTransport.openRooms().collect { rooms = it }
                }
                TwoPlayersListPage(
                    title = stringResource(R.string.joinOnlineGame),
                    playerName = name,
                    entries = rooms.map { ListEntry(it.code, it.hostName) },
                    selectedId = selected,
                    onSelect = { e ->
                        if (selected == null) {
                            selected = e.id
                            scope.launch {
                                val r = RoomTransport.requestJoin(e.id, name.trim())
                                if (r == null) {
                                    selected = null
                                    message = failedStr
                                } else {
                                    setPending(r)
                                    transport = r
                                }
                            }
                        }
                    },
                    message = if (selected != null) waitingStr else if (rooms.isEmpty()) searchingStr else message,
                    bannerId = bannerId
                ) {
                    ListPageButton(stringResource(R.string.refreshStr), Color.Blue, onClick = {
                        if (selected == null) refreshKey++
                    })
                    ListPageButton(stringResource(R.string.cancelStr), DarkRed, onClick = { cancel() })
                }
            }
            else -> TwoPlayersEntry(
                title = stringResource(R.string.onlineTwoPlayers),
                playerName = name,
                onPlayerNameChange = { name = it },
                message = message,
                busy = busy,
                onCreate = {
                    if (name.isBlank()) {
                        message = emptyName
                    } else {
                        busy = true
                        message = ""
                        scope.launch {
                            val r = RoomTransport.create(name.trim())
                            busy = false
                            if (r == null) {
                                message = failedStr
                            } else {
                                setPending(r)
                                transport = r
                                step = Step.HOSTING
                            }
                        }
                    }
                },
                onJoin = {
                    if (name.isBlank()) {
                        message = emptyName
                    } else {
                        message = ""
                        step = Step.JOINING
                    }
                },
                onLeave = { finish() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!handedOff) pending?.close()
    }
}
