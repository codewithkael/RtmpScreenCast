package com.codewithkael.rtmpscreencast.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.codewithkael.rtmpscreencast.ui.CloseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RtmpBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_EXIT") {
            //we want to exit the whole application
            context?.let { ctx ->
                RtmpService.stopService(ctx)
                ctx.startActivity(Intent(context, CloseActivity::class.java)
                    .apply {
                        addFlags(FLAG_ACTIVITY_NEW_TASK)
                    })
            }
        }
    }
}