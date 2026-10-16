package com.smile.reversi.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class Role { HOST, GUEST }

sealed class ConnectionState {
    object Idle : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Failed(val reason: String) : ConnectionState()
    object Closed : ConnectionState()
}

/** Medium-independent link to the opponent. Implementations: Nearby, Firebase room. */
interface GameTransport {
    val state: StateFlow<ConnectionState>
    val incoming: Flow<Message>
    suspend fun send(message: Message)
    fun close()
}
