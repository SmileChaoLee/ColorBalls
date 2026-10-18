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
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Remote game room on Firebase Realtime Database.
 *
 * lobby/{code}: { host: uid, name } lists rooms waiting for a guest.
 * rooms/{code}: { host: uid, guest: uid (chosen by host), requests/{uid}: name, status, msgs/{host|guest}/{pushId}: json, presence/{host|guest}: bool }
 * Each side only writes its own msgs/ and presence/ branch and listens to the opponent's.
 */
class RoomTransport private constructor(
    val role: Role,
    val code: String,
    private val room: DatabaseReference,
    private val uid: String
) : GameTransport {

    companion object {
        private const val ROOMS = "rooms"
        private const val LOBBY = "lobby"
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

        /** Creates a room listed in the lobby and returns the host side, or null on failure. */
        suspend fun create(hostName: String): RoomTransport? {
            val uid = signIn() ?: return null
            val db = FirebaseDatabase.getInstance()
            repeat(5) {
                val code = randomCode()
                val room = db.getReference(ROOMS).child(code)
                if (claim(room.child("host"), uid)) {
                    room.child("status").setValue("waiting")
                    // a vanished host takes the whole room down with it
                    room.onDisconnect().removeValue()
                    val listing = db.getReference(LOBBY).child(code)
                    listing.setValue(mapOf("host" to uid, "name" to hostName))
                    listing.onDisconnect().removeValue()
                    return RoomTransport(Role.HOST, code, room, uid).also { it.start() }
                }
            }
            return null
        }

        /** Asks the host of [code] to let this player in; the host decides. */
        suspend fun requestJoin(code: String, name: String): RoomTransport? {
            val uid = signIn() ?: return null
            val room = FirebaseDatabase.getInstance().getReference(ROOMS).child(code)
            val request = room.child("requests").child(uid)
            request.setValue(name)
            request.onDisconnect().removeValue()
            return RoomTransport(Role.GUEST, code, room, uid).also { it.awaitAcceptance() }
        }

        /** Live list of rooms waiting for a guest. */
        fun openRooms(): Flow<List<OpenRoom>> = callbackFlow {
            signIn()
            val ref = FirebaseDatabase.getInstance().getReference(LOBBY)
            val listener = object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    trySend(s.children.mapNotNull {
                        val key = it.key ?: return@mapNotNull null
                        OpenRoom(key, it.child("name").getValue(String::class.java) ?: key)
                    })
                }

                override fun onCancelled(e: DatabaseError) { trySend(emptyList()) }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }
    }

    data class OpenRoom(val code: String, val hostName: String)
    data class JoinRequest(val uid: String, val name: String)

    private val _requests = MutableStateFlow<List<JoinRequest>>(emptyList())
    /** Host side: players asking to join. */
    val requests: StateFlow<List<JoinRequest>> = _requests

    private val requestListener = object : ChildEventListener {
        override fun onChildAdded(s: DataSnapshot, prev: String?) {
            val uid = s.key ?: return
            _requests.value = _requests.value.filter { it.uid != uid } +
                    JoinRequest(uid, s.getValue(String::class.java) ?: uid)
        }

        override fun onChildChanged(s: DataSnapshot, prev: String?) {}
        override fun onChildRemoved(s: DataSnapshot) {
            _requests.value = _requests.value.filter { it.uid != s.key }
        }

        override fun onChildMoved(s: DataSnapshot, prev: String?) {}
        override fun onCancelled(e: DatabaseError) {}
    }

    private val guestListener = object : ValueEventListener {
        override fun onDataChange(s: DataSnapshot) {
            val guest = s.getValue(String::class.java) ?: return
            room.child("guest").removeEventListener(this)
            if (guest == uid) {
                room.child("requests").child(uid).removeValue()
                start()
            } else {
                _state.value = ConnectionState.Failed("rejected")
            }
        }

        override fun onCancelled(e: DatabaseError) {
            _state.value = ConnectionState.Failed(e.message)
        }
    }

    /** Host: let the chosen player in and take the room off the lobby list. */
    fun accept(request: JoinRequest) {
        room.child("guest").setValue(request.uid)
        room.child("status").setValue("playing")
        FirebaseDatabase.getInstance().getReference(LOBBY).child(code).removeValue()
        room.child("requests").removeEventListener(requestListener)
        _requests.value = emptyList()
    }

    private fun awaitAcceptance() {
        room.child("guest").addValueEventListener(guestListener)
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
        if (role == Role.GUEST) myPresence.onDisconnect().setValue(false)
        if (role == Role.HOST) room.child("requests").addChildEventListener(requestListener)
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
        if (role == Role.HOST) {
            room.child("requests").removeEventListener(requestListener)
            FirebaseDatabase.getInstance().getReference(LOBBY).child(code).removeValue()
            // deleting the room also tells the guest (host presence disappears)
            room.removeValue()
        } else {
            room.child("presence").child(mine).setValue(false)
            room.child("guest").removeEventListener(guestListener)
            room.child("requests").child(uid).removeValue()
        }
        inbox.close()
        _state.value = ConnectionState.Closed
    }
}
