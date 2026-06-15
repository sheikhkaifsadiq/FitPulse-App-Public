package com.fitpulse.app.ui.diary

import android.graphics.*
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.data.repository.FoodRepository
import com.fitpulse.app.data.repository.GeminiRepository
import com.fitpulse.app.data.repository.SelectedFoodRepository
import com.fitpulse.app.ui.navigation.Screen
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(navController: NavController) {
    if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        BarcodeScannerContent(
            isFlashOn = false,
            isAnalyzing = false,
            isLookingUpBarcode = false,
            analysisStatus = "Position barcode in the frame",
            onBackClick = {},
            onFlashToggle = {},
            onCaptureClick = {},
            onGalleryClick = {},
            cameraPreview = {
                Box(Modifier.fillMaxSize().background(Color.DarkGray)) {
                    Text("Camera Preview", color = Color.White, modifier = Modifier.align(Alignment.Center))
                }
            }
        )
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getInstance(context) }
    val foodRepository = remember { FoodRepository(database.foodDao()) }
    
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    
    var showConfirmDialog by remember { mutableStateOf<ScannedFoodInfo?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isLookingUpBarcode by remember { mutableStateOf(false) }
    var analysisStatus by remember { mutableStateOf("Position barcode in the frame") }

    val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()
    val barcodeScanner = remember { BarcodeScanning.getClient(scannerOptions) }
    val geminiRepo = remember { GeminiRepository() }

    // Multi-pass Deep Scan Pipeline
    suspend fun performDeepScan(bitmap: Bitmap): ScannedFoodInfo? {
        // Pass 1: Direct ML Kit attempt (Fast Barcode)
        analysisStatus = "Checking for barcode..."
        val pass1Input = InputImage.fromBitmap(bitmap, 0)
        val barcodes = barcodeScanner.process(pass1Input).await()
        if (barcodes.isNotEmpty()) {
            val code = barcodes.first().rawValue
            // Simple validation: Ensure barcode is not just a few random chars
            if (code != null && code.length >= 8) {
                return ScannedFoodInfo(isBarcode = true, barcodeValue = code, name = "Product $code")
            }
        }

        // Pass 2: AI Identification (Name only, strict separation)
        analysisStatus = "Identifying object..."
        val aiResult = geminiRepo.analyzeFoodImage(bitmap)
        if (aiResult != null) {
            try {
                val cleanedJson = aiResult.substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleanedJson}")
                val status = json.optString("status")
                
                if (status == "NOT_FOOD") {
                    analysisStatus = "Object is not food."
                    return null
                }
                
                return ScannedFoodInfo(
                    name = json.optString("foodName", "Unknown Food"),
                    serving = json.optString("portion", "1 portion"),
                    calories = json.optInt("calories", 0),
                    protein = json.optDouble("protein", 0.0).toFloat(),
                    carbs = json.optDouble("carbs", 0.0).toFloat(),
                    fat = json.optDouble("fat", 0.0).toFloat()
                )
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

    fun startAnalysis(bitmap: Bitmap) {
        if (isAnalyzing) return
        isAnalyzing = true
        scope.launch {
            val result = performDeepScan(bitmap)
            isAnalyzing = false
            if (result != null) {
                // For barcodes, we could fetch data here, but for now we let user confirm
                showConfirmDialog = result
            } else {
                analysisStatus = "Scan failed."
                Toast.makeText(context, "Item not recognized as food.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Auto-focus loop
    LaunchedEffect(cameraControl) {
        cameraControl?.let { control ->
            while (true) {
                val factory = SurfaceOrientedMeteringPointFactory(1f, 1f)
                val point = factory.createPoint(0.5f, 0.5f) // Center
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF).build()
                control.startFocusAndMetering(action)
                delay(4000L) // Refocus every 4 seconds
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    startAnalysis(bitmap)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BarcodeScannerContent(
        isFlashOn = isFlashOn,
        isAnalyzing = isAnalyzing,
        isLookingUpBarcode = isLookingUpBarcode,
        analysisStatus = analysisStatus,
        onBackClick = { navController.popBackStack() },
        onFlashToggle = {
            isFlashOn = !isFlashOn
            cameraControl?.enableTorch(isFlashOn)
        },
        onCaptureClick = {
            val capture = imageCapture ?: return@BarcodeScannerContent
            capture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val bitmap = image.toBitmap()
                        startAnalysis(bitmap)
                        image.close()
                    }
                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        },
        onGalleryClick = { galleryLauncher.launch("image/*") },
        cameraPreview = {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                            .build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                        
                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        // Continuous fast scanning analyzer
                        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                            @androidx.camera.core.ExperimentalGetImage
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !isAnalyzing && !isLookingUpBarcode) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (barcodes.isNotEmpty()) {
                                            val value = barcodes.first().rawValue
                                            if (value != null && value.length >= 8) {
                                                scope.launch {
                                                    isLookingUpBarcode = true
                                                    analysisStatus = "Looking up barcode..."
                                                    val result = foodRepository.lookupBarcode(value)
                                                    if (result != null) {
                                                        // Found in Open Food Facts — prefill detail screen
                                                        SelectedFoodRepository.setSelectedFood(result)
                                                        navController.navigate(
                                                            Screen.FoodDetail.createRoute(result.name, "SNACK", isManual = false)
                                                        )
                                                    } else {
                                                        // Not in database — manual entry
                                                        SelectedFoodRepository.clear()
                                                        navController.navigate(
                                                            Screen.FoodDetail.createRoute("Barcode: $value", "SNACK", isManual = true)
                                                        )
                                                    }
                                                    isLookingUpBarcode = false
                                                    analysisStatus = "Position barcode in the frame"
                                                }
                                            }
                                        }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture,
                                imageAnalysis
                            )
                            cameraControl = camera.cameraControl
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    )

    // Deep-scan (AI image capture) result handler — also does API lookup
    showConfirmDialog?.let { info ->
        LaunchedEffect(info) {
            if (info.isBarcode && info.barcodeValue != null) {
                // Try reverse-lookup for captured barcode images
                val result = foodRepository.lookupBarcode(info.barcodeValue)
                if (result != null) {
                    SelectedFoodRepository.setSelectedFood(result)
                    navController.navigate(Screen.FoodDetail.createRoute(result.name, "SNACK", isManual = false))
                } else {
                    SelectedFoodRepository.clear()
                    navController.navigate(Screen.FoodDetail.createRoute("Barcode: ${info.barcodeValue}", "SNACK", isManual = true))
                }
            } else {
                // AI-identified food — use Gemini's name to do a search
                val result = foodRepository.lookupBarcode(info.name)  // try by name, usually null
                SelectedFoodRepository.clear()
                navController.navigate(Screen.FoodDetail.createRoute(info.name, "SNACK", isManual = true))
            }
            showConfirmDialog = null
        }
    }
}

@Composable
fun ScannerOverlay() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val cutoutSize = 280.dp.toPx()
        val left = (size.width - cutoutSize) / 2
        val top = (size.height - cutoutSize) / 2 - 50.dp.toPx()

        val path = androidx.compose.ui.graphics.Path().apply {
            addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
            addRect(androidx.compose.ui.geometry.Rect(left, top, left + cutoutSize, top + cutoutSize))
            fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
        }
        drawPath(path, Color.Black.copy(alpha = 0.5f))

        val stroke = 4.dp.toPx()
        val len = 40.dp.toPx()
        val color = Color(0xFFBEF264)

        drawLine(color, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left + len, top), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left, top + len), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(left + cutoutSize, top), androidx.compose.ui.geometry.Offset(left + cutoutSize - len, top), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(left + cutoutSize, top), androidx.compose.ui.geometry.Offset(left + cutoutSize, top + len), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(left, top + cutoutSize), androidx.compose.ui.geometry.Offset(left + len, top + cutoutSize), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(left, top + cutoutSize), androidx.compose.ui.geometry.Offset(left + cutoutSize - len, top + cutoutSize), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(left + cutoutSize, top + cutoutSize), androidx.compose.ui.geometry.Offset(left + cutoutSize - len, top + cutoutSize), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(left + cutoutSize, top + cutoutSize), androidx.compose.ui.geometry.Offset(left + cutoutSize, top + cutoutSize - len), stroke)
    }
}

fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

data class ScannedFoodInfo(
    val name: String = "",
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val serving: String = "",
    val isBarcode: Boolean = false,
    val barcodeValue: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerContent(
    isFlashOn: Boolean,
    isAnalyzing: Boolean,
    isLookingUpBarcode: Boolean,
    analysisStatus: String,
    onBackClick: () -> Unit,
    onFlashToggle: () -> Unit,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    cameraPreview: @Composable () -> Unit
) {
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Scanner", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { pv ->
        Box(modifier = Modifier.fillMaxSize().padding(pv)) {
            cameraPreview()

            ScannerOverlay()

            // Status Text HUD
            Column(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isAnalyzing) "Deep Scan Active" else "Scanning...",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFBEF264),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color.Black.copy(0.6f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = analysisStatus,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            if (isAnalyzing || isLookingUpBarcode) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFBEF264), strokeWidth = 3.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (isLookingUpBarcode) "Searching Open Food Facts..." else "Deep Scan Active",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 64.dp, start = 32.dp, end = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash Toggle
                IconButton(
                    onClick = onFlashToggle,
                    modifier = Modifier.size(56.dp).background(Color.Black.copy(0.5f), CircleShape)
                ) {
                    Icon(if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff, "Flash", tint = Color.White)
                }

                // Capture Button (Deep Scan Trigger)
                IconButton(
                    onClick = onCaptureClick,
                    modifier = Modifier.size(80.dp).background(Color(0xFFBEF264), CircleShape)
                ) {
                    Icon(Icons.Default.PhotoCamera, "Capture", modifier = Modifier.size(36.dp), tint = Color.Black)
                }

                // Gallery Button
                IconButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.size(56.dp).background(Color.Black.copy(0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.PhotoLibrary, "Gallery", tint = Color.White)
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun BarcodeScannerPreview() {
    MaterialTheme {
        BarcodeScannerContent(
            isFlashOn = false,
            isAnalyzing = false,
            isLookingUpBarcode = false,
            analysisStatus = "Position barcode in the frame",
            onBackClick = {},
            onFlashToggle = {},
            onCaptureClick = {},
            onGalleryClick = {},
            cameraPreview = {
                Box(Modifier.fillMaxSize().background(Color.DarkGray)) {
                    Text("Camera Preview", color = Color.White, modifier = Modifier.align(Alignment.Center))
                }
            }
        )
    }
}
