package com.codewithkael.rtmpscreencast.ui

import android.os.Bundle
import androidx.activity.ComponentActivity

class CloseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finishAffinity()
    }
}