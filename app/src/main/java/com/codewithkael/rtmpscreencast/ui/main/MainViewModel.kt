package com.codewithkael.rtmpscreencast.ui.main

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.result.ActivityResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codewithkael.rtmpscreencast.service.RtmpService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private var myService: RtmpService? = null
    private var isBound: Boolean = false
    var streamingState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var serviceStreamingStateJob: Job? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as RtmpService.LocalBinder
            myService = binder.getService()
            serviceStreamingStateJob?.cancel()
            serviceStreamingStateJob = myService?.streamingState
                ?.onEach { state ->
                    streamingState.value = state
                }?.launchIn(viewModelScope)

            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    init {
        RtmpService.startService(application)
        RtmpService.bindService(application, connection)
    }


    override fun onCleared() {
        super.onCleared()
        serviceStreamingStateJob?.cancel()
        if (isBound) {
            application.unbindService(connection)
        }
        myService = null
    }

    fun start(url: String, result: ActivityResult) {
        result.data?.let {
            myService?.prepareStreamRtp(url, result.resultCode, it)
            myService?.startStreamRtp()
        }
    }

    fun stopStream() {
        myService?.handleStopService()
    }

}