package com.codewithkael.rtmpscreencast.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codewithkael.rtmpscreencast.R
import com.codewithkael.rtmpscreencast.service.RtmpService
import com.codewithkael.rtmpscreencast.ui.theme.RtmpScreenCastTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private fun getRecordingScreenIntent(): Intent {
        val mediaProjectionManager = application.getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager
        return mediaProjectionManager.createScreenCaptureIntent()
    }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            val urlInputText = remember { mutableStateOf(RtmpService.url) }
            val startForResult = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    mainViewModel.start(urlInputText.value,result)
                }
            }

            val requestPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions.all { it.value }) {
                    startForResult.launch(getRecordingScreenIntent())
                }
            }
            
            RtmpScreenCastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val streamingState = mainViewModel.streamingState.collectAsState()

                        Text(
                            text = "Welcome to Android Rtmp Screen Cast",
                            fontSize = 20.sp,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Enter the URL below:",
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )

                        TextField(value = urlInputText.value, onValueChange = {
                            urlInputText.value = it
                        })

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = {
                            if (!streamingState.value) {
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.FOREGROUND_SERVICE,
                                        android.Manifest.permission.POST_NOTIFICATIONS,
                                        android.Manifest.permission.RECORD_AUDIO,
                                    )
                                )
                            } else {
                                mainViewModel.stopStream()
                            }
                        }) {
                            Text(text = if (!streamingState.value) "Start Streaming" else "Stop Streaming")
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.youtube_logo),
                                contentDescription = "YouTube Channel",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://www.youtube.com/c/CodeWithKael")
                                        )
                                        context.startActivity(intent)
                                    }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "CodeWithKael",
                                fontSize = 16.sp,
                                color = Color.Red,
                                modifier = Modifier.clickable {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.youtube.com/c/CodeWithKael")
                                    )
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

