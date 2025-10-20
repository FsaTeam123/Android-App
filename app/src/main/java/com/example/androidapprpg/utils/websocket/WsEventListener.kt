package com.example.androidapprpg.utils.websocket

import android.util.Log
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Response
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy

class WsEventListener(private val albHost: String) : EventListener() {

    companion object { private const val TAG = "WS_EVT" }

    override fun callStart(call: Call) {
        Log.d(TAG, "callStart url=${call.request().url}")
    }

    override fun dnsStart(call: Call, domainName: String) {
        if (domainName == albHost) Log.d(TAG, "dnsStart $domainName")
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        if (domainName == albHost) {
            val ips = inetAddressList.joinToString { it.hostAddress ?: "?" }
            Log.d(TAG, "dnsEnd   $domainName -> [$ips]")
        }
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        Log.d(TAG, "connectStart ${inetSocketAddress.address.hostAddress}:${inetSocketAddress.port} via $proxy")
    }

    override fun secureConnectStart(call: Call) {
        Log.d(TAG, "TLS handshake start")
    }

    override fun secureConnectEnd(call: Call, handshake: okhttp3.Handshake?) {
        val cipher = handshake?.cipherSuite ?: "?"
        val tls = handshake?.tlsVersion ?: "?"
        Log.d(TAG, "TLS handshake end (tls=$tls cipher=$cipher)")
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        Log.d(TAG, "responseHeadersEnd code=${response.code} ${response.message}")
    }

    override fun callFailed(call: Call, ioe: java.io.IOException) {
        Log.e(TAG, "callFailed url=${call.request().url}", ioe)
    }
}
