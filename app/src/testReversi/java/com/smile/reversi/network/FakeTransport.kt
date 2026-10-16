package com.smile.reversi.network

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/** In-process transport pair, used for tests. */
class FakeTransport private constructor() : GameTransport {
    private val inbox = Channel<Message>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Connected)
    private lateinit var peer: FakeTransport

    override val state: StateFlow<ConnectionState> = _state
    override val incoming: Flow<Message> = inbox.receiveAsFlow()

    override suspend fun send(message: Message) {
        // round-trips through the codec so tests exercise the wire format
        Message.decode(message.encode())?.let { peer.inbox.trySend(it) }
    }

    override fun close() {
        _state.value = ConnectionState.Closed
        inbox.close()
    }

    companion object {
        fun pair(): Pair<FakeTransport, FakeTransport> {
            val a = FakeTransport()
            val b = FakeTransport()
            a.peer = b
            b.peer = a
            return a to b
        }
    }
}
