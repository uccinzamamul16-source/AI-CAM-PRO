package com.dreamhome.aicampro

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.security.MessageDigest

private val ToolGold = Color(0xFFD2AA58)

@Composable
internal fun AiPhotoStudioScreen(surface: Color, foreground: Color, muted: Color) {
    val context = LocalContext.current
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var rotation by remember { mutableIntStateOf(0) }
    var grayscale by remember { mutableStateOf(false) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var message by remember { mutableStateOf("Choose a photo to start editing.") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                original = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                rotation = 0
                grayscale = false
                brightness = 0f
                message = if (original != null) "Photo loaded. Adjust it below." else "Could not read this image."
            } catch (e: Exception) { message = "Could not open photo: " + (e.message ?: "unknown error") }
        }
    }
    val edited = remember(original, rotation, grayscale, brightness) {
        original?.let { makeEditedBitmap(it, rotation, grayscale, brightness) }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("AI Photo Studio", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Basic on-device photo editing. These tools do not require a paid AI API.", color = muted, fontSize = 12.sp)
        Button(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Choose Photo") }
        edited?.let { bitmap ->
            Image(bitmap.asImageBitmap(), contentDescription = "Photo preview", modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { rotation = (rotation + 90) % 360 }, modifier = Modifier.weight(1f)) { Text("Rotate 90°") }
                OutlinedButton(onClick = { grayscale = !grayscale }, modifier = Modifier.weight(1f)) { Text(if (grayscale) "Color On" else "B&W") }
            }
            Text("Brightness", color = foreground, fontWeight = FontWeight.SemiBold)
            Slider(value = brightness, onValueChange = { brightness = it }, valueRange = -80f..80f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Darker", color = muted, fontSize = 11.sp)
                Text("Brighter", color = muted, fontSize = 11.sp)
            }
            Button(onClick = {
                message = try {
                    val saved = saveBitmapToGallery(context, bitmap)
                    if (saved) "Edited photo saved to Pictures/AI CAM PRO." else "Could not save photo."
                } catch (e: Exception) { "Save failed: " + (e.message ?: "unknown error") }
            }, modifier = Modifier.fillMaxWidth()) { Text("Save Edited Photo") }
            TextButton(onClick = { rotation = 0; grayscale = false; brightness = 0f; message = "Edits reset." }) { Text("Reset edits") }
        }
        Text(message, color = ToolGold, fontSize = 12.sp)
        Text("This is a basic photo editor, not generative AI retouching. The selected photo is processed locally and is not uploaded.", color = muted, fontSize = 11.sp)
    }
}

private fun makeEditedBitmap(source: Bitmap, rotation: Int, grayscale: Boolean, brightness: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    val output = Bitmap.createBitmap(rotated.width, rotated.height, Bitmap.Config.ARGB_8888)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    val color = ColorMatrix()
    if (grayscale) color.setSaturation(0f)
    val values = color.array
    values[4] = brightness
    values[9] = brightness
    values[14] = brightness
    values[19] = 0f
    paint.colorFilter = ColorMatrixColorFilter(ColorMatrix(values))
    Canvas(output).drawBitmap(rotated, 0f, 0f, paint)
    if (rotated !== source && rotated !== output) rotated.recycle()
    return output
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "AICAM_EDIT_" + System.currentTimeMillis() + ".jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AI CAM PRO")
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    return try {
        context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 94, it) } ?: false
    } catch (e: Exception) {
        context.contentResolver.delete(uri, null, null)
        throw e
    }
}

@Composable
internal fun MyDesignsScreen(surface: Color, foreground: Color, muted: Color) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("my_designs", Context.MODE_PRIVATE) }
    val saved = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("items", emptySet()) ?: emptySet()) } }
    var label by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val entry = label.trim().ifBlank { "Saved design" } + "|" + uri.toString()
                if (saved.none { it.substringAfter("|", "") == uri.toString() }) {
                    saved.add(0, entry)
                    prefs.edit().putStringSet("items", saved.toSet()).apply()
                    message = "Design saved to My Designs."
                } else message = "This design is already saved."
                label = ""
            } catch (e: Exception) { message = "Could not save design: " + (e.message ?: "unknown error") }
        }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("My Designs", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Keep favourite room, outfit and reference photos on this device.", color = muted, fontSize = 12.sp)
        OutlinedTextField(label, { label = it }, label = { Text("Design name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(onClick = { picker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Add Design from Gallery") }
        Text("Saved designs (" + saved.size + ")", color = ToolGold, fontWeight = FontWeight.Bold)
        if (saved.isEmpty()) Text("No saved designs yet.", color = muted)
        saved.forEach { entry ->
            val parts = entry.split("|", limit = 2)
            Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(parts.firstOrNull().orEmpty(), color = foreground, fontWeight = FontWeight.SemiBold)
                    if (parts.size == 2) {
                        val uri = runCatching { android.net.Uri.parse(parts[1]) }.getOrNull()
                        if (uri != null) AndroidView(factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.FIT_CENTER } }, update = { it.setImageURI(uri) }, modifier = Modifier.fillMaxWidth().height(150.dp))
                    }
                    TextButton(onClick = { saved.remove(entry); prefs.edit().putStringSet("items", saved.toSet()).apply(); message = "Design removed." }) { Text("Remove") }
                }
            }
        }
        if (message.isNotBlank()) Text(message, color = ToolGold, fontSize = 12.sp)
    }
}

@Composable
internal fun PrivateGalleryScreen(surface: Color, foreground: Color, muted: Color) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("private_gallery", Context.MODE_PRIVATE) }
    var pin by remember { mutableStateOf("") }
    var unlocked by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var files by remember { mutableStateOf(emptyList<File>()) }
    val folder = remember { File(context.filesDir, "private_gallery").apply { mkdirs() } }
    fun refresh() { files = folder.listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() } ?: emptyList() }
    LaunchedEffect(unlocked) { if (unlocked) refresh() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && unlocked) {
            try {
                val target = File(folder, "private_" + System.currentTimeMillis() + ".jpg")
                context.contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(target).use { output -> input.copyTo(output) } }
                refresh()
                message = "Photo copied into app-private storage."
            } catch (e: Exception) { message = "Could not import photo: " + (e.message ?: "unknown error") }
        }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Private Gallery", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Photos copied here are stored in this app's private internal folder, not the public gallery.", color = muted, fontSize = 12.sp)
        if (!unlocked) {
            val storedHash = prefs.getString("pin_hash", null)
            val hasPin = !storedHash.isNullOrBlank()
            OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(8) }, label = { Text(if (hasPin) "Enter PIN" else "Create PIN (4–8 digits)") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                val enteredHash = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
                if (pin.length !in 4..8) message = "PIN must be 4 to 8 digits."
                else if (hasPin && enteredHash != storedHash) message = "Incorrect PIN."
                else {
                    if (!hasPin) prefs.edit().putString("pin_hash", enteredHash).apply()
                    unlocked = true
                    pin = ""
                    message = if (hasPin) "Gallery unlocked." else "PIN created. Keep it safe; there is no PIN recovery."
                    refresh()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text(if (hasPin) "Unlock Gallery" else "Set PIN & Open Gallery") }
            Text("If you forget this PIN, there is no recovery option. Uninstalling the app deletes this private gallery.", color = muted, fontSize = 11.sp)
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { picker.launch("image/*") }, modifier = Modifier.weight(1f)) { Text("Add Photo") }
                OutlinedButton(onClick = { unlocked = false; pin = "" }, modifier = Modifier.weight(1f)) { Text("Lock Gallery") }
            }
            Text("Private photos (" + files.size + ")", color = ToolGold, fontWeight = FontWeight.Bold)
            if (files.isEmpty()) Text("No private photos yet.", color = muted)
            files.forEach { file ->
                Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(8.dp)) {
                        AndroidView(factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.CENTER_CROP } }, update = { it.setImageURI(android.net.Uri.fromFile(file)) }, modifier = Modifier.fillMaxWidth().height(180.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(file.name, color = foreground, fontSize = 11.sp)
                            TextButton(onClick = { file.delete(); refresh(); message = "Photo deleted." }) { Text("Delete") }
                        }
                    }
                }
            }
        }
        if (message.isNotBlank()) Text(message, color = ToolGold, fontSize = 12.sp)
    }
}

@Composable
internal fun BackupRestoreScreen(surface: Color, foreground: Color, muted: Color) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("Create a backup ZIP of local business databases and private-gallery photos.") }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) {
            try {
                val dbDir = context.getDatabasePath("placeholder").parentFile
                val privateDir = File(context.filesDir, "private_gallery")
                context.contentResolver.openOutputStream(uri)?.use { raw ->
                    java.util.zip.ZipOutputStream(raw).use { zip ->
                        fun addFile(file: File, prefix: String) {
                            if (file.isFile) {
                                zip.putNextEntry(java.util.zip.ZipEntry(prefix + file.name))
                                file.inputStream().use { it.copyTo(zip) }
                                zip.closeEntry()
                            }
                        }
                        dbDir?.listFiles()?.filter { it.isFile }?.forEach { addFile(it, "databases/") }
                        privateDir.listFiles()?.filter { it.isFile }?.forEach { addFile(it, "private_gallery/") }
                        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
                        prefsDir.listFiles()?.filter { it.isFile }?.forEach { addFile(it, "shared_prefs/") }
                    }
                }
                message = "Backup ZIP created. Store it somewhere safe. This archive may contain customer and private data."
            } catch (e: Exception) { message = "Backup failed: " + (e.message ?: "unknown error") }
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                val restoreDir = File(context.filesDir, "restore_import").apply { mkdirs() }
                var count = 0
                context.contentResolver.openInputStream(uri)?.use { input ->
                    java.util.zip.ZipInputStream(input).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            val name = entry.name.replace("\\", "/")
                            if (!entry.isDirectory && !name.startsWith("/") && !name.split("/").contains("..") &&
                                (name.startsWith("private_gallery/") || name.startsWith("databases/") || name.startsWith("shared_prefs/"))) {
                                val out = File(restoreDir, name)
                                out.parentFile?.mkdirs()
                                FileOutputStream(out).use { output -> zip.copyTo(output) }
                                count++
                            }
                            zip.closeEntry()
                            entry = zip.nextEntry
                        }
                    }
                }
                message = "Backup checked and " + count + " files staged in the app's restore_import folder. Database restore is not applied automatically; contact support before replacing live data."
            } catch (e: Exception) { message = "Could not read backup ZIP: " + (e.message ?: "unknown error") }
        }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Backup & Restore", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Export a local backup ZIP. Import checks the archive and stages its files safely without overwriting live databases.", color = muted, fontSize = 12.sp)
        Button(onClick = { export.launch("AI_CAM_PRO_Backup_" + SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date()) + ".zip") }, modifier = Modifier.fillMaxWidth()) { Text("Create Backup ZIP") }
        OutlinedButton(onClick = { import.launch(arrayOf("application/zip", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) { Text("Import / Check Backup ZIP") }
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
            Text(message, color = foreground, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
        }
        Text("Keep backups private: they can include customer records, transactions and private photos. Import currently stages files for safe manual recovery; it does not replace live databases.", color = muted, fontSize = 11.sp)
    }
}

@Composable
internal fun AppAppearanceScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    surface: Color,
    foreground: Color,
    muted: Color
) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("App Appearance", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Choose the look of your workspace.", color = muted, fontSize = 12.sp)
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (darkTheme) "Black & Gold theme is active" else "White & Gold theme is active", color = foreground, fontWeight = FontWeight.SemiBold)
                Button(onClick = onToggleTheme, modifier = Modifier.fillMaxWidth()) {
                    Text(if (darkTheme) "Switch to White & Gold" else "Switch to Black & Gold")
                }
            }
        }
        Text("Theme selection applies immediately. Current selection lasts until the app process is restarted.", color = muted, fontSize = 11.sp)
    }
}
