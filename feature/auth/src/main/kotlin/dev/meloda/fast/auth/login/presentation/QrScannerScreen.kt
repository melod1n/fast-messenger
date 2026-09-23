package dev.meloda.fast.auth.login.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.LocalActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import dev.meloda.fast.auth.login.QrCodeAuthParser
import dev.meloda.fast.auth.login.model.QrLoginState
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.common.LocalLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    loginState: QrLoginState,
    onQrCodeScanned: (String) -> Unit,
    onRetry: () -> Unit,
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val logger = LocalLogger.current
    val haptic = LocalHapticFeedback.current
    val parser = remember { QrCodeAuthParser() }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var hintWrongQr by remember { mutableStateOf(false) }
    var scanGeneration by remember { mutableIntStateOf(0) }
    var scanAccepted by remember { mutableStateOf(false) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var permissionRequestInFlight by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            hasCameraPermission = it
            permissionRequestInFlight = false
        }
    )
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            hasCameraPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    )
    LaunchedEffect(loginState) {
        if (loginState is QrLoginState.Error) scanAccepted = false
        if (loginState == QrLoginState.Scanning) {
            scanAccepted = false
            hintWrongQr = false
            scanGeneration++
        }
    }

    val isProcessing = scanAccepted || loginState == QrLoginState.Processing
    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            QrCameraPreview(
                modifier = Modifier.fillMaxSize(),
                scanGeneration = scanGeneration,
                parser = parser,
                onQrAccepted = { qr ->
                    scanAccepted = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onQrCodeScanned(qr)
                },
                onWrongQr = { hintWrongQr = true },
                onQrCleared = { hintWrongQr = false },
                onCameraReady = { camera = it }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.scan_qr_code),
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back_round_24),
                                contentDescription = stringResource(R.string.qr_scanner_back),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = stringResource(
                            when {
                                isProcessing -> R.string.qr_scanner_accepted
                                loginState is QrLoginState.Error -> loginState.messageResId
                                hintWrongQr -> R.string.qr_scanner_hint_wrong
                                else -> R.string.qr_scanner_hint
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (loginState is QrLoginState.Error || hintWrongQr && !isProcessing) {
                            MaterialTheme.colorScheme.error
                        } else Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                if (loginState is QrLoginState.Error) {
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                        Text(stringResource(R.string.try_again))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .border(
                            width = 3.dp,
                            color = when {
                                isProcessing -> MaterialTheme.colorScheme.primary
                                loginState is QrLoginState.Error || hintWrongQr -> MaterialTheme.colorScheme.error
                                else -> Color.White
                            },
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                FilledTonalIconButton(
                    onClick = {
                        val activeCamera = camera ?: return@FilledTonalIconButton
                        val nextTorchState = !isTorchOn
                        val result = activeCamera.cameraControl.enableTorch(nextTorchState)
                        result.addListener({
                            runCatching { result.get() }
                                .onSuccess { isTorchOn = nextTorchState }
                                .onFailure { logger.error("QrScannerScreen", "Failed to toggle torch", it) }
                        }, ContextCompat.getMainExecutor(context))
                    },
                    enabled = camera?.cameraInfo?.hasFlashUnit() == true,
                    modifier = Modifier.padding(bottom = 32.dp).size(64.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isTorchOn) MaterialTheme.colorScheme.primary
                        else Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(
                            if (isTorchOn) R.drawable.ic_flashlight_on_round_24
                            else R.drawable.ic_flashlight_off_round_24
                        ),
                        contentDescription = stringResource(R.string.qr_scanner_torch),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    TopAppBar(
                        title = { Text(stringResource(R.string.scan_qr_code)) },
                        navigationIcon = {
                            IconButton(onClick = onBackClicked) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_back_round_24),
                                    contentDescription = stringResource(R.string.qr_scanner_back)
                                )
                            }
                        }
                    )
                }
            }

            if (!permissionRequestInFlight) {
                val openSettings = permissionRequested && activity?.let {
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        it, Manifest.permission.CAMERA
                    )
                } == true
                AlertDialog(
                    onDismissRequest = onBackClicked,
                    title = { Text(stringResource(R.string.qr_scanner_camera_permission_title)) },
                    text = { Text(stringResource(R.string.qr_scanner_camera_permission)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (openSettings) {
                                    settingsLauncher.launch(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null)
                                        )
                                    )
                                } else {
                                    permissionRequested = true
                                    permissionRequestInFlight = true
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        ) {
                            Text(
                                stringResource(
                                    if (openSettings) R.string.qr_scanner_open_settings
                                    else R.string.qr_scanner_grant_camera
                                )
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onBackClicked) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}
