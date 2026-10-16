package com.smile.reversi.network

import com.smile.reversi.models.ReversiGridData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NetworkProtocolTest {

    @Test
    fun messagesRoundTrip() {
        val msgs = listOf(
            Message.Hello(1, "Pixel"),
            Message.Move(3, 2, 4, 1, -77),
            Message.Sync(5, "R".repeat(64), 2),
            Message.Resign, Message.Rematch, Message.Ping
        )
        for (m in msgs) assertEquals(m, Message.decode(m.encode()))
    }

    @Test
    fun garbageIsIgnored() {
        assertNull(Message.decode("not json".toByteArray()))
        assertNull(Message.decode("""{"t":"unknown"}""".toByteArray()))
    }

    @Test
    fun boardSyncRestoresHash() {
        val a = ReversiGridData().apply { initialize() }
        val b = ReversiGridData().apply { initialize() }
        a.placePiece(2, 3, ReversiGridData.HUMAN_PLAYER)
        assertEquals(false, a.boardHash() == b.boardHash())
        b.loadBoard(a.boardString())
        assertEquals(a.boardHash(), b.boardHash())
    }

    @Test
    fun fakeTransportDeliversToPeer() = runBlocking {
        val (a, b) = FakeTransport.pair()
        a.send(Message.Move(1, 2, 3, 1, 9))
        assertEquals(Message.Move(1, 2, 3, 1, 9), b.incoming.first())
    }
}
