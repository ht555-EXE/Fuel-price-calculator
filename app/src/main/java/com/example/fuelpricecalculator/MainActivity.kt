package com.example.fuelpricecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fuelpricecalculator.ui.theme.FuelPriceCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuelPriceCalculatorTheme {
                FuelPriceCalculatorApp()
            }
        }
    }
}

@Composable
fun FuelPriceCalculatorApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon, contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            // FIX: We check which destination is selected and call the correct Composable
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.FAVORITES -> Text("Saved Trips", Modifier.padding(innerPadding))
                AppDestinations.PROFILE -> Text("Car Settings", Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // This is your core UI: Car Card, Buttons, and Trip List
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Zone 1: Current Vehicle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Current Vehicle", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "AA11 ABC", // This will eventually come from your DVLA logic
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Volkswagen Golf - 2.0 TDI", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Zone 2: Action Buttons
        Button(
            onClick = { /* TODO: Implement Journey Planner Activity Intent */ },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Start New Journey")
        }

        Button(
            onClick = { /* TODO: Implement SharedPreferences logic to change car */ },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Change Car")
        }

        // Zone 3: Recent Activity Header
        Text(
            text = "Recent Trips",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Zone 3: The List (Requirement #4: RecyclerView equivalent)
        val dummyTrips = listOf("London to Exeter", "Manchester to Leeds", "Birmingham to Bristol")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(dummyTrips) { tripName ->
                TripItem(destination = tripName, cost = "£${(15..45).random()}.20")
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Trip Details", Icons.Default.LocationOn),
    PROFILE("Setting", Icons.Default.Settings),
}

@Composable
fun TripItem(destination: String, cost: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = destination, fontWeight = FontWeight.Medium)
            Text(text = "£14.50", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}