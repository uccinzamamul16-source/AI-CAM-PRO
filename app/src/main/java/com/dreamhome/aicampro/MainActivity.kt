package com.dreamhome.aicampro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF17212B)
private val Gold = Color(0xFFC69A45)
private val Warm = Color(0xFFF7F4ED)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { DashboardScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen() {
    val modules = listOf(
        "Camera Studio", "Interior AI Studio",
        "Measurements", "Material Rates",
        "Quotations", "Projects",
        "Payments", "Private Gallery"
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI CAM PRO", color = Ink)
                        Text("Dream Home Interior Solution", fontSize = 12.sp, color = Gold)
                    }
                }
            )
        },
        containerColor = Warm
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modules) { module -> ModuleCard(module) }
        }
    }
}

@Composable
private fun ModuleCard(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(18.dp)
        ) {
            Text(title, color = Ink, fontSize = 16.sp)
            Text("Planned module", color = Gold, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
