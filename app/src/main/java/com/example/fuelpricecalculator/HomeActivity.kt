package com.example.fuelpricecalculator

import android.app.Application
import android.content.Intent
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fuelpricecalculator.ui.theme.FuelPriceCalculatorTheme
import kotlin.collections.emptyList
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.libraries.places.api.Places
import java.util.concurrent.TimeUnit

class TripApplication : Application(){
    val database by lazy { AppDatabase.getDatabase(this) }
    private val dvlaRetrofit by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://driver-vehicle-licensing.api.gov.uk/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
    }

    private val googleRetrofit by lazy{
        retrofit2.Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
    }
    private val dvlaApiService by lazy {dvlaRetrofit.create(DvlaApiService::class.java)}
    private val googleApiService by lazy {googleRetrofit.create(DistanceMatrixAPI::class.java)}
    lateinit var settingsManager: SettingsManager
    val repository by lazy { FuelRepository(database.tripDao(),
                                            database.carDao(), dvlaApiService, googleApiService, settingsManager) }

    override fun onCreate() {
        super.onCreate()
        if(!Places.isInitialized()){
            //TODO: fix api keys make local
            Places.initializeWithNewPlacesApiEnabled(applicationContext, "AIzaSyDOhBfgUByu7EzkbviohlK87YPOiGVYals")
        }
        settingsManager = SettingsManager(this)
        val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()
        val workRequest = PeriodicWorkRequestBuilder<FuelPriceWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FuelSync",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

class HomeActivity : ComponentActivity() {
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
    //TODO: efficiencies in fuelViewModel
    val trips by fuelViewModel.tripsForCurrentCar.observeAsState(initial = emptyList())
    val cars by fuelViewModel.allCars.observeAsState(emptyList())
    val isMetric by fuelViewModel.useMetric.observeAsState(initial = false)
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        HomeScreen(
            modifier = Modifier.padding(innerPadding),
            trips = trips,
            currentCar = cars.firstOrNull(),
            fuelViewModel = fuelViewModel,
            isMetric = isMetric
        )
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, trips: List<Trip>, currentCar: Car?, fuelViewModel: FuelViewModel, isMetric: Boolean) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Current Vehicle",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
            )
            IconButton(onClick = {
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
            }){
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable{
                val intent = Intent(context, ChangeCarActivity::class.java)
                context.startActivity(intent)
            },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentCar?.license ?: "No Car Added",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = currentCar?.let { "${it.make ?: ""} \u00B7 ${it.colour} \u00B7 ${it.fuelType}" }
                        ?: "Tap to add a vehicle", style = MaterialTheme.typography.bodyLarge)
                    Text(text = currentCar?.let { "Efficiency - ${String.format("%.1f", it.efficiency)} mpg"} ?: "")
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.padding(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Trips",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
            )
            IconButton(onClick = {
                val intent = Intent(context, AddTripActivity::class.java)
                context.startActivity(intent)
            }){
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        if (trips.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Trips Recorded",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(trips,
                    key = {it.id}
                ) { trip ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {value ->
                            if (value == SwipeToDismissBoxValue.EndToStart){
                                fuelViewModel.deleteTrip(trip)
                                true
                            } else{
                                false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color =
                                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else Color.Transparent

                            Box(
                                Modifier.fillMaxSize().background(color, CardDefaults.shape)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    ) {
                    TripItem(trip = trip, isMetric = isMetric)
                    }
                }
            }
        }
    }
}

@Composable
fun TripItem(trip: Trip, isMetric: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${trip.origin.substringBefore(",")} \u2192 ${trip.destination.substringBefore(",")}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    softWrap = true,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Now use the Boolean passed from the parent
                val distanceText = if (!isMetric) {
                    "${String.format("%.1f", trip.distance)} miles"
                } else {
                    "${String.format("%.1f", trip.distance * 1.609)} km"
                }

                Text(
                    text = "$distanceText \u00B7 ${trip.duration}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "£${String.format("%.2f", trip.cost)}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}