package com.dreamhome.aicampro

import android.os.Bundle
import java.util.Locale
import java.text.SimpleDateFormat
import java.io.File
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageCapture
import androidx.camera.core.CameraSelector
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.widget.Toast
import android.os.Environment
import android.content.pm.PackageManager
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gold = Color(0xFFD2AA58)
private val DarkBackground = Color(0xFF101318)
private val DarkSurface = Color(0xFF1B2028)
private val LightBackground = Color(0xFFF7F4ED)
private val LightSurface = Color(0xFFFFFFFF)

private data class AppModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val details: List<String>
)

private val appModules = listOf(
    AppModule("Camera Studio", "Capture and edit", Icons.Default.CameraAlt,
        listOf("In-app camera and photo capture", "Video capture", "Photo enhancement and editing", "AI tools will be connected in a later stage")),
    AppModule("AI Clothes Changer", "Virtual outfit preview", Icons.Default.Storefront,
        listOf("Men, women and kids clothing catalogue", "Shirts, trousers, dresses, suits and traditional wear", "Swimwear and bikini outfit try-on for consenting adults", "Upload your own clothing reference", "Outfit replacement only; no undressing or nudity generation")),
    AppModule("Background Studio", "Change photo background", Icons.Default.Image,
        listOf("Keep the original background", "Choose a ready-made background", "Upload a custom background image", "AI subject cut-out and blending integration pending")),
    AppModule("Interior AI Studio", "Preview room designs", Icons.Default.DesignServices,
        listOf("Living room, bedroom, kitchen and bathroom ideas", "False ceiling, wall, paint and wallpaper designs", "Tiles, flooring, furniture and lighting references", "Room photo redesign preview; AI integration pending")),
    AppModule("AI Photo Studio", "Enhance photos", Icons.Default.AutoAwesome,
        listOf("Lighting and colour correction", "Product-photo presentation", "Crop, rotate and collage tools", "Advanced AI enhancement integration pending")),
    AppModule("Measurements", "Room and material calculator", Icons.Default.Calculate,
        listOf("Room-wise area calculations", "Tile quantity and wastage", "Putty, paint, cement and sand estimates", "Separate room entries")),
    AppModule("Material Rates", "Manage current rates", Icons.Default.PriceChange,
        listOf("Add and edit material rates", "Cement, sand, tiles and finishing materials", "Keep rate snapshots with old quotations")),
    AppModule("Quotations", "Create customer PDFs", Icons.Default.Description,
        listOf("Multi-item quotation structure", "20% default markup target", "Warn when markup is below 10%", "Customer PDF hides internal costs and profit")),
    AppModule("Projects", "Track site work", Icons.Default.Folder,
        listOf("Customer and site records", "Work checklist and milestones", "Progress status and photos")),
    AppModule("Customers", "Customer follow-ups", Icons.Default.People,
        listOf("Customer contact details", "Site visit records", "Follow-up reminders and history")),
    AppModule("Payments", "Track money", Icons.Default.Payments,
        listOf("Advance and running payments", "Pending balance", "Project expenses and profitability")),
    AppModule("Business Reports", "Monthly summary", Icons.Default.Summarize,
        listOf("Income and expense summaries", "Profit and pending payments", "Project-wise reports")),
    AppModule("My Designs", "Saved favourites", Icons.Default.Collections,
        listOf("Save favourite clothes and room references", "Save colour combinations", "Local favourites collection")),
    AppModule("Private Gallery", "PIN-protected photos", Icons.Default.Lock,
        listOf("Private local photo collection", "PIN protection", "User-controlled deletion")),
    AppModule("Backup & Restore", "Control your data", Icons.Default.SettingsBackupRestore,
        listOf("Local export and restore", "Optional cloud backup after explicit setup", "No automatic photo uploads")),
    AppModule("App Appearance", "Choose your theme", Icons.Default.Palette,
        listOf("Premium black-and-gold dark theme", "White-and-gold light theme", "Switch theme from the dashboard"))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AiCamProApp() }
    }
}

@Composable
private fun AiCamProApp() {
    var darkTheme by remember { mutableStateOf(true) }
    var selectedModule by remember { mutableStateOf<AppModule?>(null) }

    val background = if (darkTheme) DarkBackground else LightBackground
    val surface = if (darkTheme) DarkSurface else LightSurface
    val foreground = if (darkTheme) Color(0xFFF5F1E8) else Color(0xFF20252D)
    val muted = if (darkTheme) Color(0xFFB7B1A5) else Color(0xFF6C6A64)

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = background) {
            if (selectedModule == null) {
                DashboardScreen(
                    darkTheme = darkTheme,
                    surface = surface,
                    foreground = foreground,
                    muted = muted,
                    onOpen = { selectedModule = it },
                    onToggleTheme = { darkTheme = !darkTheme }
                )
            } else {
                ModuleDetailScreen(
                    module = selectedModule!!,
                    darkTheme = darkTheme,
                    surface = surface,
                    foreground = foreground,
                    muted = muted,
                    onBack = { selectedModule = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(
    darkTheme: Boolean,
    surface: Color,
    foreground: Color,
    muted: Color,
    onOpen: (AppModule) -> Unit,
    onToggleTheme: () -> Unit
) {
    Scaffold(
        containerColor = if (darkTheme) DarkBackground else LightBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI CAM PRO", color = foreground, fontWeight = FontWeight.Bold)
                        Text("DREAM HOME INTERIOR SOLUTION", color = Gold, fontSize = 10.sp, letterSpacing = 1.sp)
                    }
                },
                actions = {
                    Icon(
                        imageVector = if (darkTheme) Icons.Default.WbSunny else Icons.Default.DarkMode,
                        contentDescription = "Switch theme",
                        tint = Gold,
                        modifier = Modifier
                            .padding(horizontal = 18.dp)
                            .size(25.dp)
                            .clickable { onToggleTheme() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (darkTheme) DarkBackground else LightBackground)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Your workspace", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Design, calculate and manage your projects", color = muted, fontSize = 13.sp)
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(appModules) { module ->
                    ModuleCard(module, surface, foreground, muted) { onOpen(module) }
                }
            }
        }
    }
}

@Composable
private fun ModuleCard(
    module: AppModule,
    surface: Color,
    foreground: Color,
    muted: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(142.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(38.dp).background(Gold.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(module.icon, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
            }
            Text(module.title, color = foreground, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(module.subtitle, color = muted, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleDetailScreen(
    module: AppModule,
    darkTheme: Boolean,
    surface: Color,
    foreground: Color,
    muted: Color,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = if (darkTheme) DarkBackground else LightBackground,
        topBar = {
            TopAppBar(
                title = { Text(module.title, color = foreground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Gold,
                        modifier = Modifier.padding(horizontal = 16.dp).size(24.dp).clickable { onBack() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (darkTheme) DarkBackground else LightBackground)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            if (module.title == "Camera Studio") {
                CameraStudioScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "Measurements") {
                MeasurementCalculator(surface = surface, foreground = foreground, muted = muted)
            } else {
                Icon(module.icon, contentDescription = null, tint = Gold, modifier = Modifier.size(42.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(module.title, color = foreground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(module.subtitle, color = muted, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(20.dp))
                module.details.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                        Text("•", color = Gold, fontSize = 18.sp, modifier = Modifier.padding(end = 10.dp))
                        Text(item, color = foreground, fontSize = 15.sp, lineHeight = 21.sp)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(14.dp)) {
                    Text(
                        "This screen is part of the app foundation. Individual tools and AI services will be enabled in development stages.",
                        color = muted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun MeasurementCalculator(surface: Color, foreground: Color, muted: Color) {
    var roomName by remember { mutableStateOf("Bedroom") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var tileLength by remember { mutableStateOf("2") }
    var tileWidth by remember { mutableStateOf("4") }
    var wastage by remember { mutableStateOf("10") }

    val l = length.toDoubleOrNull() ?: 0.0
    val w = width.toDoubleOrNull() ?: 0.0
    val tl = tileLength.toDoubleOrNull() ?: 0.0
    val tw = tileWidth.toDoubleOrNull() ?: 0.0
    val waste = (wastage.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
    val area = l * w
    val tileArea = tl * tw
    val tileCount = if (area > 0 && tileArea > 0) kotlin.math.ceil(area / tileArea * (1.0 + waste / 100.0)).toInt() else 0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Room measurement", color = foreground, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Enter dimensions in feet. Estimates update automatically.", color = muted, fontSize = 13.sp)
        OutlinedTextField(value = roomName, onValueChange = { roomName = it }, label = { Text("Room name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField("Length (ft)", length, { length = it }, Modifier.weight(1f))
            NumberField("Width (ft)", width, { width = it }, Modifier.weight(1f))
        }
        Text("Tile size (feet)", color = Gold, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField("Tile length", tileLength, { tileLength = it }, Modifier.weight(1f))
            NumberField("Tile width", tileWidth, { tileWidth = it }, Modifier.weight(1f))
        }
        NumberField("Wastage (%)", wastage, { wastage = it }, Modifier.fillMaxWidth())
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$roomName estimate", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                ResultLine("Floor area", "%.2f sq ft".format(area), foreground, muted)
                ResultLine("Tile area", if (tileArea > 0) "%.2f sq ft".format(tileArea) else "—", foreground, muted)
                ResultLine("Wastage included", "$waste%", foreground, muted)
                ResultLine("Tiles required", if (tileCount > 0) "$tileCount tiles" else "Enter valid dimensions", foreground, muted)
            }
        }
        Text("Tile count is rounded up and includes the selected wastage. This is an estimate; cutting pattern and site conditions can change actual quantity.", color = muted, fontSize = 12.sp)
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> if (input.isEmpty() || input.matches(Regex("^\\d*(\\.\\d*)?$"))) onValueChange(input) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

@Composable
private fun ResultLine(label: String, value: String, foreground: Color, muted: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = muted, fontSize = 13.sp)
        Text(value, color = foreground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
private fun CameraStudioScreen(surface: Color, foreground: Color, muted: Color) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraMessage by remember { mutableStateOf("Camera permission is required to take photos.") }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        cameraMessage = if (granted) "Camera is ready." else "Camera permission was not granted. Enable it in app settings to use Camera Studio."
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Camera Studio", color = foreground, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Capture photos directly inside AI CAM PRO. Images stay in this app's local media folder.", color = muted, fontSize = 13.sp)
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                capture
                            )
                            imageCapture = capture
                            cameraMessage = "Camera ready. Tap Capture Photo."
                        } catch (error: Exception) {
                            cameraMessage = "Unable to start camera: " + (error.message ?: "unknown error")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
                Text(cameraMessage, color = muted, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
            }
            androidx.compose.material3.Button(
                onClick = {
                    val capture = imageCapture
                    if (capture == null) {
                        cameraMessage = "Camera is still starting. Please wait."
                    } else {
                        val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir, "AI_CAM_PRO")
                        if (!folder.exists()) folder.mkdirs()
                        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
                        val photoFile = File(folder, "AICAM_$stamp.jpg")
                        val output = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                        capture.takePicture(output, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                                cameraMessage = "Photo saved: " + photoFile.name
                                Toast.makeText(context, "Photo saved in AI CAM PRO", Toast.LENGTH_SHORT).show()
                            }
                            override fun onError(exception: ImageCaptureException) {
                                cameraMessage = "Photo capture failed: " + (exception.message ?: "Please try again")
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = imageCapture != null
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Text("  Capture Photo")
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Gold, modifier = Modifier.size(42.dp))
                    Text("Allow camera access", color = foreground, fontWeight = FontWeight.Bold)
                    Text(cameraMessage, color = muted, fontSize = 13.sp)
                    androidx.compose.material3.Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Camera Permission")
                    }
                }
            }
        }
        Text("Captured photos are stored in app-specific storage. Video recording and editing will be added separately.", color = muted, fontSize = 12.sp)
    }
}
