package app.simetris.simapp.media.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.simetris.simapp.R
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CameraScreen(goBack: () -> Unit, onCaptured: (ByteArray) -> Unit) {
    val context = LocalContext.current
    val closeDescription = stringResource(R.string.close)
    val captureDescription = stringResource(R.string.take_photo)
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionGranted = it
    }
    var capture by remember { mutableStateOf<ImageCapture?>(null) }
    var provider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var capturing by remember { mutableStateOf(false) }
    var captureError by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val window = (context as? Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, it.decorView) }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }

    DisposableEffect(Unit) {
        onDispose {
            active = false
            provider?.unbindAll()
        }
    }
    LaunchedEffect(Unit) {
        if (!permissionGranted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun takePicture() {
        val camera = capture ?: return
        if (capturing) return
        capturing = true
        captureError = false
        val file = try {
            File.createTempFile("captura", ".jpg", context.cacheDir)
        } catch (_: Exception) {
            capturing = false
            captureError = true
            return
        }
        camera.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(),
            ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                    scope.launch(Dispatchers.IO) {
                        val bytes = runCatching { file.readBytes() }.getOrNull()
                        file.delete()
                        withContext(Dispatchers.Main) {
                            capturing = false
                            if (active && bytes != null) onCaptured(bytes)
                            else if (active) captureError = true
                        }
                    }
                }
                override fun onError(error: ImageCaptureException) {
                    file.delete()
                    capturing = false
                    captureError = true
                }
            })
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (permissionGranted) AndroidView(factory = { cameraContext ->
            PreviewView(cameraContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                val future = ProcessCameraProvider.getInstance(cameraContext)
                future.addListener({
                    val cameraProvider = future.get()
                    if (!active) return@addListener
                    provider = cameraProvider
                    val preview = CameraPreview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }
                    val photo = ImageCapture.Builder().build()
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA, preview, photo)
                    capture = photo
                }, ContextCompat.getMainExecutor(cameraContext))
            }
        }, modifier = Modifier.fillMaxSize())
        else Column(Modifier.align(Alignment.Center).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.camera_permission), color = Color.White)
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.allow_camera))
            }
        }

        IconButton(onClick = goBack,
            modifier = Modifier.align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(12.dp)
                .semantics { contentDescription = closeDescription }) {
            Icon(painterResource(R.drawable.ic_close), contentDescription = null,
                tint = Color.White)
        }
        if (captureError) Text(stringResource(R.string.camera_capture_failed),
            modifier = Modifier.align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 120.dp),
            color = Color.White)
        if (permissionGranted) IconButton(onClick = ::takePicture,
            enabled = capture != null && !capturing,
            modifier = Modifier.align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 22.dp).size(84.dp)
                .border(4.dp, Color.White, CircleShape)
                .semantics { contentDescription = captureDescription }) {
            Box(Modifier.size(62.dp).background(
                if (capturing) Color.LightGray else Color.White, CircleShape))
        }
    }
}
