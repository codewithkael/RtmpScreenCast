package com.codewithkael.rtmpscreencast.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.codewithkael.rtmpscreencast.MyConnectChecker
import com.codewithkael.rtmpscreencast.R
import com.pedro.library.base.DisplayBase
import com.pedro.library.rtmp.RtmpDisplay
import com.pedro.library.rtsp.RtspDisplay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RtmpService : Service() {

    //service section
    private var isServiceRunning = false
    val streamingState: MutableStateFlow<Boolean> = MutableStateFlow(false)


    //notification section
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    //rtp section
    private var displayBase: DisplayBase? = null
    private val myConnectChecker = object : MyConnectChecker() {
        override fun onConnectionSuccess() {
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("TAG", "onConnectionSuccess: ")
                streamingState.emit(true)
            }
        }

        override fun onDisconnect() {
            super.onDisconnect()
            CoroutineScope(Dispatchers.IO).launch {
                streamingState.emit(false)
            }
        }

        override fun onConnectionFailed(reason: String) {
            Log.d("TAG", "failure connect: ")

            CoroutineScope(Dispatchers.IO).launch {
                streamingState.emit(false)
            }
        }
    }


    fun prepareStreamRtp(incomingUrl: String, resultCode: Int, data: Intent) {
        url = incomingUrl
        if (url.startsWith("rtmp")) {
            displayBase = RtmpDisplay(baseContext, true, myConnectChecker)
            displayBase?.setIntentResult(resultCode, data)
        } else {
            displayBase = RtspDisplay(baseContext, true, myConnectChecker)
            displayBase?.setIntentResult(resultCode, data)
        }
        displayBase?.glInterface?.setForceRender(true)
    }

    fun startStreamRtp() {
        if (displayBase?.isStreaming != true) {
            if (displayBase?.prepareVideo() == true && displayBase?.prepareAudio() == true) {
                displayBase?.startStream(url)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            streamingState.emit(true)
        }
    }

    override fun onCreate() {
        super.onCreate()
        setupNotification()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent.action?.let { serviceAction ->
            when (serviceAction) {
                "Start" -> handleStartService()
                "Stop" -> handleStopService()
            }
        }
        return START_STICKY
    }

    private fun handleStartService() {
        if (!isServiceRunning) {
            startForeground(1, notificationBuilder.build())
            isServiceRunning = true
        }
    }

    fun handleStopService() {
        isServiceRunning = false
        displayBase?.stopStream()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    inner class LocalBinder : Binder() {
        fun getService(): RtmpService = this@RtmpService
    }

    private val binder = LocalBinder()
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun setupNotification() {
        notificationManager = getSystemService(
            NotificationManager::class.java
        )
        val notificationChannel = NotificationChannel(
            "rtmpChannel", "foreground", NotificationManager.IMPORTANCE_HIGH
        )

        val intent = Intent(this, RtmpBroadcastReceiver::class.java).apply {
            action = "ACTION_EXIT"
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        notificationManager.createNotificationChannel(notificationChannel)
        notificationBuilder = NotificationCompat.Builder(
            this, "rtmpChannel"
        ).setSmallIcon(R.mipmap.ic_launcher)
            .addAction(R.drawable.ic_launcher_foreground, "Exit", pendingIntent).setOngoing(true)
    }

    companion object {
        //    private var url:String = "rtmp://141.11.184.69/live/${UUID.randomUUID()}"
        var url: String = ""
        fun startService(context: Context) {
            Thread {
                context.startForegroundService(Intent(context, RtmpService::class.java).apply {
                    action = "Start"
                })
            }.start()
        }

        fun stopService(context: Context) {
            context.startForegroundService(Intent(context, RtmpService::class.java).apply {
                action = "Stop"
            })
        }

        fun bindService(context: Context, connection: ServiceConnection) {
            context.bindService(
                Intent(context, RtmpService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

}