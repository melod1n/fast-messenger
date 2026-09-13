package dev.meloda.fast.auth.login.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dev.meloda.fast.auth.login.QrCodeAuthParser
import dev.meloda.fast.ui.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    onQrCodeScanned: (String) -> Unit,
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isTorchOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var hintWrongQr by remember { mutableStateOf(false) }
    var acceptedQr by remember { mutableStateOf(false) }

    val scannedFlag = remember { AtomicBoolean(false) }
    val lastRejectAt = remember { AtomicLong(0L) }
    val lastValue = remember { AtomicReference<String?>(null) }
    val matchCount = remember { AtomicInteger(0) }
    val requiredFrames = 4
    val acceptDelayMs = 600L
    val analysisExecutor = remember<ExecutorService> { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { barcodeScanner.close() }
            runCatching { analysisExecutor.shutdown() }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_qr_code)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back_round_24),
                            contentDescription = stringResource(R.string.qr_scanner_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val newTorchState = !isTorchOn
                            camera?.cameraControl?.enableTorch(newTorchState)
                            isTorchOn = newTorchState
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isTorchOn) R.drawable.ic_star_fill_round_24
                                else R.drawable.ic_star_round_24
                            ),
                            contentDescription = stringResource(R.string.qr_scanner_torch)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                @OptIn(ExperimentalGetImage::class)
                                val mediaImage = imageProxy.image
                                if (mediaImage == null || scannedFlag.get()) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                val frameWidth = imageProxy.width
                                val frameHeight = imageProxy.height
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (scannedFlag.get()) return@addOnSuccessListener
                                        var candidate: String? = null
                                        for (barcode in barcodes) {
                                            val rawValue = barcode.rawValue
                                            if (rawValue.isNullOrBlank()) continue

                                            val box = barcode.boundingBox
                                            if (box != null && !QrCodeAuthParser.isInCenter(
                                                    boxLeft = box.left,
                                                    boxTop = box.top,
                                                    boxRight = box.right,
                                                    boxBottom = box.bottom,
                                                    imageWidth = frameWidth,
                                                    imageHeight = frameHeight
                                                )
                                            ) {
                                                continue
                                            }

                                            if (!QrCodeAuthParser.isScannable(rawValue)) {
                                                val now = System.currentTimeMillis()
                                                if (now - lastRejectAt.get() > 1500L) {
                                                    lastRejectAt.set(now)
                                                    mainHandler.post { hintWrongQr = true }
                                                }
                                                continue
                                            }

                                            candidate = rawValue
                                            break
                                        }
                                        if (candidate == null) {
                                            lastValue.set(null)
                                            matchCount.set(0)
                                            return@addOnSuccessListener
                                        }
                                        if (candidate == lastValue.get()) {
                                            matchCount.incrementAndGet()
                                        } else {
                                            lastValue.set(candidate)
                                            matchCount.set(1)
                                        }
                                        if (matchCount.get() >= requiredFrames && scannedFlag.compareAndSet(false, true)) {
                                            val accepted = candidate
                                            mainHandler.post {
                                                acceptedQr = true
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                            mainHandler.postDelayed({ onQrCodeScanned(accepted) }, acceptDelayMs)
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                                    .addOnFailureListener {
                                        runCatching { imageProxy.close() }
                                    }
                            }

                            try {
                                cameraProvider.unbindAll()
                                val boundCamera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                                mainHandler.post { camera = boundCamera }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .border(
                                width = 3.dp,
                                color = when {
                                    acceptedQr -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                    hintWrongQr -> MaterialTheme.colorScheme.error
                                    else -> androidx.compose.ui.graphics.Color.White
                                },
                                shape = RoundedCornerShape(18.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(
                            if (acceptedQr) R.string.qr_scanner_accepted
                            else if (hintWrongQr) R.string.qr_scanner_hint_wrong
                            else R.string.qr_scanner_hint
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hintWrongQr) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                Text(
                    text = stringResource(R.string.qr_scanner_camera_permission),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
