package com.smile.reversi.network

import org.json.JSONObject

/** Wire protocol between two Reversi devices. Encoded as one JSON object per message. */
sealed class Message {
    data class Hello(val version: Int, val name: String) : Message()
    data class Move(val seq: Int, val x: Int, val y: Int, val player: Int, val hash: Int) : Message()
    data class Sync(val seq: Int, val board: String, val current: Int) : Message()
    object Resign : Message()
    object Rematch : Message()
    object Ping : Message()

    fun encode(): ByteArray {
        val o = JSONObject()
        when (this) {
            is Hello -> o.put(T, "hello").put("v", version).put("name", name)
            is Move -> o.put(T, "move").put("seq", seq).put("x", x).put("y", y)
                .put("p", player).put("h", hash)
            is Sync -> o.put(T, "sync").put("seq", seq).put("b", board).put("c", current)
            Resign -> o.put(T, "resign")
            Rematch -> o.put(T, "rematch")
            Ping -> o.put(T, "ping")
        }
        return o.toString().toByteArray(Charsets.UTF_8)
    }

    companion object {
        const val PROTOCOL_VERSION = 1
        private const val T = "t"

        fun decode(bytes: ByteArray): Message? = try {
            val o = JSONObject(String(bytes, Charsets.UTF_8))
            when (o.getString(T)) {
                "hello" -> Hello(o.getInt("v"), o.optString("name"))
                "move" -> Move(o.getInt("seq"), o.getInt("x"), o.getInt("y"),
                    o.getInt("p"), o.getInt("h"))
                "sync" -> Sync(o.getInt("seq"), o.getString("b"), o.getInt("c"))
                "resign" -> Resign
                "rematch" -> Rematch
                "ping" -> Ping
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
