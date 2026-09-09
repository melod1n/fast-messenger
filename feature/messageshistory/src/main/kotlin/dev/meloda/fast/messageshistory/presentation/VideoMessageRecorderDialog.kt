package dev.meloda.fast.messageshistory.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import dev.meloda.fast.ui.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File

@Composable
fun VideoMessageRecorderDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSendVideoMessage: (file: File, durationSec: Int) -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var currentOutputFile by remember { mutableStateOf<File?>(null) }
    var durationSec by remember { mutableIntStateOf(0) }
    var isRecordingActive by remember { mutableStateOf(false) }
    var isSent by remember { mutableStateOf(false) }
    var pendingSend by remember { mutableStateOf(false) }

    val maxDuration = 60

    DisposableEffect(Unit) {
        onDispose {
            recording?.stop()
            recording = null
            if (!isSent) {
                currentOutputFile?.delete()
            }
        }
    }

    LaunchedEffect(cameraProvider, previewView, lensFacing) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val view = previewView ?: return@LaunchedEffect

        recording?.stop()
        recording = null

        try {
            provider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = view.surfaceProvider
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.SD))
                .build()

            val videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )

            val videoDir = File(context.cacheDir, "video_messages").apply { mkdirs() }
            val outputFile = File(videoDir, "video_msg_${System.currentTimeMillis()}.mp4")
            currentOutputFile = outputFile

            val outputOptions = FileOutputOptions.Builder(outputFile).build()
            val pendingRecording = recorder.prepareRecording(context, outputOptions)

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pendingRecording.withAudioEnabled()
            }

            recording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        isRecordingActive = true
                    }

                    is VideoRecordEvent.Finalize -> {
                        isRecordingActive = false
                        if (event.hasError()) {
                            Log.e("VideoRecorder", "Recording error: ${event.cause}")
                            if (pendingSend) {
                                pendingSend = false
                                currentOutputFile?.delete()
                                onDismiss()
                            }
                        } else if (pendingSend) {
                            pendingSend = false
                            val file = currentOutputFile
                            if (file != null && file.exists() && file.length() > 0) {
                                isSent = true
                                onSendVideoMessage(file, durationSec.coerceAtLeast(1))
                            }
                            onDismiss()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VideoRecorder", "Use case binding failed", e)
        }
    }

    LaunchedEffect(isRecordingActive) {
        if (isRecordingActive) {
            while (isActive && durationSec < maxDuration) {
                delay(1000L)
                durationSec++
            }
            if (durationSec >= maxDuration) {
                pendingSend = true
                recording?.stop()
            }
        }
    }

    Dialog(
        onDismissRequest = {
            recording?.stop()
            if (!isSent) {
                currentOutputFile?.delete()
            }
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRecordingActive) Color.Red else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val mins = durationSec / 60
                    val secs = durationSec % 60
                    Text(
                        text = String.format(java.util.Locale.getDefault(), "%d:%02d / 1:00", mins, secs),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val view = PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                            previewView = view

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                cameraProvider = cameraProviderFuture.get()
                            }, ContextCompat.getMainExecutor(ctx))

                            view
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    val progress by animateFloatAsState(
                        targetValue = durationSec.toFloat() / maxDuration.toFloat(),
                        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                        label = "progress"
                    )

                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            recording?.stop()
                            recording = null
                            currentOutputFile?.delete()
                            currentOutputFile = null
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_round_24),
                            contentDescription = "Cancel",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            recording?.stop()
                            recording = null
                            currentOutputFile?.delete()
                            currentOutputFile = null
                            isRecordingActive = false
                            durationSec = 0
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                CameraSelector.LENS_FACING_BACK
                            } else {
                                CameraSelector.LENS_FACING_FRONT
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh_round_24),
                            contentDescription = "Flip camera",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            pendingSend = true
                            recording?.stop()
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send_round_24),
                            contentDescription = "Send video note",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}
