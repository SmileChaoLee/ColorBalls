package com.smile.reversi.network

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Bluetooth / Wi-Fi Direct play through Google Nearby Connections.
 * Nearby chooses the underlying medium. Host advertises, guest discovers and connects.
 */
class NearbyTransport(
    context: Context,
    val role: Role,
    private val localName: String
) : GameTransport {

    data class Endpoint(val id: String, val name: String)

    companion object {
        const val SERVICE_ID = "com.smile.reversi.nearby"
        private val STRATEGY = Strategy.P2P_POINT_TO_POINT
    }

    private val client: ConnectionsClient = Nearby.getConnectionsClient(context.applicationContext)
    private val inbox = Channel<Message>(Channel.UNLIMITED)
    private var peerId: String? = null

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    private val _endpoints = MutableStateFlow<List<Endpoint>>(emptyList())

    override val state: StateFlow<ConnectionState> = _state
    override val incoming: Flow<Message> = inbox.receiveAsFlow()
    val endpoints: StateFlow<List<Endpoint>> = _endpoints

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes -> Message.decode(bytes)?.let { inbox.trySend(it) } }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val connectionCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    peerId = endpointId
                    client.stopAdvertising()
                    client.stopDiscovery()
                    _state.value = ConnectionState.Connected
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED ->
                    _state.value = ConnectionState.Failed("rejected")
                else -> _state.value = ConnectionState.Failed(result.status.statusMessage ?: "error")
            }
        }

        override fun onDisconnected(endpointId: String) {
            if (endpointId == peerId) _state.value = ConnectionState.Closed
        }
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            _endpoints.value = _endpoints.value.filter { it.id != endpointId } +
                    Endpoint(endpointId, info.endpointName)
        }

        override fun onEndpointLost(endpointId: String) {
            _endpoints.value = _endpoints.value.filter { it.id != endpointId }
        }
    }

    /** Host: become visible to nearby guests. */
    fun startHosting() {
        _state.value = ConnectionState.Connecting
        client.startAdvertising(
            localName, SERVICE_ID, connectionCallback,
            AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        ).addOnFailureListener { _state.value = ConnectionState.Failed(it.message ?: "advertise") }
    }

    /** Guest: look for hosts; pick one with [connectTo]. */
    fun startDiscovery() {
        _state.value = ConnectionState.Connecting
        client.startDiscovery(
            SERVICE_ID, discoveryCallback,
            DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        ).addOnFailureListener { _state.value = ConnectionState.Failed(it.message ?: "discover") }
    }

    fun connectTo(endpoint: Endpoint) {
        client.requestConnection(localName, endpoint.id, connectionCallback)
            .addOnFailureListener { _state.value = ConnectionState.Failed(it.message ?: "connect") }
    }

    override suspend fun send(message: Message) {
        peerId?.let { client.sendPayload(it, Payload.fromBytes(message.encode())) }
    }

    override fun close() {
        client.stopAdvertising()
        client.stopDiscovery()
        client.stopAllEndpoints()
        inbox.close()
        _state.value = ConnectionState.Closed
    }
}
