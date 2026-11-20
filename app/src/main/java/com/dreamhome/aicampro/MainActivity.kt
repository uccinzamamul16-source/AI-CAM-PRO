package com.dreamhome.aicampro

import android.os.Bundle
import android.net.Uri
import android.widget.ImageView
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
import androidx.camera.core.Camera
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.widget.Toast
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.ValueCallback
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.key
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
                    onBack = { selectedModule = null },
                    onToggleTheme = { darkTheme = !darkTheme }
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
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).background(Gold.copy(alpha = 0.16f), RoundedCornerShape(17.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Gold, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Your workspace", color = foreground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Design • calculate • manage", color = muted, fontSize = 12.sp)
                        Text("SMART TOOLS FOR YOUR BUSINESS", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("EXPLORE TOOLS", color = foreground, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("${appModules.size} MODULES", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 18.dp),
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
        modifier = Modifier.fillMaxWidth().height(154.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Gold.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(module.icon, contentDescription = null, tint = Gold, modifier = Modifier.size(23.dp))
                }
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Gold.copy(alpha = 0.65f), modifier = Modifier.size(16.dp))
            }
            Text(module.title, color = foreground, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Text(module.subtitle, color = muted, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 2)
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
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
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
        val pageNeedsOwnScrolling = module.title in setOf(
            "Camera Studio", "AI Clothes Changer", "Background Studio", "Interior AI Studio"
        )
        val pageModifier = if (pageNeedsOwnScrolling) {
            Modifier.fillMaxSize().padding(padding).padding(20.dp)
        } else {
            Modifier.fillMaxWidth().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())
        }
        Column(modifier = pageModifier) {
            if (module.title == "Camera Studio") {
                CameraStudioScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "AI Clothes Changer") {
                FreeAiTryOnScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "Background Studio") {
                FreeWebAiScreen(
                    title = "Free Background Studio",
                    description = "Remove a photo background in your browser. The selected tool says processing happens on your device, without uploading the photo.",
                    url = "https://kitlibre.com/remove-bg/",
                    surface = surface, foreground = foreground, muted = muted
                )
            } else if (module.title == "Interior AI Studio") {
                FreeWebAiScreen(
                    title = "Free Interior AI Studio",
                    description = "Upload a room photo, choose a design style and generate a redesign. Free use is limited to five redesigns per hour according to the service.",
                    url = "https://www.freeredesign.com/",
                    surface = surface, foreground = foreground, muted = muted
                )
            } else if (module.title == "Measurements") {
                MeasurementCalculator(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "Customers") {
                BusinessRecordsScreen(isProjects = false, surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "Projects") {
                BusinessRecordsScreen(isProjects = true, surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "Material Rates") {
                MaterialRatesScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "Quotations") {
                QuotationMakerScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "Payments") {
                PaymentsScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "Business Reports") {
                BusinessReportsScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "AI Photo Studio") {
                AiPhotoStudioScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "My Designs") {
                MyDesignsScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "Private Gallery") {
                PrivateGalleryScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "Backup & Restore") {
                BackupRestoreScreen(surface = surface, foreground = foreground, muted = muted)
            } else if (module.title == "App Appearance") {
                AppAppearanceScreen(darkTheme = darkTheme, onToggleTheme = onToggleTheme, surface = surface, foreground = foreground, muted = muted)
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
private fun FreeAiTryOnScreen(surface: Color, foreground: Color, muted: Color) {
    var uploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uploadCallback?.onReceiveValue(if (uri != null) arrayOf(uri) else null)
        uploadCallback = null
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Free virtual try-on", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Upload a person's photo and a clothing photo in the demo below, then tap Try-on.",
            color = muted,
            fontSize = 13.sp
        )
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
            Text(
                "Free shared demo: daily limits and waiting queues may apply. Photos are sent to Hugging Face for processing when you upload them. Use only photos you have permission to use. This research model is licensed for non-commercial use only.",
                color = muted,
                fontSize = 11.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            uploadCallback?.onReceiveValue(null)
                            uploadCallback = filePathCallback
                            imagePicker.launch("image/*")
                            return true
                        }
                    }
                    loadUrl("https://yisol-idm-vton.hf.space/")
                }
            },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}

@Composable
private fun FreeWebAiScreen(
    title: String,
    description: String,
    url: String,
    surface: Color,
    foreground: Color,
    muted: Color
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
            Text(description, color = muted, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
        }
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
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
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }
    var cameraMessage by remember { mutableStateOf("Camera permission is required to take photos.") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedPhotoUri = uri
        if (uri != null) cameraMessage = "Gallery photo selected. AI editing tools will be connected in a later stage."
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        cameraMessage = if (granted) "Camera is ready." else "Camera permission was not granted. Enable it in app settings to use Camera Studio."
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Camera Studio", color = foreground, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Full-screen preview • Photo & video", color = muted, fontSize = 11.sp)
            }
            if (hasCameraPermission) {
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        if (flashEnabled) boundCamera?.cameraControl?.enableTorch(false)
                        flashEnabled = false
                        useFrontCamera = !useFrontCamera
                        if (isRecording) {
                            activeRecording?.stop()
                            activeRecording = null
                            isRecording = false
                        }
                        imageCapture = null
                        videoCapture = null
                        boundCamera = null
                        hasFlash = false
                        cameraMessage = "Switching camera..."
                    }
                ) {
                    Text(if (useFrontCamera) "Rear" else "Front")
                }
            }
        }
        if (hasCameraPermission) {
            key(useFrontCamera) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                                val capture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .build()
                                val recorder = Recorder.Builder()
                                    .setQualitySelector(QualitySelector.from(Quality.HD))
                                    .build()
                                val video = VideoCapture.withOutput(recorder)
                                cameraProvider.unbindAll()
                                val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                                val camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture, video)
                                boundCamera = camera
                                hasFlash = camera.cameraInfo.hasFlashUnit()
                                flashEnabled = false
                                imageCapture = capture
                                videoCapture = video
                                cameraMessage = if (useFrontCamera) "Front camera ready." else "Rear camera ready."
                            } catch (error: Exception) {
                                imageCapture = null
                                videoCapture = null
                                boundCamera = null
                                hasFlash = false
                                cameraMessage = "Unable to start camera: " + (error.message ?: "camera may not be available")
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        val next = !flashEnabled
                        boundCamera?.cameraControl?.enableTorch(next)
                        flashEnabled = next
                        cameraMessage = if (next) "Flash is ON." else "Flash is OFF."
                    },
                    modifier = Modifier.weight(1f),
                    enabled = hasFlash && boundCamera != null && !useFrontCamera && !isRecording
                ) {
                    Text(if (flashEnabled) "Flash ON" else "Flash OFF")
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
                    modifier = Modifier.weight(1f),
                    enabled = imageCapture != null
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Text("  Capture")
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Button(
                    onClick = {
                        val video = videoCapture
                        if (video == null) {
                            cameraMessage = "Camera is still starting. Please wait."
                        } else if (isRecording) {
                            activeRecording?.stop()
                            activeRecording = null
                            isRecording = false
                            cameraMessage = "Finishing video..."
                        } else {
                            val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir, "AI_CAM_PRO")
                            if (!folder.exists()) folder.mkdirs()
                            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
                            val videoFile = File(folder, "AICAM_$stamp.mp4")
                            val output = FileOutputOptions.Builder(videoFile).build()
                            try {
                                activeRecording = video.output.prepareRecording(context, output)
                                    .start(ContextCompat.getMainExecutor(context)) { event ->
                                        when (event) {
                                            is VideoRecordEvent.Start -> {
                                                isRecording = true
                                                cameraMessage = "Recording video..."
                                            }
                                            is VideoRecordEvent.Finalize -> {
                                                isRecording = false
                                                activeRecording = null
                                                cameraMessage = if (event.hasError()) "Video recording failed. Please try again." else "Video saved: " + videoFile.name
                                            }
                                        }
                                    }
                            } catch (error: Exception) {
                                isRecording = false
                                cameraMessage = "Unable to record video: " + (error.message ?: "unknown error")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = videoCapture != null
                ) {
                    Text(if (isRecording) "Stop Video" else "Record Video")
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Collections, contentDescription = null)
                    Text("  Gallery")
                }
            }
            Text(cameraMessage, color = muted, fontSize = 11.sp, maxLines = 2)
            selectedPhotoUri?.let { uri ->
                AndroidView(
                    factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
                    update = { view -> view.setImageURI(uri) },
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )
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
        Text("Flash availability depends on your phone's rear camera. Photos and videos remain in AI CAM PRO's local storage.", color = muted, fontSize = 10.sp)
    }
}
