package dev.meloda.fast.auth.login.presentation

import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dev.meloda.fast.auth.login.QrCodeAuthParser
import dev.meloda.fast.ui.common.LocalLogger
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

@Composable
internal fun QrCameraPreview(
    modifier: Modifier = Modifier,
    scanGeneration: Int,
    parser: QrCodeAuthParser,
    onQrAccepted: (String) -> Unit,
    onWrongQr: () -> Unit,
    onQrCleared: () -> Unit,
    onCameraReady: (Camera) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val logger = LocalLogger.current
    val scope = rememberCoroutineScope()
    val currentOnQrAccepted = rememberUpdatedState(onQrAccepted)
    val currentOnWrongQr = rememberUpdatedState(onWrongQr)
    val currentOnQrCleared = rememberUpdatedState(onQrCleared)
    val currentOnCameraReady = rememberUpdatedState(onCameraReady)

    val scanned = remember { AtomicBoolean(false) }
    val disposed = remember { AtomicBoolean(false) }
    val lastRejectAt = remember { AtomicLong(0L) }
    val lastValue = remember { AtomicReference<String?>(null) }
    val matchCount = remember { AtomicInteger(0) }
    val providerRef = remember { AtomicReference<ProcessCameraProvider?>(null) }
    val previewRef = remember { AtomicReference<Preview?>(null) }
    val analysisRef = remember { AtomicReference<ImageAnalysis?>(null) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    LaunchedEffect(scanGeneration) {
        scanned.set(false)
        lastValue.set(null)
        matchCount.set(0)
    }

    DisposableEffect(Unit) {
        onDispose {
            disposed.set(true)
            val provider = providerRef.get()
            val preview = previewRef.get()
            val analysis = analysisRef.get()
            if (provider != null && preview != null && analysis != null) {
                runCatching { provider.unbind(preview, analysis) }
            }
            runCatching { barcodeScanner.close() }
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            val previewView = PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraFuture = ProcessCameraProvider.getInstance(viewContext)
            cameraFuture.addListener({
                if (disposed.get()) return@addListener
                val provider = try {
                    cameraFuture.get()
                } catch (e: Exception) {
                    logger.error("QrCameraPreview", "Failed to open camera", e)
                    return@addListener
                }

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    @OptIn(ExperimentalGetImage::class)
                    val mediaImage = imageProxy.image
                    if (mediaImage == null || scanned.get() || disposed.get()) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    val frameWidth = imageProxy.width
                    val frameHeight = imageProxy.height
                    try {
                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                if (scanned.get() || disposed.get()) return@addOnSuccessListener
                                val candidate = barcodes.firstNotNullOfOrNull { barcode ->
                                    val value = barcode.rawValue?.takeIf(String::isNotBlank)
                                        ?: return@firstNotNullOfOrNull null
                                    val box = barcode.boundingBox
                                    if (box != null && !parser.isInCenter(
                                            box.left, box.top, box.right, box.bottom,
                                            frameWidth, frameHeight
                                        )
                                    ) return@firstNotNullOfOrNull null
                                    if (!parser.isScannable(value)) {
                                        val now = System.currentTimeMillis()
                                        if (now - lastRejectAt.get() > 1_500L) {
                                            lastRejectAt.set(now)
                                            scope.launch { currentOnWrongQr.value() }
                                        }
                                        return@firstNotNullOfOrNull null
                                    }
                                    value
                                }
                                if (candidate == null) {
                                    lastValue.set(null)
                                    matchCount.set(0)
                                    if (barcodes.isEmpty()) scope.launch { currentOnQrCleared.value() }
                                    return@addOnSuccessListener
                                }
                                scope.launch { currentOnQrCleared.value() }
                                val count = if (candidate == lastValue.get()) {
                                    matchCount.incrementAndGet()
                                } else {
                                    lastValue.set(candidate)
                                    matchCount.set(1)
                                    1
                                }
                                if (count >= REQUIRED_FRAMES && scanned.compareAndSet(false, true)) {
                                    scope.launch { currentOnQrAccepted.value(candidate) }
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } catch (e: Exception) {
                        imageProxy.close()
                        logger.error("QrCameraPreview", "Failed to scan frame", e)
                    }
                }

                try {
                    val camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                    providerRef.set(provider)
                    previewRef.set(preview)
                    analysisRef.set(analysis)
                    scope.launch { currentOnCameraReady.value(camera) }
                } catch (e: Exception) {
                    analysis.clearAnalyzer()
                    logger.error("QrCameraPreview", "Failed to bind camera", e)
                }
            }, ContextCompat.getMainExecutor(viewContext))
            previewView
        }
    )
}

private const val REQUIRED_FRAMES = 4
