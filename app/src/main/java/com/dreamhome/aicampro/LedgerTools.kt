package com.dreamhome.aicampro

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val LedgerGold = Color(0xFFD2AA58)
private data class LedgerEntry(val id: Long, val kind: String, val project: String, val amount: Double, val note: String, val createdAt: Long)

private class LedgerDatabase(context: Context) : SQLiteOpenHelper(context, "ai_cam_ledger.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ledger (id INTEGER PRIMARY KEY AUTOINCREMENT, kind TEXT NOT NULL, project TEXT NOT NULL, amount REAL NOT NULL, note TEXT NOT NULL, created_at INTEGER NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun add(kind: String, project: String, amount: Double, note: String) {
        val v = ContentValues().apply { put("kind", kind); put("project", project); put("amount", amount); put("note", note); put("created_at", System.currentTimeMillis()) }
        writableDatabase.insert("ledger", null, v)
    }
    fun all(): List<LedgerEntry> {
        val out = mutableListOf<LedgerEntry>()
        readableDatabase.query("ledger", null, null, null, null, null, "created_at DESC").use { c ->
            while (c.moveToNext()) out.add(LedgerEntry(c.getLong(0), c.getString(1), c.getString(2), c.getDouble(3), c.getString(4), c.getLong(5)))
        }
        return out
    }
    fun delete(id: Long) { writableDatabase.delete("ledger", "id=?", arrayOf(id.toString())) }
}

@Composable
internal fun PaymentsScreen(surface: Color, foreground: Color, muted: Color) {
    val context = LocalContext.current
    val db = remember { LedgerDatabase(context.applicationContext) }
    val entries = remember { mutableStateListOf<LedgerEntry>() }
    var kind by remember { mutableStateOf("Payment received") }
    var project by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    fun refresh() { entries.clear(); entries.addAll(db.all()) }
    LaunchedEffect(Unit) { refresh() }
    val received = entries.filter { it.kind == "Payment received" }.sumOf { it.amount }
    val expenses = entries.filter { it.kind == "Expense" }.sumOf { it.amount }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("Payments & Accounts", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Record advances, customer payments and business expenses locally.", color = muted, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard("Received", received, surface, foreground, Modifier.weight(1f))
            SummaryCard("Expenses", expenses, surface, foreground, Modifier.weight(1f))
        }
        SummaryCard("Net cash recorded", received - expenses, surface, foreground, Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = kind == "Payment received", onClick = { kind = "Payment received" }, label = { Text("Payment") })
            FilterChip(selected = kind == "Expense", onClick = { kind = "Expense" }, label = { Text("Expense") })
        }
        OutlinedTextField(project, { project = it }, label = { Text("Project / customer") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(amount, { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Amount ₹") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(note, { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val value = amount.toDoubleOrNull()
            if (project.isBlank() || value == null || value <= 0) message = "Enter project/customer and a valid amount."
            else { db.add(kind, project.trim(), value, note.trim()); project = ""; amount = ""; note = ""; message = "Record saved on this device."; refresh() }
        }, modifier = Modifier.fillMaxWidth()) { Text("Save $kind") }
        if (message.isNotBlank()) Text(message, color = LedgerGold, fontSize = 12.sp)
        Text("Transaction history (${entries.size})", color = LedgerGold, fontWeight = FontWeight.Bold)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            items(entries, key = { it.id }) { e ->
                Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(10.dp)) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(e.kind + " • " + e.project, color = foreground, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(e.createdAt)) + if (e.note.isNotBlank()) " • ${e.note}" else "", color = muted, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) { Text("₹${"%.2f".format(e.amount)}", color = LedgerGold, fontWeight = FontWeight.Bold); TextButton(onClick = { db.delete(e.id); refresh() }) { Text("Delete") } }
                    }
                }
            }
        }
        Text("This is a cashbook, not a bank statement or tax ledger. Verify entries before using them for accounting.", color = muted, fontSize = 10.sp)
    }
}

@Composable
internal fun BusinessReportsScreen(surface: Color, foreground: Color, muted: Color) {
    val context = LocalContext.current
    val db = remember { LedgerDatabase(context.applicationContext) }
    var entries by remember { mutableStateOf(emptyList<LedgerEntry>()) }
    LaunchedEffect(Unit) { entries = db.all() }
    val now = Calendar.getInstance()
    now.set(Calendar.DAY_OF_MONTH, 1); now.set(Calendar.HOUR_OF_DAY, 0); now.set(Calendar.MINUTE, 0); now.set(Calendar.SECOND, 0); now.set(Calendar.MILLISECOND, 0)
    val monthStart = now.timeInMillis
    val monthEntries = entries.filter { it.createdAt >= monthStart }
    val allIn = entries.filter { it.kind == "Payment received" }.sumOf { it.amount }
    val allOut = entries.filter { it.kind == "Expense" }.sumOf { it.amount }
    val monthIn = monthEntries.filter { it.kind == "Payment received" }.sumOf { it.amount }
    val monthOut = monthEntries.filter { it.kind == "Expense" }.sumOf { it.amount }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Business Reports", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Summary calculated from entries saved in Payments & Accounts.", color = muted, fontSize = 13.sp)
        Text("This month", color = LedgerGold, fontWeight = FontWeight.Bold)
        SummaryCard("Payments received", monthIn, surface, foreground, Modifier.fillMaxWidth())
        SummaryCard("Expenses recorded", monthOut, surface, foreground, Modifier.fillMaxWidth())
        SummaryCard("Net recorded this month", monthIn - monthOut, surface, foreground, Modifier.fillMaxWidth())
        Divider()
        Text("All-time totals", color = LedgerGold, fontWeight = FontWeight.Bold)
        SummaryCard("Total received", allIn, surface, foreground, Modifier.fillMaxWidth())
        SummaryCard("Total expenses", allOut, surface, foreground, Modifier.fillMaxWidth())
        SummaryCard("Net cash recorded", allIn - allOut, surface, foreground, Modifier.fillMaxWidth())
        Text("Pending customer balances and project profitability need project contract values and cost records; they are not included yet.", color = muted, fontSize = 11.sp)
        Text("Report refreshes when this screen is reopened. Add or correct entries in Payments & Accounts.", color = muted, fontSize = 11.sp)
    }
}

@Composable
private fun SummaryCard(label: String, value: Double, surface: Color, foreground: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) { Text(label, color = foreground, fontSize = 12.sp); Text("₹${"%.2f".format(value)}", color = LedgerGold, fontSize = 19.sp, fontWeight = FontWeight.Bold) }
    }
}