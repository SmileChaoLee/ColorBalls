package com.smile.reversi.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smile.colorballs_main.views.ui.theme.ColorBallsTheme
import com.smile.colorballs_main.R
import com.smile.reversi.constants.ReversiConstants
import com.smile.reversi.network.ConnectionState
import com.smile.reversi.network.GameTransport
import com.smile.reversi.network.NearbyTransport
import com.smile.reversi.network.NetPermissions
import com.smile.reversi.network.Role
import com.smile.reversi.network.RoomTransport
import com.smile.reversi.network.TransportHolder
import kotlinx.coroutines.launch

/** Connects to an opponent (Nearby or online room), then opens the game screen. */
class LobbyActivity : ComponentActivity() {

    companion object {
        const val EXTRA_MODE = "LobbyMode"
    }

    private var handedOff = false
    private var pending: GameTransport? = null
    private val playerName: String = Build.MODEL ?: "Player"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getIntExtra(EXTRA_MODE, ReversiConstants.ONLINE_NEARBY)
        setContent {
            ColorBallsTheme {
                Scaffold { padding ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                    ) {
                        if (mode == ReversiConstants.ONLINE_NEARBY) NearbyLobby() else RoomLobby()
                    }
                }
            }
        }
    }

    private fun startGame(mode: Int, transport: GameTransport, role: Role) {
        if (handedOff) return
        handedOff = true
        TransportHolder.set(transport, role, playerName)
        Intent(this, ReversiActivity::class.java).also {
            it.putExtra(ReversiConstants.PLAY_MODE, mode)
            startActivity(it)
        }
        finish()
    }

    private fun setPending(t: GameTransport) {
        pending?.close()
        pending = t
    }

    @Composable
    private fun WatchState(transport: GameTransport?, mode: Int, role: Role,
                           onFailed: (String) -> Unit) {
        transport ?: return
        val state by transport.state.collectAsState()
        LaunchedEffect(state) {
            when (val s = state) {
                ConnectionState.Connected -> startGame(mode, transport, role)
                is ConnectionState.Failed -> onFailed(s.reason)
                else -> {}
            }
        }
    }

    @Composable
    private fun NearbyLobby() {
        var transport by remember { mutableStateOf<NearbyTransport?>(null) }
        var wantRole by remember { mutableStateOf<Role?>(null) }
        var error by remember { mutableStateOf("") }
        val deniedStr = getString(R.string.permissionDenied)

        fun begin(role: Role) {
            error = ""
            val t = NearbyTransport(this@LobbyActivity, role, playerName)
            setPending(t)
            transport = t
            if (role == Role.HOST) t.startHosting() else t.startDiscovery()
        }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result.values.all { it }) wantRole?.let { begin(it) } else error = deniedStr
        }

        fun request(role: Role) {
            wantRole = role
            launcher.launch(NetPermissions.nearby())
        }

        Text(getString(R.string.nearbyPlay), fontSize = 22.sp)
        WatchState(transport, ReversiConstants.ONLINE_NEARBY, wantRole ?: Role.HOST) { error = it }

        val t = transport
        if (t == null) {
            Button(onClick = { request(Role.HOST) }, modifier = Modifier.fillMaxWidth()) {
                Text(getString(R.string.hostGame))
            }
            Button(onClick = { request(Role.GUEST) }, modifier = Modifier.fillMaxWidth()) {
                Text(getString(R.string.findGame))
            }
        } else if (t.role == Role.HOST) {
            Text(getString(R.string.waitingForOpponent))
        } else {
            Text(getString(R.string.searching))
            val endpoints by t.endpoints.collectAsState()
            endpoints.forEach { e ->
                Button(onClick = { t.connectTo(e) }, modifier = Modifier.fillMaxWidth()) {
                    Text(e.name)
                }
            }
        }
        if (error.isNotEmpty()) Text("${getString(R.string.connectionFailed)}: $error")
    }

    @Composable
    private fun RoomLobby() {
        var transport by remember { mutableStateOf<RoomTransport?>(null) }
        var code by remember { mutableStateOf("") }
        var busy by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        val notFound = getString(R.string.roomNotFound)

        Text(getString(R.string.onlineRoom), fontSize = 22.sp)
        WatchState(transport, ReversiConstants.ONLINE_ROOM, transport?.role ?: Role.HOST) {
            error = it
        }

        val t = transport
        if (t != null) {
            if (t.role == Role.HOST) {
                Text(getString(R.string.shareRoomCode))
                Text(t.code, fontSize = 36.sp)
            }
            Text(getString(R.string.waitingForOpponent))
        } else {
            Button(
                enabled = !busy,
                onClick = {
                    busy = true
                    error = ""
                    scope.launch {
                        val r = RoomTransport.create()
                        busy = false
                        if (r == null) error = getString(R.string.connectionFailed)
                        else { setPending(r); transport = r }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(getString(R.string.createRoom)) }

            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().take(8) },
                label = { Text(getString(R.string.roomCode)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                enabled = !busy && code.length >= 4,
                onClick = {
                    busy = true
                    error = ""
                    scope.launch {
                        val r = RoomTransport.join(code)
                        busy = false
                        if (r == null) error = notFound
                        else { setPending(r); transport = r }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(getString(R.string.joinRoom)) }
        }
        if (error.isNotEmpty()) Text(error)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!handedOff) pending?.close()
    }
}
