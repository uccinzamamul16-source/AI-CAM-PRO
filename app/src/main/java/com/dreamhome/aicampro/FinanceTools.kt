package com.dreamhome.aicampro

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private val FinanceGold = Color(0xFFD2AA58)

private data class MaterialRate(val id: Long, val name: String, val unit: String, val rate: Double)
private data class QuoteLine(val name: String, val quantity: Double, val costRate: Double)

private class FinanceDatabase(context: Context) : SQLiteOpenHelper(context, "ai_cam_finance.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE material_rates (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, unit TEXT NOT NULL, rate REAL NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun addRate(name: String, unit: String, rate: Double) {
        val values = ContentValues().apply { put("name", name); put("unit", unit); put("rate", rate) }
        writableDatabase.insert("material_rates", null, values)
    }
    fun updateRate(id: Long, name: String, unit: String, rate: Double) {
        val values = ContentValues().apply { put("name", name); put("unit", unit); put("rate", rate) }
        writableDatabase.update("material_rates", values, "id=?", arrayOf(id.toString()))
    }
    fun rates(): List<MaterialRate> {
        val result = mutableListOf<MaterialRate>()
        readableDatabase.query("material_rates", null, null, null, null, null, "name COLLATE NOCASE ASC").use { c ->
            while (c.moveToNext()) result.add(MaterialRate(c.getLong(0), c.getString(1), c.getString(2), c.getDouble(3)))
        }
        return result
    }
    fun deleteRate(id: Long) { writableDatabase.delete("material_rates", "id=?", arrayOf(id.toString())) }
}

@Composable
internal fun MaterialRatesScreen(surface: Color, foreground: Color, muted: Color) {
    val context = LocalContext.current
    val db = remember { FinanceDatabase(context.applicationContext) }
    val rates = remember { mutableStateListOf<MaterialRate>() }
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("sq ft") }
    var rate by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Long?>(null) }
    fun refresh() { rates.clear(); rates.addAll(db.rates()) }
    LaunchedEffect(Unit) { refresh() }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        Text("Material Rate Manager", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Add current material prices. Rates stay on this device.", color = muted, fontSize = 13.sp)
        OutlinedTextField(name, { name = it }, label = { Text("Material / work name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(unit, { unit = it }, label = { Text("Unit (sq ft, bag, kg)") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(rate, { rate = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Rate ₹") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
        }
        Button(onClick = {
            val amount = rate.toDoubleOrNull()
            if (name.isBlank() || unit.isBlank() || amount == null || amount < 0) message = "Enter material, unit and a valid rate."
            else { val id = editingId; if (id == null) { db.addRate(name.trim(), unit.trim(), amount); message = "Rate saved." } else { db.updateRate(id, name.trim(), unit.trim(), amount); editingId = null; message = "Rate updated." }; name = ""; rate = ""; unit = "sq ft"; refresh() }
        }, modifier = Modifier.fillMaxWidth()) { Text(if (editingId == null) "Save Material Rate" else "Update Material Rate") }
        if (message.isNotBlank()) Text(message, color = FinanceGold, fontSize = 12.sp)
        Text("Saved rates (${rates.size})", color = FinanceGold, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(rates, key = { it.id }) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(item.name, color = foreground, fontWeight = FontWeight.SemiBold); Text("₹${"%.2f".format(item.rate)} / ${item.unit}", color = muted, fontSize = 12.sp) }
                        Row { TextButton(onClick = { editingId = item.id; name = item.name; unit = item.unit; rate = item.rate.toString(); message = "Editing ${item.name}" }) { Text("Edit", color = FinanceGold) }; TextButton(onClick = { if (editingId == item.id) { editingId = null; name = ""; rate = "" }; db.deleteRate(item.id); refresh() }) { Text("Delete", color = FinanceGold) } }
                    }
                }
            }
        }
        Text("Rates can be edited or deleted. Historical rate snapshots are not yet available.", color = muted, fontSize = 11.sp)
    }
}

@Composable
internal fun QuotationMakerScreen(surface: Color, foreground: Color, muted: Color) {
    val context = LocalContext.current
    var customer by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var costRate by remember { mutableStateOf("") }
    var markup by remember { mutableStateOf("20") }
    val lines = remember { mutableStateListOf<QuoteLine>() }
    var message by remember { mutableStateOf("") }
    val percent = (markup.toDoubleOrNull() ?: 0.0)
    val cost = lines.sumOf { it.quantity * it.costRate }
    val total = lines.sumOf { it.quantity * it.costRate * (1.0 + percent / 100.0) }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxSize()) {
        Text("Quotation Maker", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Build a multi-item estimate and export a customer PDF.", color = muted, fontSize = 13.sp)
        OutlinedTextField(customer, { customer = it }, label = { Text("Customer name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(site, { site = it }, label = { Text("Site address") }, modifier = Modifier.fillMaxWidth())
        Text("Add quotation item", color = FinanceGold, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(itemName, { itemName = it }, label = { Text("Item / work description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(quantity, { quantity = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
            OutlinedTextField(costRate, { costRate = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Your cost ₹/unit") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
        }
        Button(onClick = {
            val q = quantity.toDoubleOrNull(); val r = costRate.toDoubleOrNull()
            if (itemName.isBlank() || q == null || q <= 0 || r == null || r < 0) message = "Enter item, quantity and valid cost rate."
            else { lines.add(QuoteLine(itemName.trim(), q, r)); itemName = ""; quantity = "1"; costRate = ""; message = "Item added." }
        }, modifier = Modifier.fillMaxWidth()) { Text("Add Item") }
        OutlinedTextField(markup, { markup = it.filter { ch -> ch.isDigit() || ch == '.' }.take(5) }, label = { Text("Markup (%) — default 20") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        if (percent < 10 && lines.isNotEmpty()) Text("Warning: markup is below 10%. Check your margin before sharing.", color = Color(0xFFE0A04B), fontSize = 12.sp)
        Text("Items (${lines.size})", color = FinanceGold, fontWeight = FontWeight.Bold)
        Column(Modifier.weight(1f).fillMaxWidth().padding(vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            lines.forEachIndexed { index, line ->
                Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(10.dp)) {
                    Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(line.name, color = foreground, fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text("${line.quantity} × ₹${"%.2f".format(line.costRate)} cost", color = muted, fontSize = 11.sp) }
                        TextButton(onClick = { lines.removeAt(index) }) { Text("Remove", color = FinanceGold) }
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Customer quotation total", color = FinanceGold, fontWeight = FontWeight.Bold)
                Text("₹${"%.2f".format(total)}", color = foreground, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Internal cost and profit are not printed on customer PDF.", color = muted, fontSize = 11.sp)
            }
        }
        Button(onClick = {
            if (customer.isBlank() || lines.isEmpty()) message = "Enter customer name and add at least one item."
            else if (percent < 0 || percent > 500) message = "Enter markup between 0 and 500%."
            else {
                try {
                    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    val quoteNo = "AICAM-$stamp"
                    val folder = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir, "AI_CAM_PRO_Quotations")
                    if (!folder.exists()) folder.mkdirs()
                    val file = File(folder, "$quoteNo.pdf")
                    val doc = PdfDocument()
                    val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    var y = 55f
                    paint.textSize = 22f; paint.isFakeBoldText = true; page.canvas.drawText("DREAM HOME INTERIOR SOLUTION", 38f, y, paint)
                    paint.isFakeBoldText = false; paint.textSize = 11f; y += 25f; page.canvas.drawText("Kolkata | Customer Quotation", 38f, y, paint)
                    paint.textSize = 12f; y += 42f; page.canvas.drawText("Quotation: $quoteNo", 38f, y, paint)
                    y += 22f; page.canvas.drawText("Customer: $customer", 38f, y, paint)
                    y += 22f; page.canvas.drawText("Site: ${site.ifBlank { "Not specified" }}", 38f, y, paint)
                    y += 38f; paint.isFakeBoldText = true; page.canvas.drawText("DESCRIPTION", 38f, y, paint); page.canvas.drawText("QTY", 355f, y, paint); page.canvas.drawText("RATE", 415f, y, paint); page.canvas.drawText("AMOUNT", 490f, y, paint)
                    paint.isFakeBoldText = false; y += 20f
                    lines.forEach { line ->
                        val sellRate = line.costRate * (1.0 + percent / 100.0)
                        val amount = sellRate * line.quantity
                        val label = if (line.name.length > 42) line.name.take(39) + "..." else line.name
                        page.canvas.drawText(label, 38f, y, paint); page.canvas.drawText("%.2f".format(line.quantity), 355f, y, paint); page.canvas.drawText("%.2f".format(sellRate), 415f, y, paint); page.canvas.drawText("%.2f".format(amount), 490f, y, paint)
                        y += 22f
                    }
                    y += 22f; paint.isFakeBoldText = true; paint.textSize = 16f; page.canvas.drawText("TOTAL: Rs. ${"%.2f".format(total)}", 365f, y, paint)
                    paint.isFakeBoldText = false; paint.textSize = 10f; y += 55f; page.canvas.drawText("Thank you for your business.", 38f, y, paint)
                    page.canvas.drawText("This quotation is subject to final site measurement and scope confirmation.", 38f, y + 18f, paint)
                    doc.finishPage(page); FileOutputStream(file).use { doc.writeTo(it) }; doc.close()
                    val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                    val share = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    context.startActivity(Intent.createChooser(share, "Share customer quotation"))
                    message = "PDF created. Internal cost and profit are hidden from the customer copy."
                } catch (e: Exception) { message = "Could not create PDF: ${e.message ?: "unknown error"}" }
            }
        }, modifier = Modifier.fillMaxWidth(), enabled = lines.isNotEmpty()) { Text("Create & Share Customer PDF") }
        if (message.isNotBlank()) Text(message, color = FinanceGold, fontSize = 11.sp)
        Text("Markup is applied to your entered cost rate. PDF shows selling rate only, not your internal cost or profit.", color = muted, fontSize = 10.sp)
    }
}