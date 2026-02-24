package com.example.fuelpricecalculator

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fuelpricecalculator.ui.theme.FuelPriceCalculatorTheme
import kotlin.collections.emptyList

class TripApplication : Application(){
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { FuelRepository(database.tripDao(), database.carDao()) }
}

class MainActivity : ComponentActivity() {
    private val viewModel: FuelViewModel by viewModels {
        val repository = (application as TripApplication).repository
        FuelViewModelFactory(repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            FuelPriceCalculatorTheme {
                FuelPriceCalculatorApp(viewModel)
            }
        }
    }
}

@Composable
fun FuelPriceCalculatorApp(fuelViewModel: FuelViewModel) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val trips by fuelViewModel.allTrips.observeAsState(initial = emptyList())
    val cars by fuelViewModel.allCars.observeAsState(emptyList())
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
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(modifier = Modifier.padding(innerPadding), trips = trips, currentCar = cars.firstOrNull())
                AppDestinations.FAVORITES -> Text("Saved Trips", Modifier.padding(innerPadding))
                AppDestinations.PROFILE -> Text("Car Settings", Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, trips: List<Trip>, currentCar: Car?) {
    // This is your core UI: Car Card, Buttons, and Trip List
    Column(modifier = modifier.fillMaxSize().padding(16.dp),
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
                    text = currentCar?.license ?: "No Car Added",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = currentCar?.let { "${it.make ?: ""} ${it.model ?: ""}"} ?: "No car details available", style = MaterialTheme.typography.bodyLarge)
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
            onClick = { /* TODO: Implement logic to change car */ },
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(trips) { trip ->
                TripItem(trip = trip)
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
fun TripItem(trip: Trip) { // Changed parameters to take a Trip object
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = trip.destination, fontWeight = FontWeight.Medium)
                Text(text = "${trip.distance} miles", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "£${String.format("%.2f", trip.cost)}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}