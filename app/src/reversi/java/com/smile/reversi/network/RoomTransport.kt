package com.smile.reversi.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Remote game room on Firebase Realtime Database.
 *
 * rooms/{code}: { host: uid, guest: uid, status, msgs/{host|guest}/{pushId}: json, presence/{host|guest}: bool }
 * Each side only writes its own msgs/ and presence/ branch and listens to the opponent's.
 */
class RoomTransport private constructor(
    val role: Role,
    val code: String,
    private val room: DatabaseReference
) : GameTransport {

    companion object {
        private const val ROOMS = "rooms"
        private const val CODE_LENGTH = 5
        private const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        private suspend fun signIn(): String? = suspendCancellableCoroutine { cont ->
            val auth = FirebaseAuth.getInstance()
            auth.currentUser?.let { cont.resume(it.uid); return@suspendCancellableCoroutine }
            auth.signInAnonymously()
                .addOnSuccessListener { cont.resume(it.user?.uid) }
                .addOnFailureListener { cont.resume(null) }
        }

        private suspend fun claim(ref: DatabaseReference, uid: String): Boolean =
            suspendCancellableCoroutine { cont ->
                ref.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(data: MutableData): Transaction.Result {
                        if (data.value != null) return Transaction.abort()
                        data.value = uid
                        return Transaction.success(data)
                    }

                    override fun onComplete(e: DatabaseError?, committed: Boolean, s: DataSnapshot?) {
                        cont.resume(e == null && committed)
                    }
                })
            }

        private fun randomCode() = (1..CODE_LENGTH)
            .map { CODE_ALPHABET.random() }.joinToString("")

        /** Creates a room and returns the host side, or null on failure. */
        suspend fun create(): RoomTransport? {
            val uid = signIn() ?: return null
            val db = FirebaseDatabase.getInstance().getReference(ROOMS)
            repeat(5) {
                val code = randomCode()
                if (claim(db.child(code).child("host"), uid)) {
                    db.child(code).child("status").setValue("waiting")
                    return RoomTransport(Role.HOST, code, db.child(code)).also { it.start() }
                }
            }
            return null
        }

        /** Joins an existing room as guest, or null if the code is invalid/full. */
        suspend fun join(code: String): RoomTransport? {
            val uid = signIn() ?: return null
            val room = FirebaseDatabase.getInstance().getReference(ROOMS).child(code.uppercase())
            if (!claim(room.child("guest"), uid)) return null
            room.child("status").setValue("playing")
            return RoomTransport(Role.GUEST, code.uppercase(), room).also { it.start() }
        }
    }

    private val inbox = Channel<Message>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Connecting)
    override val state: StateFlow<ConnectionState> = _state
    override val incoming: Flow<Message> = inbox.receiveAsFlow()

    private val mine = if (role == Role.HOST) "host" else "guest"
    private val theirs = if (role == Role.HOST) "guest" else "host"
    private val msgListener = object : ChildEventListener {
        override fun onChildAdded(s: DataSnapshot, prev: String?) {
            s.getValue(String::class.java)?.let { json ->
                Message.decode(json.toByteArray(Charsets.UTF_8))?.let { inbox.trySend(it) }
            }
        }

        override fun onChildChanged(s: DataSnapshot, prev: String?) {}
        override fun onChildRemoved(s: DataSnapshot) {}
        override fun onChildMoved(s: DataSnapshot, prev: String?) {}
        override fun onCancelled(e: DatabaseError) {
            _state.value = ConnectionState.Failed(e.message)
        }
    }
    private val presenceListener = object : ValueEventListener {
        override fun onDataChange(s: DataSnapshot) {
            val online = s.getValue(Boolean::class.java) == true
            if (online) {
                _state.value = ConnectionState.Connected
            } else if (_state.value == ConnectionState.Connected) {
                _state.value = ConnectionState.Closed
            }
        }

        override fun onCancelled(e: DatabaseError) {}
    }

    private fun start() {
        val myPresence = room.child("presence").child(mine)
        myPresence.setValue(true)
        myPresence.onDisconnect().setValue(false)
        room.child("msgs").child(theirs).addChildEventListener(msgListener)
        room.child("presence").child(theirs).addValueEventListener(presenceListener)
    }

    override suspend fun send(message: Message) {
        room.child("msgs").child(mine).push()
            .setValue(String(message.encode(), Charsets.UTF_8))
    }

    override fun close() {
        room.child("msgs").child(theirs).removeEventListener(msgListener)
        room.child("presence").child(theirs).removeEventListener(presenceListener)
        room.child("presence").child(mine).setValue(false)
        if (role == Role.HOST) room.child("status").setValue("closed")
        inbox.close()
        _state.value = ConnectionState.Closed
    }
}
