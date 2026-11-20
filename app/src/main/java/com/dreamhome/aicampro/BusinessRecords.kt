package com.dreamhome.aicampro

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class CustomerRecord(val id: Long, val name: String, val phone: String, val address: String)
private data class ProjectRecord(val id: Long, val name: String, val customer: String, val site: String, val area: String, val status: String)

private class BusinessDatabase(context: Context) : SQLiteOpenHelper(context, "ai_cam_business.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE customers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, phone TEXT NOT NULL, address TEXT NOT NULL)")
        db.execSQL("CREATE TABLE projects (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, customer TEXT NOT NULL, site TEXT NOT NULL, area TEXT NOT NULL, status TEXT NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { }
    fun addCustomer(name: String, phone: String, address: String) {
        val values = ContentValues().apply { put("name", name); put("phone", phone); put("address", address) }
        writableDatabase.insert("customers", null, values)
    }
    fun customers(): List<CustomerRecord> {
        val result = mutableListOf<CustomerRecord>()
        readableDatabase.query("customers", null, null, null, null, null, "id DESC").use { c ->
            while (c.moveToNext()) result.add(CustomerRecord(c.getLong(c.getColumnIndexOrThrow("id")), c.getString(c.getColumnIndexOrThrow("name")), c.getString(c.getColumnIndexOrThrow("phone")), c.getString(c.getColumnIndexOrThrow("address"))))
        }
        return result
    }
    fun addProject(name: String, customer: String, site: String, area: String, status: String) {
        val values = ContentValues().apply { put("name", name); put("customer", customer); put("site", site); put("area", area); put("status", status) }
        writableDatabase.insert("projects", null, values)
    }
    fun projects(): List<ProjectRecord> {
        val result = mutableListOf<ProjectRecord>()
        readableDatabase.query("projects", null, null, null, null, null, "id DESC").use { c ->
            while (c.moveToNext()) result.add(ProjectRecord(c.getLong(c.getColumnIndexOrThrow("id")), c.getString(c.getColumnIndexOrThrow("name")), c.getString(c.getColumnIndexOrThrow("customer")), c.getString(c.getColumnIndexOrThrow("site")), c.getString(c.getColumnIndexOrThrow("area")), c.getString(c.getColumnIndexOrThrow("status"))))
        }
        return result
    }
}

@Composable
internal fun BusinessRecordsScreen(isProjects: Boolean, surface: Color, foreground: Color, muted: Color) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { BusinessDatabase(context.applicationContext) }
    val customerItems = remember { mutableStateListOf<CustomerRecord>() }
    val projectItems = remember { mutableStateListOf<ProjectRecord>() }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Planning") }
    var message by remember { mutableStateOf("") }
    LaunchedEffect(isProjects) {
        customerItems.clear(); customerItems.addAll(db.customers())
        projectItems.clear(); projectItems.addAll(db.projects())
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (isProjects) "Project Manager" else "Customer Management", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(if (isProjects) "Save project and site details on this device." else "Save customer contact details privately on this device.", color = muted, fontSize = 13.sp)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(if (isProjects) "Project name" else "Customer name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (isProjects) {
            OutlinedTextField(value = customer, onValueChange = { customer = it }, label = { Text("Customer name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = site, onValueChange = { site = it }, label = { Text("Site address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area (sq ft)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status (e.g. Planning / In Progress / Completed)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        } else {
            OutlinedTextField(value = phone, onValueChange = { phone = it.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' || ch == '-' } }, label = { Text("Phone number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address / notes") }, modifier = Modifier.fillMaxWidth())
        }
        Button(onClick = {
            if (name.isBlank()) { message = if (isProjects) "Enter a project name." else "Enter a customer name." }
            else if (isProjects) {
                if (site.isBlank()) message = "Enter the site address." else { db.addProject(name.trim(), customer.trim(), site.trim(), area.trim(), status.trim().ifBlank { "Planning" }); projectItems.clear(); projectItems.addAll(db.projects()); name = ""; customer = ""; site = ""; area = ""; status = "Planning"; message = "Project saved on this device." }
            } else {
                if (phone.isBlank()) message = "Enter a phone number." else { db.addCustomer(name.trim(), phone.trim(), address.trim()); customerItems.clear(); customerItems.addAll(db.customers()); name = ""; phone = ""; address = ""; message = "Customer saved on this device." }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text(if (isProjects) "Save Project" else "Save Customer") }
        if (message.isNotBlank()) Text(message, color = Gold, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(if (isProjects) "Saved projects (${projectItems.size})" else "Saved customers (${customerItems.size})", color = Gold, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isProjects) items(projectItems, key = { it.id }) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.name, color = foreground, fontWeight = FontWeight.SemiBold)
                        Text("Customer: ${item.customer.ifBlank { "Not specified" }}", color = muted, fontSize = 12.sp)
                        Text("Site: ${item.site}", color = muted, fontSize = 12.sp)
                        Row { Text("Area: ${item.area.ifBlank { "—" }} sq ft", color = muted, fontSize = 12.sp); Spacer(Modifier.weight(1f)); Text(item.status, color = Gold, fontSize = 12.sp) }
                    }
                }
            } else items(customerItems, key = { it.id }) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.name, color = foreground, fontWeight = FontWeight.SemiBold)
                        Text(item.phone, color = muted, fontSize = 12.sp)
                        if (item.address.isNotBlank()) Text(item.address, color = muted, fontSize = 12.sp)
                    }
                }
            }
        }
        Text("Records are stored locally in the app database. They are not uploaded automatically.", color = muted, fontSize = 11.sp)
    }
}