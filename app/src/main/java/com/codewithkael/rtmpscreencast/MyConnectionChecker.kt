package com.codewithkael.rtmpscreencast

import com.pedro.common.ConnectChecker

open class MyConnectChecker : ConnectChecker {
    override fun onAuthError() {
    }

    override fun onAuthSuccess() {
    }

    override fun onConnectionFailed(reason: String) {
    }

    override fun onConnectionStarted(url: String) {
    }

    override fun onConnectionSuccess() {
    }

    override fun onDisconnect() {
    }

    override fun onNewBitrate(bitrate: Long) {
    }
}