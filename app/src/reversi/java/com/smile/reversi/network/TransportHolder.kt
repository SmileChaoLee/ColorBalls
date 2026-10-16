package com.smile.reversi.network

/** Hands the connected transport from the lobby to the game screen and across rotations. */
object TransportHolder {
    var transport: GameTransport? = null
        private set
    var role: Role = Role.HOST
        private set
    var playerName: String = ""
        private set

    fun set(t: GameTransport, r: Role, name: String) {
        if (transport !== t) transport?.close()
        transport = t
        role = r
        playerName = name
    }

    fun clear() {
        transport?.close()
        transport = null
    }
}
